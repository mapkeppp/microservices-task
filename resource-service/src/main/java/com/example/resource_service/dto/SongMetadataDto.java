package com.example.resource_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SongMetadataDto {
    private Integer id;
    private String name;
    private String artist;
    private String album;
    private String duration;
    private String year;
}