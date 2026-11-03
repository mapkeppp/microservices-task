package com.example.song_service.controller;

import com.example.song_service.dto.SongDto;
import com.example.song_service.service.SongService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@AutoConfigureMockMvc
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
    void shouldCreateSongAndReturn200() throws Exception {
        when(songService.createSong(any(SongDto.class))).thenReturn(1);

        mockMvc.perform(post("/songs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testSongDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
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
    void shouldDeleteSongsAndReturn200() throws Exception {
        when(songService.deleteSongs("1,2,3")).thenReturn(Arrays.asList(1, 2, 3));

        mockMvc.perform(delete("/songs?id=1,2,3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ids[0]").value(1))
                .andExpect(jsonPath("$.ids[1]").value(2))
                .andExpect(jsonPath("$.ids[2]").value(3));
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

    @Test
    void shouldDeleteSongsWithEmptyParams() throws Exception {
        when(songService.deleteSongs("")).thenReturn(Collections.emptyList());

        mockMvc.perform(delete("/songs?id="))
                .andExpect(status().isOk());
    }
}