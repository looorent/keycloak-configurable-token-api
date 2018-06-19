package be.looorent;

import org.jboss.logging.Logger;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.*;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resources.Cors;

import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.keycloak.services.resources.Cors.ACCESS_CONTROL_ALLOW_METHODS;
import static org.keycloak.services.resources.Cors.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.keycloak.services.util.DefaultClientSessionContext.fromClientSessionScopeParameter;

/**
 * @author Lorent Lempereur
 */
public class ConfigurableTokenResourceProvider implements RealmResourceProvider {

    static final String ID = "configurable-token";
    private static final Logger LOG = Logger.getLogger(ConfigurableTokenResourceProvider.class);

    private final KeycloakSession session;
    private final TokenManager tokenManager;

    ConfigurableTokenResourceProvider(KeycloakSession session) {
        this.session = session;
        this.tokenManager = new TokenManager();
    }

    @Override
    public Object getResource() {
        return this;
    }

    @Override
    public void close() {}

    @OPTIONS
    public Response preflight(@Context HttpRequest request) {
        return Cors.add(request, Response.ok()).auth().preflight().allowedMethods("POST", "OPTIONS").build();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response createToken(TokenConfiguration tokenConfiguration, @Context HttpRequest request) {
        RealmModel realm = session.getContext().getRealm();
        AuthenticationManager.AuthResult authenticated = new AppAuthManager().authenticateBearerToken(session, realm);
        UserModel user = authenticated.getUser();
        UserSessionModel userSession = authenticated.getSession();
        ClientModel client = realm.getClientByClientId(authenticated.getToken().getAudience()[0]);
        AuthenticatedClientSessionModel clientSession = userSession.getAuthenticatedClientSessionByClient(client.getId());

        AccessToken token = createAccessToken(realm, user, userSession, client, clientSession);
        updateTokenExpiration(token, tokenConfiguration);
        AccessTokenResponse response = buildResponse(realm, userSession, client, clientSession, token);

        return buildCorsResponse(request, response);
    }

    private Response buildCorsResponse(@Context HttpRequest request, AccessTokenResponse response) {
        Cors cors = Cors.add(request)
                        .auth()
                        .allowedMethods("POST")
                        .auth()
                        .exposedHeaders(ACCESS_CONTROL_ALLOW_METHODS, ACCESS_CONTROL_ALLOW_ORIGIN)
                        .allowAllOrigins();
        return cors.builder(Response.ok(response).type(APPLICATION_JSON_TYPE)).build();
    }

    private AccessToken createAccessToken(RealmModel realm,
                                          UserModel user,
                                          UserSessionModel userSession,
                                          ClientModel client,
                                          AuthenticatedClientSessionModel clientSession) {
        LOG.infof("Configurable token requested for username=%s and client=%s on realm=%s", user.getUsername(), client.getClientId(), realm.getName());
        ClientSessionContext clientSessionContext = fromClientSessionScopeParameter(clientSession);
        return tokenManager.createClientAccessToken(session, realm, client, user, userSession, clientSessionContext);
    }

    private AccessTokenResponse buildResponse(RealmModel realm,
                                              UserSessionModel userSession,
                                              ClientModel client,
                                              AuthenticatedClientSessionModel clientSession,
                                              AccessToken token) {
        EventBuilder eventBuilder = new EventBuilder(realm, session, session.getContext().getConnection());
        ClientSessionContext clientSessionContext = fromClientSessionScopeParameter(clientSession);
        return tokenManager.responseBuilder(realm, client, eventBuilder, session, userSession, clientSessionContext)
                .accessToken(token)
                .build();
    }

    private void updateTokenExpiration(AccessToken token, TokenConfiguration tokenConfiguration) {
        token.expiration(tokenConfiguration.computeTokenExpiration(token.getExpiration()));
    }
}
