package be.looorent;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.crypto.SignatureVerifierContext;
import org.keycloak.events.EventBuilder;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.*;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.cors.Cors;

import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static java.util.Optional.ofNullable;
import static org.keycloak.services.cors.Cors.ACCESS_CONTROL_ALLOW_METHODS;
import static org.keycloak.services.cors.Cors.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.keycloak.services.util.DefaultClientSessionContext.fromClientSessionScopeParameter;

/**
 * @author Lorent Lempereur
 */
public class ConfigurableTokenResource {

    private static final Logger LOG = Logger.getLogger(ConfigurableTokenResource.class);

    private final KeycloakSession session;
    private final TokenManager tokenManager;
    private final EventBuilder event;
    private final ConfigurationTokenResourceConfiguration configuration;

    ConfigurableTokenResource(KeycloakSession session,
                              EventBuilder event,
                              ConfigurationTokenResourceConfiguration configuration) {
        this.session = session;
        this.event = event;
        this.tokenManager = new TokenManager();
        this.configuration = configuration;
    }

    @Path("")
    @OPTIONS
    public Response preflight() {
        return session.getProvider(Cors.class)
                .request(session.getContext().getHttpRequest())
                .builder(Response.ok())
                .auth()
                .preflight()
                .allowedMethods("POST", "OPTIONS")
                .allowedOrigins(configuration.getCorsOrigins())
                .build();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response createToken(TokenConfiguration tokenConfiguration) {
        HttpRequest request = session.getContext().getHttpRequest();
        try {
            AccessToken accessToken = validateTokenAndUpdateSession(request);
            UserSessionModel userSession = this.findSession();
            AccessTokenResponse response = this.createAccessToken(userSession, accessToken, tokenConfiguration);
            return this.buildCorsResponse(request, response);
        } catch (ConfigurableTokenException e) {
            LOG.error("An error occurred when fetching an access token", e);
            ErrorRepresentation error = new ErrorRepresentation();
            error.setErrorMessage(e.getMessage());
            return this.buildCorsResponse(request, Response.status(BAD_REQUEST).entity(error).type(MediaType.APPLICATION_JSON));
        }
    }

    private AccessTokenResponse createAccessToken(UserSessionModel userSession,
                                                  AccessToken accessToken,
                                                  TokenConfiguration tokenConfiguration) {
        RealmModel realm = this.session.getContext().getRealm();
        ClientModel client = realm.getClientByClientId(accessToken.getIssuedFor());
        LOG.infof("Configurable token requested for username=%s and client=%s on realm=%s", userSession.getUser().getUsername(), client.getClientId(), realm.getName());
        AuthenticatedClientSessionModel clientSession = userSession.getAuthenticatedClientSessionByClient(client.getId());
        ClientSessionContext clientSessionContext = fromClientSessionScopeParameter(clientSession, session);

        AccessToken newToken = tokenManager.createClientAccessToken(session, realm, client, userSession.getUser(), userSession, clientSessionContext);
        updateTokenExpiration(newToken, tokenConfiguration, userSession.getUser());
        return buildResponse(realm, userSession, client, clientSession, newToken);
    }

    private AccessToken validateTokenAndUpdateSession(HttpRequest request) throws ConfigurableTokenException {
        try {
            RealmModel realm = session.getContext().getRealm();
            String tokenString = readAccessTokenFrom(request);
            @SuppressWarnings("unchecked") TokenVerifier<AccessToken> verifier = TokenVerifier.create(tokenString, AccessToken.class).withChecks(
                    TokenVerifier.IS_ACTIVE,
                    new TokenVerifier.RealmUrlCheck(Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realm.getName()))
            );
            SignatureVerifierContext verifierContext = session.getProvider(SignatureProvider.class, verifier.getHeader().getAlgorithm().name()).verifier(verifier.getHeader().getKeyId());
            verifier.verifierContext(verifierContext);
            AccessToken accessToken = verifier.verify().getToken();
            if (!tokenManager.checkTokenValidForIntrospection(session, realm, accessToken, true, event)) {
                throw new VerificationException("introspection_failed");
            }
            return accessToken;
        } catch (VerificationException e) {
            LOG.warn("Keycloak-ConfigurableToken: introspection of token failed", e);
            throw new ConfigurableTokenException("access_token_introspection_failed: "+e.getMessage());
        }
    }

