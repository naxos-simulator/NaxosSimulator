DROP TABLE IF EXISTS planet_osm_line_ram;
CREATE TABLE planet_osm_line_ram(osm_id int);
SELECT AddGeometryColumn('planet_osm_line_ram', 'way', 900913, 'LINESTRING', 2);

INSERT INTO planet_osm_line_ram(osm_id, way) SELECT osm_id, way FROM planet_osm_line WHERE highway in ('motorway', 'motorway_link', 'trunk', 'trunk_link', 'primary', 'primary_link', 'secondary', 'secondary_link', 'tertiary',  'tertiary_link');--, 'living_street', 'pedestrian', 'residential', 'unclassified', 'service', 'track', 'bus_guideway', 'raceway', 'road');
--INSERT INTO planet_osm_line_ram(osm_id, way) SELECT osm_id, way FROM planet_osm_line WHERE highway in ('motorway', 'motorway_link', 'trunk', 'trunk_link', 'primary', 'primary_link', 'secondary', 'secondary_link');
--INSERT INTO planet_osm_line_ram(osm_id, way) SELECT osm_id, way FROM planet_osm_line WHERE highway in ('motorway', 'motorway_link');

DROP TABLE IF EXISTS planet_osm_point_box;
CREATE TABLE planet_osm_point_box(osm_id1 int, osm_id2 int);

SELECT AddGeometryColumn('planet_osm_point_box', 'way1', 900913, 'LINESTRING', 2);
SELECT AddGeometryColumn('planet_osm_point_box', 'way2', 900913, 'LINESTRING', 2);

INSERT INTO planet_osm_point_box(osm_id1, osm_id2, way1, way2) SELECT a1.osm_id, a2.osm_id, a1.way, a2.way FROM planet_osm_line_ram a1, planet_osm_line_ram a2 WHERE (a1.osm_id <> a2.osm_id AND a1.way && a2.way);

SELECT pg_size_pretty(pg_relation_size('planet_osm_point_box'));

DELETE FROM planet_osm_point_box WHERE GeometryType(ST_INTERSECTION(way1, way2)) <> 'POINT';

VACUUM;

SELECT pg_size_pretty(pg_relation_size('planet_osm_point_box'));
SELECT COUNT(*) FROM planet_osm_point_box;
DROP TABLE IF EXISTS planet_osm_point_box2;
CREATE TABLE planet_osm_point_box2(osm_id1 int, osm_id2 int);

SELECT AddGeometryColumn('planet_osm_point_box2', 'way1', 900913, 'LINESTRING', 2);
SELECT AddGeometryColumn('planet_osm_point_box2', 'way2', 900913, 'LINESTRING', 2);

INSERT INTO planet_osm_point_box2 SELECT * from planet_osm_point_box;

SELECT pg_size_pretty(pg_relation_size('planet_osm_point_box'));  
SELECT pg_size_pretty(pg_relation_size('planet_osm_point_box2')); 

DROP TABLE IF EXISTS planet_osm_point_box;
VACUUM;

SELECT AddGeometryColumn('planet_osm_point_box2', 'point', 900913, 'POINT', 2);

UPDATE planet_osm_point_box2 SET point = ST_INTERSECTION(way1, way2);

SELECT pg_size_pretty(pg_relation_size('planet_osm_point_box2')); 
ALTER TABLE planet_osm_point_box2 ADD pos1 float;
ALTER TABLE planet_osm_point_box2 ADD pos2 float;

UPDATE planet_osm_point_box2 SET pos1 = ST_Line_Locate_Point(way1, point), pos2 = ST_Line_Locate_Point(way2, point);

SELECT pg_size_pretty(pg_relation_size('planet_osm_point_box2')); 

select count(1) from (SELECT DISTINCT way1, point, pos1 FROM planet_osm_point_box2) as t;
select count(1) from (SELECT DISTINCT way2, point, pos2 FROM planet_osm_point_box2) as t;

DROP TABLE IF EXISTS planet_osm_point_line2;
CREATE TABLE planet_osm_point_line2(osm_id int, pos float);-- TABLESPACE RAM_TABLESPACE;
SELECT AddGeometryColumn('planet_osm_point_line2', 'point', 900913, 'POINT', 2 );
SELECT AddGeometryColumn('planet_osm_point_line2', 'line', 900913, 'LINESTRING', 2 );

INSERT INTO planet_osm_point_line2(osm_id, pos, point, line) SELECT distinct osm_id1, pos1, point, way1 FROM planet_osm_point_box2;

ALTER TABLE planet_osm_point_line2 ADD point_id serial not null;

