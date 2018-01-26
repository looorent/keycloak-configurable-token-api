package be.looorent;

import org.jboss.logging.Logger;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.*;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.resource.RealmResourceProvider;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;

import java.util.Set;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

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

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public AccessTokenResponse createToken(TokenConfiguration tokenConfiguration) {
        RealmModel realm = session.getContext().getRealm();
        AuthenticationManager.AuthResult authenticated = new AppAuthManager().authenticateBearerToken(session, realm);
        UserModel user = authenticated.getUser();
        UserSessionModel userSession = authenticated.getSession();
        ClientModel client = realm.getClientByClientId(authenticated.getToken().getAudience()[0]);
        AuthenticatedClientSessionModel clientSession = userSession.getAuthenticatedClientSessionByClient(client.getId());

        AccessToken token = createAccessToken(realm, user, userSession, client, clientSession);
        updateTokenExpiration(token, tokenConfiguration);
        return buildResponse(realm, userSession, client, clientSession, token);
    }

    private AccessToken createAccessToken(RealmModel realm,
                                          UserModel user,
                                          UserSessionModel userSession,
                                          ClientModel client,
                                          AuthenticatedClientSessionModel clientSession) {
        Set<RoleModel> requestedRoles = user.getRoleMappings();
        LOG.infof("Configurable token requested for username=%s and client=%s on realm=%s", user.getUsername(), client.getClientId(), realm.getName());
        return tokenManager.createClientAccessToken(session, requestedRoles, realm, client, user, userSession, clientSession);
    }

    private AccessTokenResponse buildResponse(RealmModel realm,
                                              UserSessionModel userSession,
                                              ClientModel client,
                                              AuthenticatedClientSessionModel clientSession,
                                              AccessToken token) {
        EventBuilder eventBuilder = new EventBuilder(realm, session, session.getContext().getConnection());
        return tokenManager.responseBuilder(realm, client, eventBuilder, session, userSession, clientSession)
                .accessToken(token)
                .build();
    }

    private void updateTokenExpiration(AccessToken token, TokenConfiguration tokenConfiguration) {
        token.expiration(tokenConfiguration.computeTokenExpiration(token.getExpiration()));
    }
}