    private String readAccessTokenFrom(HttpRequest request) throws ConfigurableTokenException {
        String authorization = request.getHttpHeaders().getHeaderString(AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            LOG.warn("Keycloak-ConfigurableToken: no authorization header with bearer token");
            throw new ConfigurableTokenException("bearer_token_missing_in_authorization_header");
        }
        String token = authorization.substring(7);
        if (token.isEmpty()) {
            LOG.warn("Keycloak-ConfigurableToken: empty access token");
            throw new ConfigurableTokenException("missing_access_token");
        }
        return token;
    }

    private UserSessionModel findSession() throws ConfigurableTokenException {
        AuthenticationManager.AuthResult authenticated = new AppAuthManager.BearerTokenAuthenticator(session).authenticate();

        if (authenticated == null) {
            LOG.warn("Keycloak-ConfigurableToken: user not authenticated");
            throw new ConfigurableTokenException("not_authenticated");
        }

        if (authenticated.getToken().getRealmAccess() == null) {
            LOG.warn("Keycloak-ConfigurableToken: no realm associated with authorization");
            throw new ConfigurableTokenException("wrong_realm");
        }

        UserModel user = authenticated.getUser();
        if (user == null || !user.isEnabled()) {
            LOG.warn("Keycloak-ConfigurableToken: user does not exist or is not enabled");
            throw new ConfigurableTokenException("invalid_user");
        }

        UserSessionModel userSession = authenticated.getSession();
        if (userSession == null) {
            LOG.warn("Keycloak-ConfigurableToken: user does not have any active session");
            throw new ConfigurableTokenException("missing_user_session");
        }

        return userSession;
    }

    private Response buildCorsResponse(HttpRequest request, AccessTokenResponse response) {
        return buildCorsResponse(request, Response.ok(response).type(APPLICATION_JSON_TYPE));
    }

    private Response buildCorsResponse(HttpRequest request, Response.ResponseBuilder responseBuilder) {
        return session.getProvider(Cors.class)
                .request(request)
                .auth()
                .allowedMethods("POST", "OPTIONS")
                .auth()
                .exposedHeaders(ACCESS_CONTROL_ALLOW_METHODS, ACCESS_CONTROL_ALLOW_ORIGIN)
                .allowedOrigins(configuration.getCorsOrigins())
                .builder(responseBuilder)
                .build();
    }

    private AccessTokenResponse buildResponse(RealmModel realm,
                                              UserSessionModel userSession,
                                              ClientModel client,
                                              AuthenticatedClientSessionModel clientSession,
                                              AccessToken token) {
        EventBuilder eventBuilder = new EventBuilder(realm, session, session.getContext().getConnection());
        ClientSessionContext clientSessionContext = fromClientSessionScopeParameter(clientSession, session);
        return tokenManager.responseBuilder(realm, client, eventBuilder, session, userSession, clientSessionContext)
                .accessToken(token)
                .build();
    }

    private void updateTokenExpiration(AccessToken token, TokenConfiguration tokenConfiguration, UserModel user) {
        boolean longLivedTokenAllowed = ofNullable(session.getContext().getRealm().getRole(this.configuration.getLongLivedTokenRole()))
                .map(user::hasRole)
                .orElse(false);
        token.expiration(tokenConfiguration.computeTokenExpiration(token.getExp(), longLivedTokenAllowed));
    }

    static class ConfigurableTokenException extends Exception {
        public ConfigurableTokenException(String message) {
            super(message);
        }
    }
}
