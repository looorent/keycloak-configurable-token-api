package be.looorent;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getenv;
/**
 * Configures this API.
 * @author Lorent Lempereur
 */
public class ConfigurationTokenResourceConfiguration {

    private static final String KEYCLOAK_LONG_LIVED_TOKEN_ALLOWED = "KEYCLOAK_LONG_LIVED_TOKEN_ALLOWED";

    private final boolean longLivedTokenAllowed;

    public static ConfigurationTokenResourceConfiguration readFromEnvironment() {
        String environmentLongLivedAllowed = getenv(KEYCLOAK_LONG_LIVED_TOKEN_ALLOWED);
        boolean areLongLivedAllowed = environmentLongLivedAllowed != null && parseBoolean(environmentLongLivedAllowed);
        return new ConfigurationTokenResourceConfiguration(areLongLivedAllowed);
    }

    public ConfigurationTokenResourceConfiguration(boolean longLivedTokenAllowed) {
        this.longLivedTokenAllowed = longLivedTokenAllowed;
    }

    public boolean isLongLivedTokenAllowed() {
        return longLivedTokenAllowed;
    }

    @Override
    public String toString() {
        return "longLivedTokenAllowed=" + longLivedTokenAllowed;
    }
}
