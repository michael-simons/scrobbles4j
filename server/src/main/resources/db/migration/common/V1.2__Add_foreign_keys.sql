ALTER TABLE tracks ADD CONSTRAINT tracks_artists_fk FOREIGN KEY (artist_id) REFERENCES artists(id);
ALTER TABLE tracks ADD CONSTRAINT tracks_genres_fk FOREIGN KEY (genre_id) REFERENCES genres(id);
ALTER TABLE plays ADD CONSTRAINT plays_tracks_fk FOREIGN KEY (track_id) REFERENCES tracks(id);
