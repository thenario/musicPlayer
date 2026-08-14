package com.kyf.mp.server.common.file;

import com.kyf.mp.server.common.BusinessException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import javax.imageio.ImageIO;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/** Validates uploaded media before it is written to public static storage. */
public final class UploadFileValidator {
    private static final Set<String> AUDIO_EXTENSIONS = Set.of("mp3", "flac", "wav", "m4a");
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png");
    private static final Set<String> AUDIO_CONTENT_TYPES = Set.of(
            "audio/mpeg", "audio/flac", "audio/x-flac", "audio/wav", "audio/x-wav", "audio/mp4", "audio/x-m4a");
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    private UploadFileValidator() {
    }

    public static String validateAudio(MultipartFile file) {
        return validateMedia(file, AUDIO_EXTENSIONS, AUDIO_CONTENT_TYPES, "音频");
    }

    public static String validateImage(MultipartFile file) {
        String extension = validateMedia(file, IMAGE_EXTENSIONS, IMAGE_CONTENT_TYPES, "封面图片");
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
                throw new BusinessException(400, "封面图片内容无效");
            }
        } catch (IOException e) {
            throw new BusinessException(400, "封面图片无法读取");
        }
        return extension;
    }

    private static String validateMedia(MultipartFile file, Set<String> allowedExtensions,
            Set<String> allowedContentTypes, String label) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, label + "不能为空");
        }
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (!StringUtils.hasText(extension)) {
            throw new BusinessException(400, label + "缺少文件扩展名");
        }
        extension = extension.toLowerCase(Locale.ROOT);
        if (!allowedExtensions.contains(extension)) {
            throw new BusinessException(400, "不支持的" + label + "格式");
        }
        String contentType = file.getContentType();
        if (!allowedContentTypes.contains(contentType)) {
            throw new BusinessException(400, label + "Content-Type 不合法");
        }
        return extension;
    }
}