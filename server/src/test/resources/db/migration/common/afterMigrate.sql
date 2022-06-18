INSERT INTO artists VALUES (1, 'In Flames');
INSERT INTO genres VALUES (1, 'Death Metal');
INSERT INTO tracks VALUES (1, 1, 1, 'Clayman', 'Bulleted Ride', 2000, 312, 100, 'A comment', 1, 1, 1, 12, 'f', 1);
INSERT INTO tracks VALUES (2, 1, 1, 'Clayman', 'Only For The Weak', 2000, 295, 100, 'A comment', 1, 1, 3, 12, 'f', 1);
INSERT INTO plays (track_id, played_on) VALUES(1, curdate() - INTERVAL 1 DAY);
INSERT INTO plays (track_id, played_on) VALUES(2, curdate());
