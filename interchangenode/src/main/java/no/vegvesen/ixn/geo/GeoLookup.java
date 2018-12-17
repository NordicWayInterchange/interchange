package no.vegvesen.ixn.geo;
import org.postgis.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeoLookup{
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GeoLookup(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public List<String> getCountries(double lat, double lon){
        String sql = "SELECT iso2 FROM worldshape_10kmbuffer WHERE ST_Within(ST_GeomFromText(?), worldshape_10kmbuffer.geom)";
        return jdbcTemplate.queryForList(sql, new Point[] {new Point(lon, lat)}, new int[]{Point.POINT}, String.class);
    }
}
