CREATE TABLE artists (
    id INTEGER NOT NULL,
    artist VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT artists_uk UNIQUE (artist)
);

CREATE TABLE genres (
    id INTEGER NOT NULL,
    genre VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT genres_uk UNIQUE (genre)
);

CREATE TABLE tracks (
    id INTEGER NOT NULL,
    artist_id INTEGER NOT NULL,
    genre_id INTEGER NOT NULL,
    album VARCHAR(255) NOT NULL,
    name VARCHAR(4000) NOT NULL,
    year SMALLINT NULL,
    duration INTEGER,
    rating SMALLINT,
    comment VARCHAR(255),
    disc_count   SMALLINT NULL,
    disc_number  SMALLINT NULL,
    track_count  SMALLINT NULL,
    track_number SMALLINT NULL,
    compilation VARCHAR(1) DEFAULT 'f' NOT NULL CHECK(compilation IN ('t','f')) ,
    PRIMARY KEY (id),
    CONSTRAINT tracks_uk UNIQUE (album, name, track_number, disc_number, artist_id, genre_id)
);

CREATE TABLE plays (
    id INTEGER NOT NULL,
    track_id INTEGER NOT NULL,
    played_on TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);
