package com.example.song_service.repository;

import com.example.song_service.entity.Song;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SongRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SongRepository songRepository;

    private Song testSong;

    @BeforeEach
    void setUp() {
        testSong = new Song();
        testSong.setId(100);
        testSong.setName("Integration Test Song");
        testSong.setArtist("Integration Artist");
        testSong.setAlbum("Integration Album");
        testSong.setDuration("03:45");
        testSong.setYear("2020");
    }

    @Test
    void shouldSaveAndRetrieveSong() {
        Song saved = songRepository.save(testSong);
        entityManager.flush();
        entityManager.clear();

        Song retrieved = songRepository.findById(saved.getId()).orElse(null);

        assertNotNull(retrieved);
        assertEquals("Integration Test Song", retrieved.getName());
        assertEquals("Integration Artist", retrieved.getArtist());
        assertEquals("03:45", retrieved.getDuration());
    }

    @Test
    void shouldCheckIfSongExists() {
        songRepository.save(testSong);
        entityManager.flush();

        boolean exists = songRepository.existsById(100);
        boolean notExists = songRepository.existsById(999);

        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    void shouldUpdateSong() {
        Song saved = songRepository.save(testSong);
        entityManager.flush();

        saved.setName("Updated Name");
        saved.setArtist("Updated Artist");
        songRepository.save(saved);
        entityManager.flush();
        entityManager.clear();

        Song updated = songRepository.findById(saved.getId()).orElse(null);

        assertNotNull(updated);
        assertEquals("Updated Name", updated.getName());
        assertEquals("Updated Artist", updated.getArtist());
    }

    @Test
    void shouldDeleteSong() {
        Song saved = songRepository.save(testSong);
        Integer songId = saved.getId();
        entityManager.flush();

        songRepository.deleteById(songId);
        entityManager.flush();

        assertFalse(songRepository.existsById(songId));
    }

    @Test
    void shouldCountAllSongs() {
        songRepository.save(testSong);

        Song song2 = new Song();
        song2.setId(101);
        song2.setName("Another Song");
        song2.setArtist("Another Artist");
        song2.setAlbum("Another Album");
        song2.setDuration("04:20");
        song2.setYear("2021");
        songRepository.save(song2);

        entityManager.flush();

        long count = songRepository.count();

        assertTrue(count >= 2);
    }
}