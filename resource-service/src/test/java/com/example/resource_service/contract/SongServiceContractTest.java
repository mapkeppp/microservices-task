package com.example.resource_service.contract;

import com.example.resource_service.entity.Resource;
import com.example.resource_service.repository.ResourceRepository;
import com.example.resource_service.service.ResourceService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;

class SongServiceContractTest {

    @Test
    void deleteResourcesSendsDeleteRequestToSongService() {
        ResourceRepository repository = mock(ResourceRepository.class);
        S3Client s3Client = mock(S3Client.class);
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        Resource first = new Resource();
        first.setId(1);
        first.setS3Key("key-1.mp3");

        Resource second = new Resource();
        second.setId(2);
        second.setS3Key("key-2.mp3");

        when(repository.findById(1)).thenReturn(Optional.of(first));
        when(repository.findById(2)).thenReturn(Optional.of(second));

        server.expect(requestTo("http://song-service/songs?id=1,2"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withNoContent());

        ResourceService service = new ResourceService(repository, restTemplate, s3Client, rabbitTemplate);
        ReflectionTestUtils.setField(service, "songServiceUrl", "http://song-service/songs");
        ReflectionTestUtils.setField(service, "bucketName", "test-bucket");

        List<Integer> deleted = service.deleteResources("1,2");

        assertEquals(List.of(1, 2), deleted);
        server.verify();
    }
}
