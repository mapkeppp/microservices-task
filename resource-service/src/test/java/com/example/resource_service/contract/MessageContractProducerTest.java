package com.example.resource_service.contract;

import com.example.resource_service.config.RabbitMQConfig;
import com.example.resource_service.entity.Resource;
import com.example.resource_service.repository.ResourceRepository;
import com.example.resource_service.service.ResourceService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.s3.S3Client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MessageContractProducerTest {

    @Test
    void uploadSendsResourceIdAsIntegerToQueue() {
        ResourceRepository repository = mock(ResourceRepository.class);
        S3Client s3Client = mock(S3Client.class);
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        ResourceService service = new ResourceService(repository, new RestTemplate(), s3Client, rabbitTemplate);
        ReflectionTestUtils.setField(service, "bucketName", "test-bucket");

        when(repository.save(any(Resource.class))).thenAnswer(inv -> {
            Resource r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 1);
            return r;
        });

        TransactionSynchronizationManager.initSynchronization();
        try {
            service.uploadResource(new byte[]{1, 2, 3});
            for (TransactionSynchronization s : TransactionSynchronizationManager.getSynchronizations()) {
                s.afterCommit();
            }
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        verify(rabbitTemplate).convertAndSend(RabbitMQConfig.QUEUE_NAME, (Object) 1);
    }
}
