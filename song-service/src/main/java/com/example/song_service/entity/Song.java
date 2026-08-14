package com.example.song_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "songs")
@Getter
@Setter
@NoArgsConstructor
public class Song {

    @Id
    private Integer id;

    private String name;
    private String artist;
    private String album;
    private String duration;
    private String year;

}