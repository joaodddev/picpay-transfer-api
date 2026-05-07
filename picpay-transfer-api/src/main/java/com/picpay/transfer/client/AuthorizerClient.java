package com.picpay.transfer.client;

import com.picpay.transfer.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class AuthorizerClient {

    private final RestTemplate restTemplate;
    private final String authorizerUrl;

    public AuthorizerClient(RestTemplate restTemplate,
                            @Value("${picpay.authorizer.url}") String authorizerUrl) {
        this.restTemplate = restTemplate;
        this.authorizerUrl = authorizerUrl;
    }

    public void authorize() {
        try {
            var response = restTemplate.getForEntity(authorizerUrl, Map.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new BusinessException("Transferência não autorizada pelo serviço externo.", HttpStatus.UNPROCESSABLE_ENTITY);
            }

            Map<?, ?> body = response.getBody();
            if (body == null) {
                throw new BusinessException("Resposta inválida do autorizador.", HttpStatus.UNPROCESSABLE_ENTITY);
            }

            // O mock retorna { "status": "success", "data": { "authorization": true } }
            Map<?, ?> data = (Map<?, ?>) body.get("data");
            if (data == null || !Boolean.TRUE.equals(data.get("authorization"))) {
                throw new BusinessException("Transferência não autorizada.", HttpStatus.UNPROCESSABLE_ENTITY);
            }

        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("Serviço autorizador indisponível.", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
