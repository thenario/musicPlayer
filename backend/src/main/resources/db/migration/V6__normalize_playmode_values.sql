UPDATE play_state SET playmode = 'sequential' WHERE playmode = 'sequence' OR playmode IS NULL;

ALTER TABLE play_state ALTER playmode SET DEFAULT 'sequential';
