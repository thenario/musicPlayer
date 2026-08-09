package com.kyf.mp.javaserver.modules.SongModule.DTO;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class EDitSongDTO {
    private String song_name;
    private String lyrics;
    private String t_lyrics;
    private MultipartFile song_cover;
}
