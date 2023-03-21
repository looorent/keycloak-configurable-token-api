package be.looorent;

import org.jboss.logging.Logger;

import static java.lang.System.getenv;
import static java.util.Arrays.asList;

/**
 * Configures this API.
 * @author Lorent Lempereur
 */
public class ConfigurationTokenResourceConfiguration {
    private static final Logger LOG = Logger.getLogger(ConfigurableTokenResourceProviderFactory.class);

    private static final String KEYCLOAK_LONG_LIVED_ROLE_NAME = "KEYCLOAK_LONG_LIVED_ROLE_NAME";
    private static final String DEFAULT_KEYCLOAK_LONG_LIVED_ROLE_NAME = "long_lived_token";
    private static final String KEYCLOAK_LONG_LIVED_CORS_ORIGINS = "KEYCLOAK_LONG_LIVED_CORS_ORIGINS";
    private static final String[] DEFAULT_KEYCLOAK_LONG_LIVED_CORS_ORIGINS = new String[] { "*" };

    private final String longLivedTokenRole;
    private final String[] corsOrigins;

    public static ConfigurationTokenResourceConfiguration readFromEnvironment() {
        String longLivedTokenRole = readLongLivedRoleFromEnvironment();
        String[] corsOrigins = readCorsOrigins();
        return new ConfigurationTokenResourceConfiguration(longLivedTokenRole, corsOrigins);
    }

    public ConfigurationTokenResourceConfiguration(String longLivedTokenRole, String[] corsOrigins) {
        this.longLivedTokenRole = longLivedTokenRole;
        this.corsOrigins = corsOrigins;
    }

    public String getLongLivedTokenRole() {
        return longLivedTokenRole;
    }

    @Override
    public String toString() {
        return "longLivedTokenRole=" + longLivedTokenRole;
    }

    public String[] getCorsOrigins() {
        return corsOrigins;
    }

    private static String readLongLivedRoleFromEnvironment() {
        String roleForLongLivedTokens = getenv(KEYCLOAK_LONG_LIVED_ROLE_NAME);
        if (roleForLongLivedTokens == null || roleForLongLivedTokens.trim().isEmpty()) {
            LOG.warn("Keycloak-ConfigurableToken : Long lived role name provided, using default one.");
            return DEFAULT_KEYCLOAK_LONG_LIVED_ROLE_NAME;
        } else {
            return roleForLongLivedTokens;
        }
    }

    private static String[] readCorsOrigins() {
        String corsOrigins = getenv(KEYCLOAK_LONG_LIVED_CORS_ORIGINS);
        if (corsOrigins == null || corsOrigins.trim().isEmpty()) {
            LOG.warnf("Keycloak-ConfigurableToken : no cors origin is defined in environment variables. Using '%s'.", asList(DEFAULT_KEYCLOAK_LONG_LIVED_CORS_ORIGINS));
            return DEFAULT_KEYCLOAK_LONG_LIVED_CORS_ORIGINS;
        } else {
            String[] origins = parseCorsAllowedOrigins(corsOrigins);
            LOG.debugf("Keycloak-LoginActionToken : CORS origin allowed (for sessions) configured: %s", asList(origins));
            return origins;
        }
    }

    private static String[] parseCorsAllowedOrigins(String corsOrigins) {
        return corsOrigins.trim().split(",");
    }
}
