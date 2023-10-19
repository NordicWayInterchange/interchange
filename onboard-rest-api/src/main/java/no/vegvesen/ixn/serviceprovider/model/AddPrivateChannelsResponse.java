package no.vegvesen.ixn.serviceprovider.model;

import java.util.ArrayList;
import java.util.List;

public class AddPrivateChannelsResponse {
    private String version = "1.0";
    private String name;
    private List<PrivateChannelApi> privateChannels = new ArrayList<>();

    public AddPrivateChannelsResponse() {

    }

    public AddPrivateChannelsResponse(List<PrivateChannelApi> privateChannelApis) {
        this.privateChannels = privateChannelApis;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrivateChannelApis(List<PrivateChannelApi> privateChannelApis) {
        this.privateChannels = privateChannelApis;
    }

    public List<PrivateChannelApi> getPrivateChannelApis() {
        return privateChannels;
    }
    @Override
    public String toString(){
        return "AddDeliveriesResponse{" +
                "version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", privateChannels=" + privateChannels +
                '}';
    }
}
