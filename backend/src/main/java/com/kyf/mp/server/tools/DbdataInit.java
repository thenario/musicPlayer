package com.kyf.mp.server.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class DbdataInit {

    public static void main(String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: DbdataInit <songs-directory> <song-covers-directory>");
        }

        Path songsPath = requireDirectory(args[0], "songs directory");
        Path coversPath = requireDirectory(args[1], "song covers directory");
        try (Stream<Path> songs = Files.list(songsPath); Stream<Path> covers = Files.list(coversPath)) {
            long songCount = songs.filter(Files::isRegularFile).count();
            long coverCount = covers.filter(Files::isRegularFile).count();
            System.out.printf("Found %d song files and %d cover files.%n", songCount, coverCount);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to scan media directories", exception);
        }
    }

    static Path requireDirectory(String value, String description) {
        Path path = Path.of(value).toAbsolutePath().normalize();
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException(description + " is not a directory: " + path);
        }
        return path;
    }
}