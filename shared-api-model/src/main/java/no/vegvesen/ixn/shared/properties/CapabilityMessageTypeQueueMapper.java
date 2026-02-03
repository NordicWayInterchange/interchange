package no.vegvesen.ixn.shared.properties;

import java.util.Map;

public class CapabilityMessageTypeQueueMapper {

    private CapabilityMessageTypeQueueMapper() {
    }

    public static final Map<String, String> MESSAGE_TYPE_TO_QUEUE = Map.of(
            "DATEX2", "bi-datex",
            "DENM",   "bi-denm",
            "IVIM",   "bi-ivim",
            "SPATEM", "bi-spatem",
            "MAPEM",  "bi-mapem",
            "SREM",   "bi-ssrem",
            "SSEM",   "bi-ssem",
            "CAM",    "bi-cam"
    );
}
