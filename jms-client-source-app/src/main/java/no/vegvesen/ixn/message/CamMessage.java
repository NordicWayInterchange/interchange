package no.vegvesen.ixn.message;

import no.vegvesen.ixn.federation.api.v1_0.Constants;

public class CamMessage extends Message {

    private Integer stationType;

    private Integer vehicleRole;

    public CamMessage() {

    }

    public CamMessage(String messageText,
                      String userId,
                      String publisherId,
                      String publicationId,
                      String originatingCountry,
                      String protocolVersion,
                      String serviceType,
                      String baselineVersion,
                      float longitude,
                      float latitude,
                      String quadTree,
                      Integer shardId,
                      Integer shardCount,
                      Integer stationType,
                      Integer vehicleRole) {
        super(messageText,
                userId,
                publisherId,
                publicationId,
                originatingCountry,
                protocolVersion,
                serviceType,
                baselineVersion,
                Constants.CAM,
                longitude,
                latitude,
                quadTree,
                shardId,
                shardCount);
        this.stationType = stationType;
        this.vehicleRole = vehicleRole;
    }

    public Integer getStationType() {
        return stationType;
    }

    public void setStationType(Integer stationType) {
        this.stationType = stationType;
    }

    public Integer getVehicleRole() {
        return vehicleRole;
    }

    public void setVehicleRole(Integer vehicleRole) {
        this.vehicleRole = vehicleRole;
    }
}
