package com.kyf.mp.server.common.file;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;

/** 上传与本地扫描共用的歌曲、封面命名规则。 */
public final class SongFileNames {
    private static final DateTimeFormatter VERSION_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final Pattern UUID_SUFFIX = Pattern.compile(
            "-([0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12})$", Pattern.CASE_INSENSITIVE);

    private SongFileNames() {
    }

    public static String audioName(String title, String extension) {
        return audioName(title, extension, UUID.randomUUID().toString());
    }

    public static String renamedAudioName(String oldName, String title) {
        Matcher matcher = UUID_SUFFIX.matcher(StringUtils.stripFilenameExtension(oldName));
        String id = matcher.find() ? matcher.group(1) : UUID.randomUUID().toString();
        return audioName(title, StringUtils.getFilenameExtension(oldName), id);
    }

    private static String audioName(String title, String extension, String id) {
        String safeTitle = StringUtils.hasText(title) ? title : "未命名";
        safeTitle = safeTitle.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_");
        return "audio-" + safeTitle + "-" + id + "." + extension.toLowerCase(Locale.ROOT);
    }

    public static String coverName(String audioName, String extension) {
        String imageExtension = extension.toLowerCase(Locale.ROOT);
        if ("jpg".equals(imageExtension)) {
            imageExtension = "jpeg";
        }
        return StringUtils.stripFilenameExtension(audioName) + "-cover." + imageExtension;
    }

    public static String fileNameFromUrl(String url) {
        int query = url.indexOf('?');
        int fragment = url.indexOf('#');
        int end = url.length();
        if (query >= 0) {
            end = Math.min(end, query);
        }
        if (fragment >= 0) {
            end = Math.min(end, fragment);
        }
        String name = StringUtils.getFilename(url.substring(0, end));
        try {
            return UriUtils.decode(name, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            // 兼容历史记录中未编码的字面百分号。
            return name;
        }
    }

    public static String publicUrl(String prefix, String fileName) {
        return prefix + UriUtils.encode(fileName, StandardCharsets.UTF_8);
    }

    public static String versionedUrl(String prefix, String fileName) {
        String version = LocalDateTime.now(ZoneId.systemDefault()).format(VERSION_FORMAT);
        return publicUrl(prefix, fileName) + "?v=" + version;
    }
}
