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

/**
 * Web service client implementation to get online results of football matches 
 * from <a href="https://www.openligadb.de">OpenLigaDB</a>. In the beginning
 * OpenLigaDB had supported SOAP methods only, but later it was expanded to REST ones.
 * Because technically both are supported yet, JWorldcup app still calls old SOAP methods 
 * but also new REST ones. The latter ones are those which were not implemented 
 * at OpenLigaDB/SOAP, they can be called only by REST interface.
 * Note: in fact all SOAP calls should be transferred to REST ones later, but 
 * OpenLigaDB REST interface is not so well documented, there is no Swagger support 
 * either.
 */
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
