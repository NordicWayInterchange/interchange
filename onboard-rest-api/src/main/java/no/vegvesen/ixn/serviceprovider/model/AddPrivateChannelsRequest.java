package no.vegvesen.ixn.serviceprovider.model;

import java.util.List;
import java.util.Objects;

public class AddPrivateChannelsRequest {
    private String version = "1.0";
    private String name;
    private List<PrivateChannelApi> privateChannels;

    public AddPrivateChannelsRequest() {
    }


    public AddPrivateChannelsRequest(List<PrivateChannelApi> privateChannels) {
        this.privateChannels = privateChannels;
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

    public List<PrivateChannelApi> getPrivateChannels() {
        return privateChannels;
    }

    public void setPrivateChannels(List<PrivateChannelApi> privateChannels) {
        this.privateChannels = privateChannels;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddPrivateChannelsRequest that = (AddPrivateChannelsRequest) o;
        return Objects.equals(version, that.version) && Objects.equals(name, that.name) && Objects.equals(privateChannels, that.privateChannels);
    }
    @Override
    public int hashCode() {
        return Objects.hash(version, name, privateChannels);
    }

    @Override
    public String toString(){
        return "AddPrivateChannelsRequest{" +
                "version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", privateChannels=" + privateChannels +
                '}';
    }
}
