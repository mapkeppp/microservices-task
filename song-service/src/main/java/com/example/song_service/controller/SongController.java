package com.example.song_service.controller;

import com.example.song_service.dto.SongDto;
import com.example.song_service.service.SongService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @PostMapping
    public ResponseEntity<Map<String, Integer>> createSong(@Valid @RequestBody SongDto songDTO) {
        Integer id = songService.createSong(songDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongDto> getSong(@PathVariable Integer id) {
        return ResponseEntity.ok(songService.getSongById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SongDto> updateSong(@PathVariable Integer id,
                                              @Valid @RequestBody SongDto songDTO) {
        return ResponseEntity.ok(songService.updateSong(id, songDTO));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteSongs(@RequestParam("id") String csvIds) {
        songService.deleteSongs(csvIds);
        return ResponseEntity.noContent().build();
    }
}