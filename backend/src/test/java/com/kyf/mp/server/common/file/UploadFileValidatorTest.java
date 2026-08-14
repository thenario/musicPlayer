package com.kyf.mp.server.common.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kyf.mp.server.common.BusinessException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class UploadFileValidatorTest {

    @Test
    void acceptsReadablePngImage() throws Exception {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        MockMultipartFile file = new MockMultipartFile("cover", "cover.PNG", "image/png", output.toByteArray());

        assertThat(UploadFileValidator.validateImage(file)).isEqualTo("png");
    }

    @Test
    void rejectsImageWithInvalidContent() {
        MockMultipartFile file = new MockMultipartFile("cover", "cover.png", "image/png", new byte[] {1, 2, 3});

        assertThatThrownBy(() -> UploadFileValidator.validateImage(file))
                .isInstanceOf(BusinessException.class)
                .hasMessage("封面图片内容无效");
    }

    @Test
    void rejectsAudioWithUnexpectedContentType() {
        MockMultipartFile file = new MockMultipartFile("song", "song.mp3", "text/plain", new byte[] {1});

        assertThatThrownBy(() -> UploadFileValidator.validateAudio(file))
                .isInstanceOf(BusinessException.class)
                .hasMessage("音频Content-Type 不合法");
    }
}
