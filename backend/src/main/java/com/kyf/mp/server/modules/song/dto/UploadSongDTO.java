package com.kyf.mp.server.modules.song.dto;

import org.springframework.web.multipart.MultipartFile;

import com.kyf.mp.server.common.file.UploadFileValidator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UploadSongDTO {
    @Schema(description = "音频文件", type = "string", format = "binary")
    @NotNull(message = "请上传音频文件")
    private MultipartFile audiofile;

    @Schema(description = "歌曲封面图片", type = "string", format = "binary")
    @NotNull(message = "请上传歌曲封面")
    private MultipartFile coverfile;

    @Pattern(regexp = "[^\\\\/:*?\"<>|\\p{Cntrl}]*", message = "歌曲名不能包含 / \\ : * ? \" < > | 或控制字符")
    private String title;
    private String artist;
    private String album;
    private String lyrics;

    @Schema(hidden = true)
    @AssertTrue(message = "音频文件名不能为空，且不能包含 / \\ : * ? \" < > | 或控制字符")
    public boolean isAudioFileNameValid() {
        return UploadFileValidator.isOriginalFileNameValid(audiofile);
    }

    @Schema(hidden = true)
    @AssertTrue(message = "封面文件名不能为空，且不能包含 / \\ : * ? \" < > | 或控制字符")
    public boolean isCoverFileNameValid() {
        return UploadFileValidator.isOriginalFileNameValid(coverfile);
    }
}
