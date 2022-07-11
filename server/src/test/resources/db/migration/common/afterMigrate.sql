DELETE FROM plays;
DELETE FROM tracks;
DELETE FROM genres;
DELETE FROM artists;

INSERT INTO artists VALUES (1, 'In Flames');
INSERT INTO artists VALUES (2, 'Danger Dan');
INSERT INTO genres VALUES (1, 'Death Metal');
INSERT INTO genres VALUES (2, 'Rap');
INSERT INTO tracks VALUES (1, 1, 1, 'Clayman', 'Bulleted Ride', 2000, 312, 100, 'A comment', 1, 1, 1, 12, 'f', 1);
INSERT INTO tracks VALUES (2, 1, 1, 'Clayman', 'Only For The Weak', 2000, 295, 100, 'A comment', 1, 1, 3, 12, 'f', 1);
INSERT INTO tracks VALUES (3, 2, 2, 'Coming Out EP', 'Private Altersvorsorge', 2008, 167, 0, 'www.anti-alles-aktion.com', 1, 1, 7, 7, 'f', 1);
INSERT INTO plays (track_id, played_on) VALUES(1, curdate() - INTERVAL 1 DAY);
INSERT INTO plays (track_id, played_on) VALUES(2, curdate() + INTERVAL 123 MINUTE);
INSERT INTO plays (track_id, played_on) VALUES(3, current_time() - INTERVAL 25 HOUR);
