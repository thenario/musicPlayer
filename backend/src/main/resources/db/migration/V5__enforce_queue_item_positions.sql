ALTER TABLE queue_items
    ADD CONSTRAINT uq_queue_item_position UNIQUE (queue_id, queue_item_position);
