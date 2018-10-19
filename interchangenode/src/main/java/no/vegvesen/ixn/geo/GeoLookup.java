package no.vegvesen.ixn.geo;
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


    public List<String> getCountries(float lat, float lon){
        String sql = String.format("SELECT iso2 FROM worldshape_10kmbuffer WHERE ST_Within(ST_GeomFromText('POINT(%.1f %.1f)'), worldshape_10kmbuffer.geom)", lat, lon);
        return jdbcTemplate.queryForList(sql, String.class);
    }


    public List<String> getCountries(String latitude, String longitude){
        try {
            float lat = Float.parseFloat(latitude);
            float lon = Float.parseFloat(longitude);
            return getCountries(lat, lon);
        } catch (Exception e){
            throw new IllegalArgumentException(e);
        }
    }

}
