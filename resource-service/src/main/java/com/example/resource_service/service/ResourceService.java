package com.example.resource_service.service;

import com.example.resource_service.dto.SongMetadataDto;
import com.example.resource_service.entity.Resource;
import com.example.resource_service.exception.InvalidResourceException;
import com.example.resource_service.exception.ResourceNotFoundException;
import com.example.resource_service.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.xml.sax.helpers.DefaultHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final RestTemplate restTemplate;
    @Value("${song.service.url:http://localhost:8081/songs}")
    private String songServiceUrl;

    @Transactional
    public Integer uploadResource(byte[] audioData) {
        Resource resource = new Resource();
        resource.setAudioData(audioData);
        resource = resourceRepository.save(resource);

        Metadata metadata = new Metadata();
        try (InputStream input = new ByteArrayInputStream(audioData)) {
            Mp3Parser parser = new Mp3Parser();
            parser.parse(input, new DefaultHandler(), metadata, new ParseContext());
        } catch (Exception e) {
            throw new InvalidResourceException("Invalid MP3 file");
        }

        String durationStr = metadata.get("xmpDM:duration");
        String formattedDuration = null;
        if (durationStr != null) {
            try {
                long ms = (long) Double.parseDouble(durationStr);
                long totalSeconds = ms / 1000;
                long minutes = totalSeconds / 60;
                long seconds = totalSeconds % 60;
                formattedDuration = String.format("%02d:%02d", minutes, seconds);
            } catch (NumberFormatException ignored) {}
        }

        String releaseDate = metadata.get("xmpDM:releaseDate");
        String year = (releaseDate != null && releaseDate.length() >= 4) ? releaseDate.substring(0, 4) : releaseDate;

        String name = metadata.get("dc:title") != null ? metadata.get("dc:title") : metadata.get("title");
        String artist = metadata.get("xmpDM:artist");
        String album = metadata.get("xmpDM:album");

        SongMetadataDto songDto = SongMetadataDto.builder()
                .id(resource.getId())
                .name(name)
                .artist(artist)
                .album(album)
                .duration(formattedDuration)
                .year(year)
                .build();
        try {
            restTemplate.postForObject(songServiceUrl, songDto, Object.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save metadata in Song Service. Check MP3 tags.");
        }

        return resource.getId();
    }

    public byte[] getResourceById(Integer id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Invalid value '" + id + "' for ID. Must be a positive integer"
            );        }

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource with ID=" + id + " not found"));
        return resource.getAudioData();
    }

    public List<Integer> deleteResources(String csvIds) {
        if (csvIds != null && csvIds.length() > 200) {
            throw new IllegalArgumentException(
                    "CSV string is too long: received " + csvIds.length() + " characters, maximum allowed is 200"
            );        }

        List<Integer> ids = new ArrayList<>();
        if (csvIds != null && !csvIds.isEmpty()) {
            for (String idStr : csvIds.split(",")) {
                idStr = idStr.trim();
                try {
                    int id = Integer.parseInt(idStr);

                    if (id <= 0) {
                        throw new IllegalArgumentException(
                                "Invalid ID format: '" + idStr + "'. Only positive integers are allowed"
                        );
                    }

                    ids.add(id);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Invalid ID format: '" + idStr + "'. Only positive integers are allowed"
                    );
                }
            }
        }

        List<Integer> deletedIds = ids.stream()
                .filter(resourceRepository::existsById)
                .peek(resourceRepository::deleteById)
                .collect(Collectors.toList());

        if (!deletedIds.isEmpty()) {
            String actuallyDeletedCsv = deletedIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            try {
                restTemplate.delete(songServiceUrl + "?id=" + actuallyDeletedCsv);
            } catch (Exception ignored) {}
        }

        return deletedIds;
    }
}