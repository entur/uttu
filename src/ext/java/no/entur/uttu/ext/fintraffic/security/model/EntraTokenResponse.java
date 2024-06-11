package no.entur.uttu.ext.fintraffic.security.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class EntraTokenResponse {

  private final String tokenType;

  private final LocalDateTime validUntil;

  private final String accessToken;

  @JsonCreator
  public EntraTokenResponse(
    @JsonProperty("token_type") String tokenType,
    @JsonProperty("expires_in") long expiresIn,
    @JsonProperty("access_token") String accessToken
  ) {
    this.tokenType = tokenType;
    this.validUntil = LocalDateTime.now().plusSeconds(expiresIn);
    this.accessToken = accessToken;
  }

  public String getTokenType() {
    return tokenType;
  }

  public LocalDateTime getValidUntil() {
    return validUntil;
  }

  public String getAccessToken() {
    return accessToken;
  }
}
