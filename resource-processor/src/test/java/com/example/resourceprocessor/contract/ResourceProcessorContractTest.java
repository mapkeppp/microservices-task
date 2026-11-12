package com.example.resourceprocessor.contract;

import com.example.resourceprocessor.service.Mp3MetadataExtractor;
import com.example.resourceprocessor.service.ResourceProcessingListener;
import com.example.resourceprocessor.service.ResourceProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class ResourceProcessorContractTest {

    @Test
    void listenerPassesResourceIdToProcessing() {
        ResourceProcessingService processingService = mock(ResourceProcessingService.class);
        ResourceProcessingListener listener = new ResourceProcessingListener(processingService);

        listener.handleMessage(1);

        verify(processingService).processResource(1);
    }

    @Test
    void processResourceFetchesMp3AndPostsMetadataToSongService() {
        Mp3MetadataExtractor extractor = mock(Mp3MetadataExtractor.class);
        when(extractor.extract(any(byte[].class))).thenReturn(Map.of(
                "name", "Bohemian Rhapsody",
                "artist", "Queen",
                "album", "A Night at the Opera",
                "length", "355",
                "year", "1975-10-31"));

        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(requestTo("http://resource-service/resources/1"))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andRespond(withSuccess(new byte[]{1, 2, 3}, MediaType.APPLICATION_OCTET_STREAM));

        server.expect(requestTo("http://song-service/songs"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Bohemian Rhapsody"))
                .andExpect(jsonPath("$.duration").value("05:55"))
                .andExpect(jsonPath("$.year").value("1975"))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"id\":1}"));

        ResourceProcessingService service = new ResourceProcessingService(extractor, restTemplate);
        ReflectionTestUtils.setField(service, "resourceServiceUrl", "http://resource-service");
        ReflectionTestUtils.setField(service, "songServiceUrl", "http://song-service");

        service.processResource(1);

        server.verify();
    }
}