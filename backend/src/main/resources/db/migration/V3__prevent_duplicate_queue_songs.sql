ALTER TABLE queue_items
    ADD CONSTRAINT uq_queue_items_queue_song UNIQUE (queue_id, song_id);
