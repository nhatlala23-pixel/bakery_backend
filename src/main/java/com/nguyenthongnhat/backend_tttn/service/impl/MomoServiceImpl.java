package com.nguyenthongnhat.backend_tttn.service.impl;

import com.nguyenthongnhat.backend_tttn.service.MomoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomoServiceImpl implements MomoService {

    private final RestTemplate restTemplate;

    @Value("${tttn.momo.partnerCode}")
    private String partnerCode;

    @Value("${tttn.momo.accessKey}")
    private String accessKey;

    @Value("${tttn.momo.secretKey}")
    private String secretKey;

    @Value("${tttn.momo.apiUrl}")
    private String apiUrl;

    @Value("${tttn.momo.returnUrl}")
    private String returnUrl;

    @Value("${tttn.momo.notifyUrl}")
    private String notifyUrl;

    @Override
    public String createPaymentUrl(String orderId, String orderInfo, BigDecimal amount) {
        String requestId = orderId; 
        String requestType = "payWithMethod";
        String extraData = "";
        long amountLong = amount.setScale(0, java.math.RoundingMode.HALF_UP).longValue();
        
        log.info(">>> [MOMO SERVICE] Preparing request for Order: {}", orderId);
        log.info(">>> [MOMO SERVICE] Exact Amount being sent: {}", amountLong);

        // Use TreeMap to automatically guarantee alphabetical order for signature fields
        java.util.Map<String, String> rawData = new java.util.TreeMap<>();
        rawData.put("accessKey", accessKey);
        rawData.put("amount", String.valueOf(amountLong));
        rawData.put("extraData", extraData);
        rawData.put("ipnUrl", notifyUrl);
        rawData.put("orderId", orderId);
        rawData.put("orderInfo", orderInfo);
        rawData.put("partnerCode", partnerCode);
        rawData.put("redirectUrl", returnUrl);
        rawData.put("requestId", requestId);
        rawData.put("requestType", requestType);

        String rawHash = rawData.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(java.util.stream.Collectors.joining("&"));

        String signature = hmacSha256(rawHash, secretKey);

        log.debug("--- DEBUG MOMO ---");
        log.debug("RawHash: {}", rawHash);
        log.debug("Signature: {}", signature);

        Map<String, Object> body = new HashMap<>();
        body.put("partnerCode", partnerCode);
        body.put("requestId", requestId);
        body.put("amount", amountLong);
        body.put("orderId", orderId);
        body.put("orderInfo", orderInfo);
        body.put("redirectUrl", returnUrl);
        body.put("ipnUrl", notifyUrl);
        body.put("extraData", extraData);
        body.put("requestType", requestType);
        body.put("signature", signature);
        body.put("autoCapture", true);
        body.put("lang", "vi");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(apiUrl, entity, Map.class);
            if (response != null && response.containsKey("payUrl")) {
                return (String) response.get("payUrl");
            }
            log.error("MOMO RESPONSE: {}", response);
            throw new RuntimeException("MoMo không trả về payUrl. Phản hồi: " + response);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            String errorBody = e.getResponseBodyAsString();
            log.error("MOMO API ERROR: {}", errorBody, e);
            throw new RuntimeException("Lỗi từ MoMo: " + errorBody, e);
        } catch (Exception e) {
            log.error("MOMO API UNKNOWN ERROR: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi không xác định khi gọi MoMo", e);
        }
    }

    private String hmacSha256(String data, String key) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder(rawHmac.length * 2);
            for (byte b : rawHmac) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo chữ ký HMAC SHA256", e);
        }
    }
}
