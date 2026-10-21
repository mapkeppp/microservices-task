package com.example.resourceprocessor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceProcessingService {

    private final Mp3MetadataExtractor metadataExtractor;
    private final RestTemplate restTemplate;

    @Value("${resource.service.url}")
    private String resourceServiceUrl;

    @Value("${song.service.url}")
    private String songServiceUrl;

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void processResource(Integer resourceId) {
        log.info("Processing resource: {}", resourceId);

        // 1. GET binary from Resource Service
        byte[] mp3Data = restTemplate.getForObject(
                resourceServiceUrl + "/resources/" + resourceId, byte[].class);

        if (mp3Data == null) {
            throw new RuntimeException("Failed to get resource data for ID: " + resourceId);
        }

        // 2. Extract metadata
        Map<String, String> metadata = metadataExtractor.extract(mp3Data);
        log.info("Extracted metadata: {}", metadata);

        // 3. Convert duration from seconds to mm:ss
        String duration = formatDuration(metadata.get("length"));

        // 4. POST metadata to Song Service
        Map<String, Object> songDto = Map.of(
                "id", resourceId,
                "name", defaultIfNull(metadata.get("name"), "Unknown"),
                "artist", defaultIfNull(metadata.get("artist"), "Unknown"),
                "album", defaultIfNull(metadata.get("album"), "Unknown"),
                "duration", duration,
                "year", formatYear(metadata.get("year"))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(songDto, headers);

        restTemplate.postForObject(songServiceUrl + "/songs", request, String.class);
        log.info("Song metadata saved for resourceId: {}", resourceId);
    }

    private String formatDuration(String durationInSeconds) {
        try {
            double seconds = Double.parseDouble(durationInSeconds);
            int totalSeconds = (int) Math.round(seconds);
            int minutes = totalSeconds / 60;
            int secs = totalSeconds % 60;
            return String.format("%02d:%02d", minutes, secs);
        } catch (Exception e) {
            return "00:00";
        }
    }

    private String formatYear(String year) {
        if (year != null && year.length() >= 4) {
            return year.substring(0, 4);
        }
        return "2000";
    }

    private String defaultIfNull(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }
}