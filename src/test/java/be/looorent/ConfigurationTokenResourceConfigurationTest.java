package be.looorent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTokenResourceConfigurationTest {

    @Test
    void constructorStoresValues() {
        String[] origins = {"https://example.com", "https://other.com"};
        ConfigurationTokenResourceConfiguration config =
                new ConfigurationTokenResourceConfiguration("my_role", origins);

        assertEquals("my_role", config.getLongLivedTokenRole());
        assertArrayEquals(origins, config.getCorsOrigins());
    }

    @Test
    void toStringIncludesRole() {
        ConfigurationTokenResourceConfiguration config =
                new ConfigurationTokenResourceConfiguration("custom_role", new String[]{"*"});

        assertEquals("longLivedTokenRole=custom_role", config.toString());
    }
}
