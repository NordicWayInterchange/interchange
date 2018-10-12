package no.vegvesen.ixn;

import java.sql.*;
import java.util.Properties;

public class GeoLookup {

    /*
    A class that establishes a connection to a database in a docker container,
    does a query and reads the answer from the database.
     */

    public static void main(String[] args){


        String url = "jdbc:postgresql://localhost:5432/geolookup";
        Properties properties = new Properties();
        properties.setProperty("user", "geolookup");
        properties.setProperty("password", "geolookup");

        try {
            Connection connection = DriverManager.getConnection(url, properties);

            Statement statement = connection.createStatement();

            ResultSet result = statement.executeQuery("SELECT iso2 FROM worldshape_10kmbuffer WHERE ST_Within(ST_GeomFromText('POINT(10.0 63.0)'), worldshape_10kmbuffer.geom);");

            int i = 0;
            while(result.next()){
                String iso = result.getString("iso2");
                System.out.println(iso);
                i ++;
            }


        }catch(Exception e){
            System.out.println("problem " + e.getMessage());
        }

    }


}
