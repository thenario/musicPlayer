package com.kyf.mp.server.modules.song.dto;

import org.springframework.web.multipart.MultipartFile;

import com.kyf.mp.server.common.file.UploadFileValidator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EditSongDTO {
    @Pattern(regexp = "[^\\\\/:*?\"<>|\\p{Cntrl}]*", message = "歌曲名不能包含 / \\ : * ? \" < > | 或控制字符")
    private String song_name;
    private String artist;
    private String album;
    private String lyrics;
    private String t_lyrics;
    private MultipartFile song_cover;

    @Schema(hidden = true)
    @AssertTrue(message = "封面文件名不能为空，且不能包含 / \\ : * ? \" < > | 或控制字符")
    public boolean isCoverFileNameValid() {
        return UploadFileValidator.isOriginalFileNameValid(song_cover);
    }
}
