package com.zematix.jworldcup.backend.service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Service;

import com.zematix.jworldcup.backend.dto.ReCaptchaDto;
import com.zematix.jworldcup.backend.exception.GoogleException;

@Service
public class GoogleService extends ServiceBase {
	
	public static final String GOOGLE_RECAPTCHA_API_URI = "https://www.google.com/recaptcha/api";
	
	/**
	 * Service wrapper of the Google reCAPTCHA v2 siteverify method.
	 * @param secret
	 * @param response
	 * @param remoteip
	 * @return
	 * @throws GoogleException if there is problem with the WS call
	 */
	public ReCaptchaDto siteVerify(String secret, String response, String remoteip) throws GoogleException {
		ReCaptchaDto reCaptchaDto = null;
		
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(GOOGLE_RECAPTCHA_API_URI + "/siteverify")
				.queryParam("secret", secret)
				.queryParam("response", response)
				.queryParam("remoteip", remoteip);
		try {
			Builder builder = target.request(MediaType.APPLICATION_JSON);
			reCaptchaDto = builder.get(new GenericType<ReCaptchaDto>() {});
		}
		catch (Exception e) {
			throw new GoogleException(e.getMessage());
		}
		return reCaptchaDto;
	}
}
