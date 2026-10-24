package com.kyf.mp.server.modules.song.controller;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.kyf.mp.server.common.GlobalExceptionHandler;
import com.kyf.mp.server.modules.song.service.SongsService;

class SongsUploadValidationTest {
    private SongsService songsService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        songsService = mock(SongsService.class);
        mvc = MockMvcBuilders.standaloneSetup(new SongsController(songsService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"bad/name.mp3", "bad\\name.mp3", "bad:name.mp3", "bad*name.mp3",
            "bad?name.mp3", "bad\"name.mp3", "bad<name.mp3", "bad>name.mp3", "bad|name.mp3",
            "bad\nname.mp3", "bad\u0000name.mp3", " "})
    void rejectsInvalidAudioNameBeforeCallingService(String name) throws Exception {
        mvc.perform(multipart("/api/songs")
                .file(file("audiofile", name, "audio/mpeg"))
                .file(file("coverfile", "cover.png", "image/png"))
                .requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(org.hamcrest.Matchers.containsString("音频文件名")));

        verifyNoInteractions(songsService);
    }

    @Test
    void rejectsInvalidCoverNameOnUpload() throws Exception {
        mvc.perform(multipart("/api/songs")
                .file(file("audiofile", "song.mp3", "audio/mpeg"))
                .file(file("coverfile", "cover?.png", "image/png"))
                .requestAttr("userId", 1L))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(songsService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"晴天.mp3", "24⧸7.mp3", "song #1 + 50%.mp3"})
    void acceptsLegalNamesAndPreservesExistingMultipartFields(String name) throws Exception {
        MockMultipartFile audio = file("audiofile", name, "audio/mpeg");
        MockMultipartFile cover = file("coverfile", "封面.png", "image/png");
        mvc.perform(multipart("/api/songs").file(audio).file(cover)
                .param("title", "歌曲元信息")
                .param("artist", "歌手")
                .param("album", "专辑")
                .param("lyrics", "歌词")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk());

        verify(songsService).uploadSong(audio, cover, 1L, "歌曲元信息", "歌手", "专辑", "歌词");
    }

    @ParameterizedTest
    @ValueSource(strings = {"歌曲/名称", "歌曲\\名称", "歌曲:名称", "歌曲*名称", "歌曲?名称",
            "歌曲\"名称", "歌曲<名称", "歌曲>名称", "歌曲|名称", "歌曲\n名称", "歌曲\u0000名称"})
    void rejectsInvalidTitleOnUploadAndEdit(String title) throws Exception {
        mvc.perform(multipart("/api/songs")
                .file(file("audiofile", "song.mp3", "audio/mpeg"))
                .file(file("coverfile", "cover.png", "image/png"))
                .param("title", title)
                .requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(org.hamcrest.Matchers.containsString("歌曲名")));

        mvc.perform(multipart(HttpMethod.PATCH, "/api/songs/my-uploads/2")
                .param("song_name", title)
                .requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value(org.hamcrest.Matchers.containsString("歌曲名")));

        verifyNoInteractions(songsService);
    }

    @Test
    void allowsOmittingOptionalTitle() throws Exception {
        MockMultipartFile audio = file("audiofile", "song.mp3", "audio/mpeg");
        MockMultipartFile cover = file("coverfile", "cover.png", "image/png");
        mvc.perform(multipart("/api/songs").file(audio).file(cover)
                .requestAttr("userId", 1L))
                .andExpect(status().isOk());

        verify(songsService).uploadSong(audio, cover, 1L, null, null, null, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"audiofile", "coverfile"})
    void requiresBothUploadFiles(String missingField) throws Exception {
        String presentField = missingField.equals("audiofile") ? "coverfile" : "audiofile";
        mvc.perform(multipart("/api/songs")
                .file(file(presentField, "valid.mp3", "audio/mpeg"))
                .requestAttr("userId", 1L))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(songsService);
    }

    @Test
    void rejectsInvalidReplacementCoverName() throws Exception {
        mvc.perform(multipart(HttpMethod.PATCH, "/api/songs/my-uploads/2")
                .file(file("song_cover", "../cover.png", "image/png"))
                .requestAttr("userId", 1L))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(songsService);
    }

    @Test
    void allowsEditingMetadataWithoutUploadingACover() throws Exception {
        mvc.perform(multipart(HttpMethod.PATCH, "/api/songs/my-uploads/2")
                .param("song_name", "新歌名")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk());

        verify(songsService).editUploadSong(argThat(dto -> dto.getSong_cover() == null
                && "新歌名".equals(dto.getSong_name())), eq(1L), eq(2L));
    }

    private MockMultipartFile file(String field, String name, String contentType) {
        return new MockMultipartFile(field, name, contentType, new byte[] {1});
    }
}
