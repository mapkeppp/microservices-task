package com.example.resource_service.controller;

import com.example.resource_service.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping(consumes = "audio/mpeg")
    public ResponseEntity<Map<String, Integer>> uploadResource(@RequestBody byte[] audioData) {
        Integer id = resourceService.uploadResource(audioData);
        return ResponseEntity.ok(Map.of("id", id));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<byte[]> getResource(@PathVariable Integer id) {
        byte[] audioData = resourceService.getResourceById(id);
        return ResponseEntity.ok()
                .header("Content-Type", "audio/mpeg")
                .body(audioData);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Integer>>> deleteResources(@RequestParam(value = "id") String csvIds) {
        List<Integer> deletedIds = resourceService.deleteResources(csvIds);
        return ResponseEntity.ok(Map.of("ids", deletedIds));
    }
}