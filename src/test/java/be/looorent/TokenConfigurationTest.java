package be.looorent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.common.util.Time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TokenConfigurationTest {

    private static final int FIXED_TIME = 1_700_000_000;

    @BeforeEach
    void setUp() {
        Time.setOffset(FIXED_TIME - Time.currentTime());
    }

    @AfterEach
    void tearDown() {
        Time.setOffset(0);
    }

    @Test
    void nullLifespan_returnsMaxExpiration() {
        TokenConfiguration config = new TokenConfiguration(null);
        long maxExpiration = FIXED_TIME + 300L;

        long result = config.computeTokenExpiration(maxExpiration, false);

        assertEquals(maxExpiration, result);
    }

    @Test
    void nullLifespan_nullMaxExpiration_returnsZero() {
        TokenConfiguration config = new TokenConfiguration(null);

        long result = config.computeTokenExpiration(null, false);

        assertEquals(0L, result);
    }

    @Test
    void shortLivedToken_withinMaxExpiration() {
        TokenConfiguration config = new TokenConfiguration(60);
        long maxExpiration = FIXED_TIME + 300L;

        long result = config.computeTokenExpiration(maxExpiration, false);

        assertEquals(FIXED_TIME + 60L, result);
    }

    @Test
    void shortLivedToken_exceedsMaxExpiration_clampedToMax() {
        TokenConfiguration config = new TokenConfiguration(600);
        long maxExpiration = FIXED_TIME + 300L;

        long result = config.computeTokenExpiration(maxExpiration, false);

        assertEquals(maxExpiration, result);
    }

    @Test
    void longLivedTokenAllowed_canExceedMaxExpiration() {
        TokenConfiguration config = new TokenConfiguration(600);
        long maxExpiration = FIXED_TIME + 300L;

        long result = config.computeTokenExpiration(maxExpiration, true);

        assertEquals(FIXED_TIME + 600L, result);
    }

    @Test
    void longLivedTokenAllowed_veryLargeLifespan() {
        int oneYear = 31_556_952;
        TokenConfiguration config = new TokenConfiguration(oneYear);
        long maxExpiration = FIXED_TIME + 300L;

        long result = config.computeTokenExpiration(maxExpiration, true);

        assertEquals(FIXED_TIME + (long) oneYear, result);
    }

    @Test
    void nullMaxExpiration_withLifespan_usesZeroAsMax() {
        TokenConfiguration config = new TokenConfiguration(60);

        long result = config.computeTokenExpiration(null, false);

        // maxExpiration defaults to 0, requested = currentTime + 60 which is > 0, so clamped to 0
        assertEquals(0L, result);
    }

    @Test
    void nullMaxExpiration_longLivedAllowed_ignoresMax() {
        TokenConfiguration config = new TokenConfiguration(60);

        long result = config.computeTokenExpiration(null, true);

        assertEquals(FIXED_TIME + 60L, result);
    }

    @Test
    void zeroLifespan_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new TokenConfiguration(0));
    }

    @Test
    void negativeLifespan_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new TokenConfiguration(-1));
    }

    @Test
    void exactlyAtMaxExpiration() {
        TokenConfiguration config = new TokenConfiguration(300);
        long maxExpiration = FIXED_TIME + 300L;

        long result = config.computeTokenExpiration(maxExpiration, false);

        assertEquals(maxExpiration, result);
    }
}
