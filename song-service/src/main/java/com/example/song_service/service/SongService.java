package com.example.song_service.service;

import com.example.song_service.dto.SongDto;
import com.example.song_service.entity.Song;
import com.example.song_service.exception.SongAlreadyExistsException;
import com.example.song_service.exception.SongNotFoundException;
import com.example.song_service.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;

    public Integer createSong(SongDto songDTO) {
        if (songRepository.existsById(songDTO.getId())) {
            throw new SongAlreadyExistsException(
                    "Metadata for resource ID=" + songDTO.getId() + " already exists"
            );
        }

        Song song = new Song();
        song.setId(songDTO.getId());
        song.setName(songDTO.getName());
        song.setArtist(songDTO.getArtist());
        song.setAlbum(songDTO.getAlbum());
        song.setDuration(songDTO.getDuration());
        song.setYear(songDTO.getYear());

        return songRepository.save(song).getId();
    }

    public SongDto getSongById(Integer id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Invalid value '" + id + "' for ID. Must be a positive integer"
            );
        }

        Song song = songRepository.findById(id)
                .orElseThrow(() -> new SongNotFoundException(
                        "Song metadata for ID=" + id + " not found"
                ));

        SongDto dto = new SongDto();
        dto.setId(song.getId());
        dto.setName(song.getName());
        dto.setArtist(song.getArtist());
        dto.setAlbum(song.getAlbum());
        dto.setDuration(song.getDuration());
        dto.setYear(song.getYear());

        return dto;
    }

    public List<Integer> deleteSongs(String csvIds) {
        if (csvIds != null && csvIds.length() > 200) {
            throw new IllegalArgumentException(
                    "CSV string is too long: received " + csvIds.length()
                            + " characters, maximum allowed is 200"
            );
        }

        List<Integer> ids = new java.util.ArrayList<>();

        if (csvIds != null && !csvIds.isEmpty()) {
            for (String idStr : csvIds.split(",")) {
                idStr = idStr.trim();

                try {
                    int id = Integer.parseInt(idStr);

                    if (id <= 0) {
                        throw new IllegalArgumentException(
                                "Invalid ID format: '" + idStr
                                        + "'. Only positive integers are allowed"
                        );
                    }

                    ids.add(id);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Invalid ID format: '" + idStr
                                    + "'. Only positive integers are allowed"
                    );
                }
            }
        }

        return ids.stream()
                .filter(songRepository::existsById)
                .peek(songRepository::deleteById)
                .collect(java.util.stream.Collectors.toList());
    }
}