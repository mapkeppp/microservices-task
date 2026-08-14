package com.example.song_service.controller;

import com.example.song_service.dto.SongDto;
import com.example.song_service.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @PostMapping
    public ResponseEntity<Map<String, Integer>> createSong(@Valid @RequestBody SongDto songDTO) {
        Integer id = songService.createSong(songDTO);
        return ResponseEntity.ok(Map.of("id", id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongDto> getSong(@PathVariable Integer id) {
        SongDto songDTO = songService.getSongById(id);
        return ResponseEntity.ok(songDTO);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Integer>>> deleteSongs(@RequestParam(value = "id") String csvIds) {
        List<Integer> deletedIds = songService.deleteSongs(csvIds);
        return ResponseEntity.ok(Map.of("ids", deletedIds));
    }
}