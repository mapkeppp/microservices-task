package com.example.resource_service.service;

import com.example.resource_service.entity.Resource;
import com.example.resource_service.exception.ResourceNotFoundException;
import com.example.resource_service.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final RestTemplate restTemplate;
    private final S3Client s3Client;

    @Value("${song.service.url:http://song-service/songs}")
    private String songServiceUrl;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Transactional
    public Integer uploadResource(byte[] audioData) {
        String s3Key = UUID.randomUUID().toString() + ".mp3";

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(audioData));

        Resource resource = new Resource();
        resource.setS3Key(s3Key);
        resource = resourceRepository.save(resource);

        return resource.getId();
    }

    public byte[] getResourceById(Integer id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Invalid value '" + id + "' for ID. Must be a positive integer"
            );
        }

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource with ID=" + id + " not found"));

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(resource.getS3Key())
                    .build();
            return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
        } catch (Exception e) {
            throw new ResourceNotFoundException("File not found in Cloud Storage for ID=" + id);
        }
    }

    public List<Integer> deleteResources(String csvIds) {
        if (csvIds != null && csvIds.length() > 200) {
            throw new IllegalArgumentException(
                    "CSV string is too long: received " + csvIds.length() + " characters, maximum allowed is 200"
            );
        }

        List<Integer> ids = new ArrayList<>();
        if (csvIds != null && !csvIds.isEmpty()) {
            for (String idStr : csvIds.split(",")) {
                idStr = idStr.trim();
                try {
                    int id = Integer.parseInt(idStr);
                    if (id <= 0) {
                        throw new IllegalArgumentException("Invalid ID format: '" + idStr + "'. Only positive integers are allowed");
                    }
                    ids.add(id);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid ID format: '" + idStr + "'. Only positive integers are allowed");
                }
            }
        }

        List<Integer> deletedIds = new ArrayList<>();

        for (Integer id : ids) {
            resourceRepository.findById(id).ifPresent(resource -> {
                try {
                    DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(resource.getS3Key())
                            .build();
                    s3Client.deleteObject(deleteObjectRequest);
                } catch (Exception ignored) {
                }

                // 2. Удаляем запись из базы данных Resource
                resourceRepository.deleteById(id);
                deletedIds.add(id);
            });
        }

        if (!deletedIds.isEmpty()) {
            String actuallyDeletedCsv = deletedIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            try {
                restTemplate.delete(songServiceUrl + "?id=" + actuallyDeletedCsv);
            } catch (Exception ignored) {
            }
        }

        return deletedIds;
    }
}