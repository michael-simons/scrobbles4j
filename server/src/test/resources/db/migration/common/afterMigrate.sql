DELETE FROM plays;
DELETE FROM tracks;
DELETE FROM genres;
DELETE FROM artists;
DELETE FROM related_artists;

INSERT INTO artists VALUES (1, 'In Flames', 'https://en.wikipedia.org/wiki/In_Flames');
INSERT INTO artists VALUES (2, 'Danger Dan', 'https://de.wikipedia.org/wiki/Danger_Dan');
INSERT INTO artists VALUES (3, 'Juse Ju', null);
INSERT INTO artists VALUES (4, 'Queen', null);
INSERT INTO artists VALUES (5, 'Freddie Mercury', null);
INSERT INTO artists VALUES (6, 'Roger Taylor', null);
INSERT INTO artists VALUES (7, 'Koljah Danger Dan', null);
INSERT INTO artists VALUES (8, 'Koljah & Danger Dan', null);
INSERT INTO artists VALUES (9, 'Danger Dan & NMZS', null);
INSERT INTO related_artists VALUES (4, 5);
INSERT INTO related_artists VALUES (6, 4);
INSERT INTO genres VALUES (1, 'Death Metal');
INSERT INTO genres VALUES (2, 'Rap');
INSERT INTO tracks VALUES (1, 1, 1, 'Clayman', 'Bulleted Ride', 2000, 312, 100, 'A comment', 1, 1, 1, 12, 'f', 1);
INSERT INTO tracks VALUES (2, 1, 1, 'Clayman', 'Only For The Weak', 2000, 295, 100, 'A comment', 1, 1, 3, 12, 'f', 1);
INSERT INTO tracks VALUES (3, 2, 2, 'Coming Out EP', 'Private Altersvorsorge', 2008, 167, 0, 'www.anti-alles-aktion.com', 1, 1, 7, 7, 'f', 1);
INSERT INTO tracks VALUES (4, 3, 2, 'Shibuya Crossing', 'Propaganda [feat. Danger Dan]', 2018, 205, 0, 'amazon.de', 1, 1, 12, 3, 'f', 1);
INSERT INTO tracks VALUES (5, 8, 1, 'Traurige Clowns', 'Einleitung', 2010, 312, 100, 'A comment', 1, 1, 1, 12, 'f', 1);
INSERT INTO tracks VALUES (6, 9, 1, 'Aschenbecher', 'Lebensmotto Tarnkappe', 2012, 312, 100, 'A comment', 1, 1, 1, 12, 'f', 1);
INSERT INTO tracks VALUES (7, 7, 1, 'Gibt es nicht', 'Gibt es nicht', 2012, 312, 100, 'A comment', 1, 1, 1, 12, 'f', 1);
INSERT INTO plays (track_id, played_on) VALUES(1, curdate() - INTERVAL 1 DAY);
INSERT INTO plays (track_id, played_on) VALUES(2, curdate() + INTERVAL 123 MINUTE);
INSERT INTO plays (track_id, played_on) VALUES(3, current_time() - INTERVAL 25 HOUR);
