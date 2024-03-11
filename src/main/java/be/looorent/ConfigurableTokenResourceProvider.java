package be.looorent;

import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

/**
 * @author Lorent Lempereur
 */
public class ConfigurableTokenResourceProvider implements RealmResourceProvider {
    private final KeycloakSession session;
    private final EventBuilder eventBuilder;
    private final ConfigurationTokenResourceConfiguration configuration;

    ConfigurableTokenResourceProvider(KeycloakSession session,
                                      EventBuilder eventBuilder,
                                      ConfigurationTokenResourceConfiguration configuration) {
        this.session = session;
        this.eventBuilder = eventBuilder;
        this.configuration = configuration;
    }

    @Override
    public Object getResource() {
        return new ConfigurableTokenResource(session, eventBuilder, configuration);
    }

    @Override
    public void close() {}
}
