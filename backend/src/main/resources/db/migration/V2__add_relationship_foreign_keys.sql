-- Enforce the relationships already assumed by the application layer.

ALTER TABLE songs
    ADD CONSTRAINT fk_songs_uploader
        FOREIGN KEY (uploader_id) REFERENCES users (user_id)
        ON DELETE RESTRICT;

ALTER TABLE playlists
    ADD CONSTRAINT fk_playlists_creator
        FOREIGN KEY (creator_id) REFERENCES users (user_id)
        ON DELETE CASCADE;

ALTER TABLE play_state
    ADD CONSTRAINT fk_play_state_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE;

ALTER TABLE queues
    ADD CONSTRAINT fk_queues_creator
        FOREIGN KEY (creator_id) REFERENCES users (user_id)
        ON DELETE CASCADE;

ALTER TABLE queue_items
    ADD CONSTRAINT fk_queue_items_queue
        FOREIGN KEY (queue_id) REFERENCES queues (queue_id)
        ON DELETE CASCADE,
    ADD CONSTRAINT fk_queue_items_song
        FOREIGN KEY (song_id) REFERENCES songs (song_id)
        ON DELETE CASCADE;

ALTER TABLE songs_playlists_relation
    ADD CONSTRAINT fk_playlist_songs_song
        FOREIGN KEY (song_id) REFERENCES songs (song_id)
        ON DELETE CASCADE;

ALTER TABLE users_likeplaylists_relation
    ADD CONSTRAINT fk_user_likes_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE,
    ADD CONSTRAINT fk_user_likes_playlist
        FOREIGN KEY (playlist_id) REFERENCES playlists (playlist_id)
        ON DELETE CASCADE;

ALTER TABLE users_playlists_relation
    ADD CONSTRAINT fk_user_playlists_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE,
    ADD CONSTRAINT fk_user_playlists_playlist
        FOREIGN KEY (playlist_id) REFERENCES playlists (playlist_id)
        ON DELETE CASCADE;
