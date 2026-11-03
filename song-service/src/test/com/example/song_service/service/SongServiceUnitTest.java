package com.example.song_service.service;

import com.example.song_service.dto.SongDto;
import com.example.song_service.entity.Song;
import com.example.song_service.exception.SongAlreadyExistsException;
import com.example.song_service.exception.SongNotFoundException;
import com.example.song_service.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SongServiceUnitTest {

    @Mock
    private SongRepository songRepository;

    @InjectMocks
    private SongService songService;

    private SongDto testSongDto;
    private Song testSong;

    @BeforeEach
    void setUp() {
        testSongDto = new SongDto();
        testSongDto.setId(1);
        testSongDto.setName("Bohemian Rhapsody");
        testSongDto.setArtist("Queen");
        testSongDto.setAlbum("A Night at the Opera");
        testSongDto.setDuration("05:55");
        testSongDto.setYear("1975");

        testSong = new Song();
        testSong.setId(1);
        testSong.setName("Bohemian Rhapsody");
        testSong.setArtist("Queen");
        testSong.setAlbum("A Night at the Opera");
        testSong.setDuration("05:55");
        testSong.setYear("1975");
    }

    @Test
    void shouldCreateSongSuccessfully() {
        when(songRepository.existsById(1)).thenReturn(false);
        when(songRepository.save(any(Song.class))).thenReturn(testSong);

        Integer result = songService.createSong(testSongDto);

        assertNotNull(result);
        assertEquals(1, result);
        verify(songRepository, times(1)).save(any(Song.class));
    }

    @Test
    void shouldThrowExceptionWhenSongAlreadyExists() {
        when(songRepository.existsById(1)).thenReturn(true);

        assertThrows(SongAlreadyExistsException.class,
                () -> songService.createSong(testSongDto));

        verify(songRepository, never()).save(any(Song.class));
    }

    @Test
    void shouldGetSongByIdSuccessfully() {
        when(songRepository.findById(1)).thenReturn(Optional.of(testSong));

        SongDto result = songService.getSongById(1);

        assertNotNull(result);
        assertEquals("Bohemian Rhapsody", result.getName());
        assertEquals("Queen", result.getArtist());
    }

    @Test
    void shouldThrowExceptionWhenSongNotFound() {
        when(songRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(SongNotFoundException.class,
                () -> songService.getSongById(999));
    }

    @Test
    void shouldThrowExceptionWhenIdIsInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> songService.getSongById(-1));

        assertThrows(IllegalArgumentException.class,
                () -> songService.getSongById(0));
    }

    @Test
    void shouldDeleteSongsSuccessfully() {
        when(songRepository.existsById(1)).thenReturn(true);
        when(songRepository.existsById(2)).thenReturn(true);

        var result = songService.deleteSongs("1,2");

        assertEquals(2, result.size());
        assertTrue(result.contains(1));
        assertTrue(result.contains(2));
        verify(songRepository, times(2)).deleteById(anyInt());
    }

    @Test
    void shouldDeleteEmptyListWhenNonExistentIds() {
        when(songRepository.existsById(999)).thenReturn(false);

        var result = songService.deleteSongs("999");

        assertTrue(result.isEmpty());
        verify(songRepository, never()).deleteById(anyInt());
    }

    @Test
    void shouldThrowExceptionWhenCsvTooLong() {
        String longCsv = "1,".repeat(150);

        assertThrows(IllegalArgumentException.class,
                () -> songService.deleteSongs(longCsv));
    }

    @Test
    void shouldThrowExceptionWhenInvalidIdFormat() {
        assertThrows(IllegalArgumentException.class,
                () -> songService.deleteSongs("abc,def"));
    }

    @Test
    void shouldThrowExceptionWhenNegativeIdInCsv() {
        assertThrows(IllegalArgumentException.class,
                () -> songService.deleteSongs("1,-2,3"));
    }

    @Test
    void shouldHandleEmptyDeleteRequest() {
        var result = songService.deleteSongs("");

        assertTrue(result.isEmpty());
        verify(songRepository, never()).deleteById(anyInt());
    }

    @Test
    void shouldHandleNullDeleteRequest() {
        var result = songService.deleteSongs(null);

        assertTrue(result.isEmpty());
        verify(songRepository, never()).deleteById(anyInt());
    }
}