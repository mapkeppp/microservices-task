    package com.example.song_service.entity;

    import javax.persistence.Entity;
    import javax.persistence.Table;
    import javax.persistence.Id;
    import javax.persistence.GeneratedValue;
    import javax.persistence.GenerationType;
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