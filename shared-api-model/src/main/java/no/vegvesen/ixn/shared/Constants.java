package no.vegvesen.ixn.shared;

import java.util.List;

public class Constants {
    public static final String DATEX_2 = "DATEX2";
    public static final String DENM = "DENM";
    public static final String IVIM = "IVIM";
    public static final String SPATEM = "SPATEM";
    public static final String MAPEM = "MAPEM";
    public static final String SREM = "SREM";
    public static final String SSEM = "SSEM";
    public static final String CAM = "CAM";

    public static List<String> getAllMessageTypes() {
        return List.of(
                DATEX_2,
                DENM,
                IVIM,
                SPATEM,
                MAPEM,
                SREM,
                SSEM,
                CAM
        );
    }
}
