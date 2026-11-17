package com.example.apigateway.error;

import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

@Component
@Order(-2)
public class GatewayErrorHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatusCode status = resolveStatus(ex);

        String message = switch (status.value()) {
            case 404 -> "Requested resource was not found";
            case 503 -> "Service is temporarily unavailable. Please try again later";
            case 504 -> "Service did not respond in time. Please try again later";
            default -> "Unexpected error in API gateway";
        };

        String body = "{\"errorMessage\":\"" + message + "\",\"errorCode\":\"" + status.value() + "\"}";
        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private HttpStatusCode resolveStatus(Throwable ex) {
        for (Throwable current = ex; current != null; current = current.getCause()) {
            if (current instanceof ResponseStatusException rse) {
                return rse.getStatusCode();
            }
            if (current instanceof ConnectException) {
                return HttpStatus.SERVICE_UNAVAILABLE;
            }
            if (current instanceof TimeoutException) {
                return HttpStatus.GATEWAY_TIMEOUT;
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}