package com.zematix.jworldcup.backend.dto;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;


/**
 * The persistent class for the bet database table.
 * 
 */
@Data
public class ReCaptchaDto {

	@JsonProperty(required = true)
    @NotNull
    private Boolean success;

    @JsonProperty(value="challenge_ts", required = true)
    @NotNull
    private String challengeTs; // timestamp of the challenge load (ISO format yyyy-MM-dd'T'HH:mm:ssZZ)

    @JsonProperty(required = true)
    @NotNull
    private String hostname; // the hostname of the site where the reCAPTCHA was solved
    
    @JsonProperty(value="error-codes", required = true)
    private List<String> errorCodes; // optional
}