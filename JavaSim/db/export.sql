COPY (select start_id, end_id, dist_in_m, cast(ST_Azimuth(p1, p2) / (2*pi())*360 as int) as deg from planet_osm_point_id0) TO '/tmp/segments.txt';

drop view IF EXISTS scv_map_segments;
create view scv_map_segments as select start_id, end_id, rel_pos - 1 as rel_pos, round(st_x(st_line_interpolate_Point(line, 1.0*rel_pos/dist_in_m))) x, round(st_y(st_line_interpolate_Point(line, 1.0*rel_pos/dist_in_m))) y from (select generate_series(1, dist_in_m) rel_pos, * from planet_osm_point_id0) as t;

COPY (select start_id, end_id, rel_pos, x, y from scv_map_segments) to '/tmp/map_segments.txt';
COPY (select a.start_id, a.end_id, b.end_id from planet_osm_point_id0 a join planet_osm_point_id0 b on (a.end_id = b.start_id)) to '/tmp/crossings.txt';
