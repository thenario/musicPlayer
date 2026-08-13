package com.kyf.mp.server.modules.song.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class EditSongDTO {
    private String song_name;
    private String lyrics;
    private String t_lyrics;
    private MultipartFile song_cover;
}