select count(*) from planet_osm_point_line2;

drop table IF EXISTS planet_osm_point_line3;
create table planet_osm_point_line3(osm_id int, point_id int, pos float);-- TABLESPACE RAM_TABLESPACE;

insert into planet_osm_point_line3 select osm_id, point_id, pos from planet_osm_point_line2;

drop table IF EXISTS planet_osm_line_bb_id;
create table planet_osm_line_bb_id(osm_id int, pos1 float, pos2 float);

INSERT INTO planet_osm_line_bb_id(osm_id, pos1, pos2) SELECT p1.osm_id, p1.pos, p2.pos FROM planet_osm_point_line3 p1, planet_osm_point_line3 p2 WHERE p1.point_id <> p2.point_id AND p1.osm_id = p2.osm_id AND p1.pos < p2.pos AND NOT EXISTS (SELECT 1 FROM planet_osm_point_line3 p3  WHERE p1.osm_id = p3.osm_id AND p1.pos < p3.pos AND p3.pos < p2.pos AND p2.point_id <> p3.point_id AND p1.point_id <> p3.point_id);

select count(*)  from (select distinct * from planet_osm_line_bb_id where pos1 <> pos2) as t;

DROP TABLE IF EXISTS planet_osm_line_bb2;
CREATE TABLE planet_osm_line_bb2();
SELECT AddGeometryColumn('planet_osm_line_bb2', 'line', 900913, 'LINESTRING', 2 );

INSERT INTO planet_osm_line_bb2(line) SELECT ST_line_substring(t2.line, pos1, pos2) FROM (select distinct * from planet_osm_line_bb_id where pos1 <> pos2) AS T JOIN planet_osm_point_line2 T2 ON (T.osm_id = T2.osm_id) WHERE GeometryType(ST_line_substring(t2.line, pos1, pos2)) = 'LINESTRING';

SELECT pg_size_pretty(pg_relation_size('planet_osm_line_bb2'));

DROP TABLE IF EXISTS planet_osm_line_bb3; 
CREATE TABLE planet_osm_line_bb3(osm_id serial not null);
SELECT AddGeometryColumn('planet_osm_line_bb3', 'line', 900913, 'LINESTRING', 2 );

INSERT INTO planet_osm_line_bb3(line) select distinct LINE from planet_osm_line_bb2;

SELECT pg_size_pretty(pg_relation_size('planet_osm_line_bb3'));

DROP TABLE IF EXISTS planet_osm_point_id;
CREATE TABLE planet_osm_point_id(osm_id serial);
SELECT AddGeometryColumn('planet_osm_point_id', 'point', 900913, 'POINT', 2 );

INSERT INTO planet_osm_point_id(point) select distinct point FROM planet_osm_point_box2;

SELECT pg_size_pretty(pg_relation_size('planet_osm_point_id')); -- 0.9 MB

DROP TABLE IF EXISTS planet_osm_point_id0;
create table planet_osm_point_id0();
SELECT AddGeometryColumn('planet_osm_point_id0', 'line', 900913, 'LINESTRING', 2 );
SELECT AddGeometryColumn('planet_osm_point_id0', 'p1', 900913, 'POINT', 2 );
SELECT AddGeometryColumn('planet_osm_point_id0', 'p2', 900913, 'POINT', 2 );

INSERT INTO planet_osm_point_id0(line, p1, p2) SELECT line, ST_Line_Interpolate_Point(line, 0), ST_Line_Interpolate_Point(line, 1) FROM planet_osm_line_bb3;

ALTER TABLE planet_osm_point_id0 add start_id int;
ALTER TABLE planet_osm_point_id0 add end_id int;

ALTER TABLE planet_osm_point_id0 add dist_in_m int;

UPDATE planet_osm_point_id0 SET dist_in_m  = cast(GREATEST(round(ST_LENGTH(line)/7.5), 3) as int); 

create index t1 on  planet_osm_point_id(point, osm_id);

UPDATE planet_osm_point_id0 SET start_id  = (SELECT osm_id FROM planet_osm_point_id  WHERE p1 = point);

UPDATE planet_osm_point_id0 SET end_id  = (SELECT osm_id FROM planet_osm_point_id  WHERE p2 = point);

DELETE FROM planet_osm_point_id0 WHERE line IN (SELECT b.line FROM planet_osm_point_id0 a, planet_osm_point_id0 b WHERE (a.start_id = b.start_id AND a.end_id = b.end_id and a.dist_in_m < b.dist_in_m));

DELETE FROM planet_osm_point_id0 WHERE start_id = end_id;
 
