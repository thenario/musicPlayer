package com.kyf.mp.server.common.file;

import java.io.File;
import java.nio.file.Path;

/** 将配置的存储目录统一解析为绝对、规范的本地磁盘路径。 */
public final class StoragePathResolver {

    private StoragePathResolver() {
    }

    public static Path resolveDirectory(String configuredPath) {
        return Path.of(configuredPath).toAbsolutePath().normalize();
    }

    public static File resolveFile(String configuredDirectory, String fileName) {
        Path directory = resolveDirectory(configuredDirectory);
        Path target = directory.resolve(fileName).normalize();
        if (!target.startsWith(directory)) {
            throw new IllegalArgumentException("文件名不能跳出存储目录");
        }
        return target.toFile();
    }
}
