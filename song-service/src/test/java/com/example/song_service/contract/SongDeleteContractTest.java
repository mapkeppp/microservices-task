package com.example.song_service.contract;

import com.example.song_service.controller.SongController;
import com.example.song_service.service.SongService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SongController.class)
class SongDeleteContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SongService songService;

    @Test
    void deleteSongsWithCsvIdsReturns204() throws Exception {
        when(songService.deleteSongs("1,2")).thenReturn(Arrays.asList(1, 2));

        mockMvc.perform(delete("/songs").param("id", "1,2"))
                .andExpect(status().isNoContent());
    }
}
