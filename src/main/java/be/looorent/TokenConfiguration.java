package be.looorent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.lang.Math.min;
import static java.util.Optional.ofNullable;
import static org.keycloak.common.util.Time.currentTime;

/**
 * @author Lorent Lempereur
 */
public class TokenConfiguration {

    @JsonProperty("tokenLifespanInSeconds")
    private final Integer tokenLifespanInSeconds;

    @JsonCreator
    public TokenConfiguration(@JsonProperty("tokenLifespanInSeconds") Integer tokenLifespanInSeconds) {
        this.tokenLifespanInSeconds = tokenLifespanInSeconds;
    }

    public Integer getTokenLifespanInSeconds() {
        return tokenLifespanInSeconds;
    }

    public long computeTokenExpiration(Long maxExpiration, boolean longLivedTokenAllowed) {
        long furthestExpiration = ofNullable(maxExpiration).orElse(0L);
        return ofNullable(getTokenLifespanInSeconds())
                .map(lifespan -> (long) currentTime() + lifespan)
                .map(requestedExpiration -> longLivedTokenAllowed ? requestedExpiration : min(furthestExpiration, requestedExpiration))
                .orElse(furthestExpiration);
    }
}
