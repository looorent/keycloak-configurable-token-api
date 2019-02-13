package be.looorent;

import com.fasterxml.jackson.annotation.JsonProperty;

import static java.lang.Math.min;
import static java.util.Optional.ofNullable;
import static org.keycloak.common.util.Time.currentTime;

/**
 * @author Lorent Lempereur
 */
public class TokenConfiguration {

    @JsonProperty("tokenLifespanInSeconds")
    private Integer tokenLifespanInSeconds;

    public Integer getTokenLifespanInSeconds() {
        return tokenLifespanInSeconds;
    }

    public void setTokenLifespanInSeconds(Integer tokenLifespanInSeconds) {
        this.tokenLifespanInSeconds = tokenLifespanInSeconds;
    }

    public int computeTokenExpiration(int maxExpiration, boolean longLivedTokenAllowed) {
        return ofNullable(tokenLifespanInSeconds)
                .map(lifespan -> currentTime() + lifespan)
                .map(requestedExpiration -> longLivedTokenAllowed ? requestedExpiration : min(maxExpiration, requestedExpiration))
                .orElse(maxExpiration);
    }
}
