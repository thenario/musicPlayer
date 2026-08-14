package com.kyf.mp.server.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * 启动早期读取本地 .env 文件，将 key=value 注入 Spring Environment。
 *
 * <p>优先级：真实环境变量 &gt; .env 文件 &gt; application.yml 默认值。</p>
 * <p>因此：本地裸跑后端时用 backend/.env 提供密钥与数据库连接；
 * Docker 部署时由 compose 注入环境变量（容器内无 .env，走 os env）。</p>
 *
 * <p>默认读取工作目录下的 .env，可用 -Ddotenv.path=xxx 覆盖路径。</p>
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String DOTENV_PATH = System.getProperty("dotenv.path", ".env");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> vars = loadDotenv();
        if (vars.isEmpty()) {
            return;
        }
        // 插到 systemEnvironment 之后：os 环境变量优先，.env 兜底，application.yml 的 ${} 再兜底
        environment.getPropertySources()
                .addAfter("systemEnvironment", new MapPropertySource("dotenv", vars));
    }

    private Map<String, Object> loadDotenv() {
        Path path = Paths.get(DOTENV_PATH);
        Map<String, Object> map = new LinkedHashMap<>();
        if (!Files.isRegularFile(path)) {
            return map;
        }
        try {
            for (String line : Files.readAllLines(path)) {
                parseLine(line.trim(), map);
            }
        } catch (IOException e) {
            // 读取失败不阻断启动，交由后续占位符解析报错，便于定位缺失项
            System.err.println("[dotenv] 读取 .env 失败: " + e.getMessage());
        }
        return map;
    }

    private void parseLine(String line, Map<String, Object> map) {
        if (line.isEmpty() || line.startsWith("#")) {
            return;
        }
        // 兼容 export KEY=VALUE 写法
        if (line.startsWith("export ")) {
            line = line.substring("export ".length()).trim();
        }
        int idx = line.indexOf('=');
        if (idx <= 0) {
            return;
        }
        String key = line.substring(0, idx).trim();
        String value = line.substring(idx + 1).trim();
        // 去掉成对引号
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                value = value.substring(1, value.length() - 1);
            }
        }
        if (!key.isEmpty()) {
            map.put(key, value);
        }
    }
}
