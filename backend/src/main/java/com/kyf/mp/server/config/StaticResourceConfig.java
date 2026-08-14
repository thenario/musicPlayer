package com.kyf.mp.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * 静态资源映射：把 /static/** 映射到本地上传目录，由后端自己提供歌曲/封面等文件。
 * 上传路径来自 application.yml 的 file.upload.*，容器内为 /app/static/*（宿主机 static/ 卷挂载）。
 * 生产环境 Nginx 将 /static/ 转发到本服务，开发环境由 Vite 的 /static 代理转发。
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${file.upload.song-path}")
    private String songPath;

    @Value("${file.upload.song-cover-path}")
    private String songCoverPath;

    @Value("${file.upload.playlist-cover-path}")
    private String playlistCoverPath;

    @Value("${file.upload.user-cover-path}")
    private String userCoverPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/songs/**").addResourceLocations(toFileUrl(songPath));
        registry.addResourceHandler("/static/song_covers/**").addResourceLocations(toFileUrl(songCoverPath));
        registry.addResourceHandler("/static/playlist_covers/**").addResourceLocations(toFileUrl(playlistCoverPath));
        registry.addResourceHandler("/static/user_covers/**").addResourceLocations(toFileUrl(userCoverPath));
    }

    /** 磁盘路径 -> Spring 的 file: URL（兼容 Windows/Linux）。 */
    private String toFileUrl(String path) {
        String fileUrl = Path.of(path).toUri().toString();
        return fileUrl.endsWith("/") ? fileUrl : fileUrl + "/";
    }
}
