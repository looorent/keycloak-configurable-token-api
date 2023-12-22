package be.looorent;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

/**
 * @author Lorent Lempereur
 */
public class ConfigurableTokenResourceProvider implements RealmResourceProvider {
    private final KeycloakSession session;
    private final ConfigurationTokenResourceConfiguration configuration;

    ConfigurableTokenResourceProvider(KeycloakSession session, ConfigurationTokenResourceConfiguration configuration) {
        this.session = session;
        this.configuration = configuration;
    }

    @Override
    public Object getResource() {
        return new ConfigurableTokenResource(session, configuration);
    }

    @Override
    public void close() {}
}
