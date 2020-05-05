package com.ayriaplatform.apg;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

import junit.framework.TestCase;

/**
 * Unit tests for APG SDK.
 */
public class ApgSdkTest extends TestCase {

    private final ApgSdk sdk = new ApgSdk(100120, false);

    private final static String payerMobile = "09120000000";
    public static String latestReferenceCode;

    public void test001Properties() {
        assertNotNull("Referral code should not be null.", sdk.getReferralCode());
        assertNotNull("APG endpoint should not be null.", sdk.apgEndpoint());
    }

    public void test002CreateInvalidCommand() {
        try {
            sdk.createPayment(new AyriaPaymentV1Command());
            fail("We should get BAD_REQUEST");
        } catch (HttpClientErrorException e) {
            assertTrue("Error code must be 400", e.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        }
    }

    public void test003CreateValidCommand() {
        final AyriaPaymentV1Command cmd = new AyriaPaymentV1Command(sdk.getReferralCode(), new BigDecimal("1000"), payerMobile);
        final ResponseEntity<String> res = sdk.createPayment(cmd);
        assertTrue("Status should be 201", res.getStatusCode().equals(HttpStatus.CREATED));
        latestReferenceCode = res.getBody();
        assertTrue("Reference code should not be empty", StringUtils.hasText(latestReferenceCode));
    }

    public void test004CreateValidCommandFull() {
        final AyriaPaymentV1Command cmd = new AyriaPaymentV1Command(sdk.getReferralCode(), new BigDecimal("1000"), payerMobile);
        cmd.setDescription("تراکنش تستی");
        cmd.setPayerName("رستم دستان");
        cmd.setPaymentNumber("123456");
        cmd.setExtraData("{\"product\": \"clock\"}");
        final ResponseEntity<String> res = sdk.createPayment(cmd);
        assertTrue("Status should be 201", res.getStatusCode().equals(HttpStatus.CREATED));
        latestReferenceCode = res.getBody();
        assertTrue("Reference code should not be empty", StringUtils.hasText(latestReferenceCode));
    }

    public void test005GetPayment() {
        final ResponseEntity<AyriaPaymentV1DTO> res = sdk.getPayment(latestReferenceCode);
        assertTrue("Status should be 200", res.getStatusCode().equals(HttpStatus.OK));
        final AyriaPaymentV1DTO dto = res.getBody();
        assertEquals(latestReferenceCode, dto.getReferenceCode());
        assertEquals(new BigDecimal("1000"), dto.getAmount());
        assertEquals(sdk.getReferralCode(), dto.getReferralCode());
        assertEquals(payerMobile, dto.getPayerMobile());
        assertEquals("تراکنش تستی", dto.getDescription());
        assertEquals("رستم دستان", dto.getPayerName());
        assertEquals("123456", dto.getPaymentNumber());
        assertEquals("{\"product\": \"clock\"}", dto.getExtraData());
     // assertEquals("آیریا", dto.getPayeeName());  // Expected value depends to App#referralCode()
        assertFalse(dto.isPaid());
        assertFalse(dto.isCanceled());
        System.out.println(String.format("Payment created at %s, payment url is: %s", dto.getCreatedDate(), dto.getPaymentUrl()));
    }

    public void test006CancelPayment() {
        final AyriaPaymentV1CancelCommand cmd = new AyriaPaymentV1CancelCommand(latestReferenceCode, "قیمت اشتباه");
        final ResponseEntity<AyriaPaymentV1DTO> res = sdk.cancelPayment(cmd);
        assertTrue("Status should be 200", res.getStatusCode().equals(HttpStatus.OK));
        final AyriaPaymentV1DTO dto = res.getBody();
        assertTrue(dto.isCanceled());
        assertEquals("قیمت اشتباه", dto.getCancelDescription());
    }

    public void test007CancelPaymentInvalid() {
        final AyriaPaymentV1CancelCommand cmd = new AyriaPaymentV1CancelCommand(latestReferenceCode, "قیمت اشتباه");
        try {
            sdk.cancelPayment(cmd);
            fail("Already canceled payment can't be canceled.");
        } catch (HttpClientErrorException e) {
            assertTrue("Status should be 400", e.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        }
    }

}