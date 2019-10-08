DROP TABLE IF EXISTS person;
CREATE TABLE person (id serial PRIMARY KEY, first_name varchar(255), last_name varchar(255));

DROP TABLE IF EXISTS person_event;
CREATE TABLE person_event (id serial PRIMARY KEY, first_name varchar(255), last_name varchar(255), action varchar(255));
