package com.example.resourceprocessor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceProcessingListener {

    private final ResourceProcessingService processingService;

    @RabbitListener(queues = "resource-processing-queue")
    public void handleMessage(Integer resourceId) {
        log.info("Received message for resourceId: {}", resourceId);
        processingService.processResource(resourceId);
    }
}