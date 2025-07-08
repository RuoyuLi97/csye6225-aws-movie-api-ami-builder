DROP DATABASE recommend;
CREATE DATABASE IF NOT EXISTS recommend;
USE recommend;

-- Create Schema
DROP TABLE IF EXISTS temp_movies;
CREATE TABLE temp_movies (
    movieId INT,
    title VARCHAR(255) NOT NULL,
    genres VARCHAR(255)
);

DROP TABLE IF EXISTS movies;
CREATE TABLE movies (
    movieId INT NOT NULL PRIMARY KEY,
    title VARCHAR(255) NOT NULL
);

CREATE INDEX idx_movies_movieId ON movies(movieId);

DROP TABLE IF EXISTS temp_genres;
CREATE TABLE temp_genres (
	movieId INT,
    genre VARCHAR(255)
);

DROP TABLE IF EXISTS genres;
CREATE TABLE genres (
    genreId INT NOT NULL AUTO_INCREMENT,
    genre VARCHAR(255) DEFAULT NULL,
    UNIQUE KEY (genre),
    PRIMARY KEY (genreId)
);

DROP TABLE IF EXISTS movies_genres;
CREATE TABLE movies_genres (
    movieId INT NOT NULL,
   	genreId INT NOT NULL,
   	PRIMARY KEY (movieId, genreId)
);

DROP TABLE IF EXISTS links;
CREATE TABLE links (
    movieId INT NOT NULL PRIMARY KEY,
	imdbId varchar(30) DEFAULT NULL,
	tmdbId varchar(30) DEFAULT NULL
);

CREATE INDEX idx_links_movieId ON links(movieId);

DROP TABLE IF EXISTS ratings;
CREATE TABLE ratings (
    userId INT NOT NULL,
    movieId INT NOT NULL,
    rating decimal(2,1) DEFAULT NULL,
    timestamp INT DEFAULT NULL,
    PRIMARY KEY (userId, movieId)
);

CREATE INDEX idx_ratings_movieId ON ratings(movieId);

DROP TABLE IF EXISTS tags;
CREATE TABLE tags (
   	userId INT NOT NULL,
    movieId INT NOT NULL,
    tag VARCHAR(255) NOT NULL,
    timestamp INT DEFAULT NULL,
    PRIMARY KEY (userId, movieId, tag)
);

CREATE INDEX idx_tags_userId ON tags(userId);

-- Add Foreign Key
ALTER TABLE movies_genres
ADD CONSTRAINT FK_MGMovie FOREIGN KEY (movieId)
REFERENCES movies(movieId);

ALTER TABLE movies_genres
ADD CONSTRAINT FK_MGGenre FOREIGN KEY (genreId)
REFERENCES genres(genreId);

ALTER TABLE links
ADD CONSTRAINT FK_LMovie FOREIGN KEY (movieId)
REFERENCES movies(movieId);

ALTER TABLE ratings
ADD CONSTRAINT FK_RMovie FOREIGN KEY (movieId)
REFERENCES movies(movieId);

ALTER TABLE tags
ADD CONSTRAINT FK_TMovie FOREIGN KEY (movieId)
REFERENCES movies(movieId);

-- load data
LOAD DATA LOCAL INFILE '~/Desktop/CSYE6225/ml-32m/movies.csv' 
INTO TABLE temp_movies
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'  
IGNORE 1 ROWS
(movieId, title, @genres)
SET genres = REPLACE(REPLACE(@genres, CHAR(10), ''), CHAR(13), '');

INSERT INTO movies (movieId, title)
SELECT DISTINCT movieId, title
FROM temp_movies;

DELIMITER $$

CREATE PROCEDURE split_genres_to_temp()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE movie_id INT;
    DECLARE genre_str VARCHAR(255);
    DECLARE genre_value VARCHAR(255);
    
    DECLARE genre_cursor CURSOR FOR
        SELECT movieId, genres FROM temp_movies;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN genre_cursor;

    read_loop: LOOP
        FETCH genre_cursor INTO movie_id, genre_str;
        IF done THEN 
            LEAVE read_loop;
        END IF;

        IF genre_str IS NULL OR genre_str = '' THEN
            SET genre_str = '(no genres listed)';
        END IF;

        WHILE LENGTH(genre_str) > 0 DO
            SET genre_value = TRIM(SUBSTRING_INDEX(genre_str, '|', 1));

            SET genre_str = IF(LOCATE('|', genre_str) > 0, 
                               SUBSTRING(genre_str FROM LOCATE('|', genre_str) + 1), 
                               ''); 

            INSERT INTO temp_genres (movieId, genre) VALUES (movie_id, genre_value);
        END WHILE;
    END LOOP;

    CLOSE genre_cursor;
END $$

DELIMITER ;

CALL split_genres_to_temp();

INSERT INTO genres (genre)
SELECT DISTINCT genre
FROM temp_genres;

INSERT INTO movies_genres (movieId, genreId)
SELECT tg.movieId, g.genreId
FROM temp_genres tg
JOIN genres g ON tg.genre = g.genre;

DROP TABLE IF EXISTS temp_movies;
DROP TABLE IF EXISTS temp_genres;

LOAD DATA LOCAL INFILE '~/Desktop/CSYE6225/ml-32m/links.csv' 
INTO TABLE links
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(movieId, imdbId, @tmdbId)
SET tmdbId = REPLACE(REPLACE(@tmdbId, CHAR(10), ''), CHAR(13), '');

LOAD DATA LOCAL INFILE '~/Desktop/CSYE6225/ml-32m/ratings.csv' 
INTO TABLE ratings
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(userId, movieId, rating, timestamp);

LOAD DATA LOCAL INFILE '~/Desktop/CSYE6225/ml-32m/tags.csv' 
INTO TABLE tags
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(userId, movieId, tag, timestamp);

