ALTER TABLE songs_playlists_relation
    ADD CONSTRAINT uq_playlist_song_position UNIQUE (playlist_id, song_playlist_position);
