package com.example.song_service.controller;

import com.example.song_service.dto.SongDto;
import com.example.song_service.exception.SongAlreadyExistsException;
import com.example.song_service.exception.SongNotFoundException;
import com.example.song_service.service.SongService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SongController.class)
class SongControllerComponentTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SongService songService;

    @Autowired
    private ObjectMapper objectMapper;

    private SongDto testSongDto;

    @BeforeEach
    void setUp() {
        testSongDto = new SongDto();
        testSongDto.setId(1);
        testSongDto.setName("Test Song");
        testSongDto.setArtist("Test Artist");
        testSongDto.setAlbum("Test Album");
        testSongDto.setDuration("03:30");
        testSongDto.setYear("2023");
    }

    @Test
    void shouldCreateSongAndReturn201() throws Exception {
        when(songService.createSong(any(SongDto.class))).thenReturn(1);

        mockMvc.perform(post("/songs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testSongDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturn409WhenSongAlreadyExists() throws Exception {
        when(songService.createSong(any(SongDto.class)))
                .thenThrow(new SongAlreadyExistsException("exists"));

        mockMvc.perform(post("/songs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testSongDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldGetSongByIdAndReturn200() throws Exception {
        when(songService.getSongById(1)).thenReturn(testSongDto);

        mockMvc.perform(get("/songs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Song"))
                .andExpect(jsonPath("$.artist").value("Test Artist"));
    }

    @Test
    void shouldReturn404WhenSongNotFound() throws Exception {
        when(songService.getSongById(999)).thenThrow(new SongNotFoundException("not found"));

        mockMvc.perform(get("/songs/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateSongAndReturn200() throws Exception {
        when(songService.updateSong(eq(1), any(SongDto.class))).thenReturn(testSongDto);

        mockMvc.perform(put("/songs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testSongDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Song"));
    }

    @Test
    void shouldReturn404WhenUpdatingMissingSong() throws Exception {
        when(songService.updateSong(eq(999), any(SongDto.class)))
                .thenThrow(new SongNotFoundException("not found"));

        mockMvc.perform(put("/songs/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testSongDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteSongsAndReturn204() throws Exception {
        when(songService.deleteSongs("1,2,3")).thenReturn(Arrays.asList(1, 2, 3));

        mockMvc.perform(delete("/songs").param("id", "1,2,3"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldDeleteSongsWithEmptyParamsAndReturn204() throws Exception {
        when(songService.deleteSongs("")).thenReturn(Collections.emptyList());

        mockMvc.perform(delete("/songs").param("id", ""))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn400WhenDeleteParamIsMissing() throws Exception {
        mockMvc.perform(delete("/songs"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenPostingWithoutId() throws Exception {
        SongDto invalidSong = new SongDto();
        invalidSong.setName("Song");
        invalidSong.setArtist("Artist");

        mockMvc.perform(post("/songs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidSong)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenPostingWithoutName() throws Exception {
        SongDto invalidSong = new SongDto();
        invalidSong.setId(1);
        invalidSong.setArtist("Artist");

        mockMvc.perform(post("/songs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidSong)))
                .andExpect(status().isBadRequest());
    }
}