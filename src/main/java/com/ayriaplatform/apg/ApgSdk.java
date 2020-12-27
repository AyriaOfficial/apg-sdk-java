package com.ayriaplatform.apg;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * APG(Ayria payment gateway) SDK.
 *
 */
public class ApgSdk {

    private final RestTemplate restTemplate;
    private final Long referralCode;
    private final boolean isProduction;
    private final String apiKey;
    private final Long walletIdentity;

    public ApgSdk(String walletIdentity, String apiKey, boolean isProduction) {
        this.walletIdentity = Long.valueOf(walletIdentity);
        this.referralCode = Long.valueOf(walletIdentity.substring(0, walletIdentity.length() - 2));
        this.isProduction = isProduction;
        this.apiKey = apiKey;
        restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());
    }

    public ResponseEntity<AyriaPaymentV1DTO> createPayment(AyriaPaymentV1Command cmd) {
        return restTemplate.exchange(f("%s/create", apgEndpoint()), HttpMethod.POST, new HttpEntity<AyriaPaymentV1Command>(cmd, headers()), AyriaPaymentV1DTO.class);
    }

    public ResponseEntity<AyriaPaymentV1DTO> getPayment(String referenceCode) {
        return restTemplate.exchange(f("%s/get/%s", apgEndpoint(), referenceCode), HttpMethod.GET, new HttpEntity<Void>(headers()), AyriaPaymentV1DTO.class);
    }

    public ResponseEntity<AyriaPaymentV1DTO> cancelPayment(AyriaPaymentV1CancelCommand cmd) {
        return restTemplate.exchange(f("%s/cancel", apgEndpoint()), HttpMethod.POST, new HttpEntity<AyriaPaymentV1CancelCommand>(cmd, headers()), AyriaPaymentV1DTO.class);
    }

    public boolean isProduction() {
        return isProduction;
    }

    public Long getReferralCode() {
        return referralCode;
    }

    protected String apgEndpoint() {
        return isProduction ? Constants.APG_API_URL : Constants.APG_DEV_URL;
    }

    private String f(String format, Object... args) {
        return String.format(format, args);
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(Constants.APG_API_KEY, apiKey);
        headers.add(Constants.API_WALLET_ID, walletIdentity.toString());
        return headers;
    }

}
