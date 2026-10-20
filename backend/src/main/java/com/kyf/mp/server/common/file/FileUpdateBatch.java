package com.kyf.mp.server.common.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.kyf.mp.server.common.BusinessException;

import lombok.extern.slf4j.Slf4j;

/** 先准备文件，再发布；数据库事务结束后清理旧文件或恢复备份。 */
@Slf4j
public final class FileUpdateBatch {
    private final List<Change> changes = new ArrayList<>();
    private boolean finished;

    public Path stage(Path original, Path target) throws IOException {
        if (Files.exists(target) && !target.equals(original)) {
            throw new BusinessException(409, "目标文件已存在，请更换歌曲名称");
        }
        Files.createDirectories(target.getParent());
        Path directory = Files.createTempDirectory(target.getParent(), ".song-edit-");
        Path staged = directory.resolve(target.getFileName());
        Path backup = target.equals(original) && Files.exists(original) ? directory.resolve("original.backup") : null;
        changes.add(new Change(original, target, staged, backup, directory));
        if (backup != null) {
            Files.copy(original, backup);
        }
        return staged;
    }

    public void publish() throws IOException {
        for (Change change : changes) {
            if (change.backup != null) {
                Files.move(change.staged, change.target, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.move(change.staged, change.target);
            }
            change.published = true;
        }
    }

    public void completeAfterTransaction() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_COMMITTED) {
                        commit();
                    } else if (status == STATUS_ROLLED_BACK) {
                        rollback();
                    } else {
                        log.error("文件更新事务状态未知，保留原文件及备份供检查");
                    }
                }
            });
        } else {
            commit();
        }
    }

    public void rollback() {
        if (finished) {
            return;
        }
        finished = true;
        for (int i = changes.size() - 1; i >= 0; i--) {
            Change change = changes.get(i);
            try {
                if (change.published) {
                    if (change.backup != null) {
                        Files.copy(change.backup, change.target, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        Files.deleteIfExists(change.target);
                    }
                }
                cleanup(change);
            } catch (IOException exception) {
                log.error("恢复歌曲文件失败，保留备份目录 {}", change.directory, exception);
            }
        }
    }

    private void commit() {
        if (finished) {
            return;
        }
        finished = true;
        for (Change change : changes) {
            try {
                if (change.original != null && !change.original.equals(change.target)) {
                    Files.deleteIfExists(change.original);
                }
                cleanup(change);
            } catch (IOException exception) {
                log.warn("歌曲更新已提交，清理旧文件失败：{}", change.original, exception);
            }
        }
    }

    private void cleanup(Change change) throws IOException {
        Files.deleteIfExists(change.staged);
        if (change.backup != null) {
            Files.deleteIfExists(change.backup);
        }
        Files.deleteIfExists(change.directory);
    }

    private static final class Change {
        private final Path original;
        private final Path target;
        private final Path staged;
        private final Path backup;
        private final Path directory;
        private boolean published;

        private Change(Path original, Path target, Path staged, Path backup, Path directory) {
            this.original = original;
            this.target = target;
            this.staged = staged;
            this.backup = backup;
            this.directory = directory;
        }
    }
}
