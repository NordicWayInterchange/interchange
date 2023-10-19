package no.vegvesen.ixn.serviceprovider.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddPrivateChannelsResponse response = (AddPrivateChannelsResponse) o;
        return Objects.equals(version, response.version) && Objects.equals(name, response.name) && Objects.equals(privateChannels, response.privateChannels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, name, privateChannels);
    }
    @Override
    public String toString(){
        return "AddPrivateChannelsResponse{" +
                "version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", privateChannels=" + privateChannels +
                '}';
    }
}
