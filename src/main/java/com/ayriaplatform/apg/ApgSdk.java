package com.ayriaplatform.apg;

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

    public ApgSdk(Integer referralCode, boolean isProduction) {
        this.referralCode = Long.valueOf(referralCode);
        this.isProduction = isProduction;
        restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());
    }

    public ResponseEntity<String> createPayment(AyriaPaymentV1Command cmd) {
        return restTemplate.postForEntity(f("%s/create", apgEndpoint()), cmd, String.class);
    }

    public ResponseEntity<AyriaPaymentV1DTO> getPayment(String referenceCode) {
        return restTemplate.getForEntity(f("%s/get/%s", apgEndpoint(), referenceCode), AyriaPaymentV1DTO.class);
    }

    public ResponseEntity<AyriaPaymentV1DTO> cancelPayment(AyriaPaymentV1CancelCommand cmd) {
        return restTemplate.postForEntity(f("%s/cancel", apgEndpoint()), cmd, AyriaPaymentV1DTO.class);
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

}
