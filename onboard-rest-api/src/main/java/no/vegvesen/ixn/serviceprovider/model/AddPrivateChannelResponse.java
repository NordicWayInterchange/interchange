package no.vegvesen.ixn.serviceprovider.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddPrivateChannelResponse {

    private String version = "1.0";

    private String name;

    private List<PrivateChannelResponseApi> privateChannels;

    public AddPrivateChannelResponse() {
        this.privateChannels = new ArrayList<>();
    }

    public AddPrivateChannelResponse(String name) {
        this.name = name;
        this.privateChannels = new ArrayList<>();
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

    public void setPrivateChannelApis(List<PrivateChannelResponseApi> privateChannelResponseApis) {
        this.privateChannels = privateChannelResponseApis;
    }

    public List<PrivateChannelResponseApi> getPrivateChannels() {
        return privateChannels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddPrivateChannelResponse response = (AddPrivateChannelResponse) o;
        return Objects.equals(version, response.version) && Objects.equals(name, response.name) && Objects.equals(privateChannels, response.privateChannels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, name, privateChannels);
    }

    @Override
    public String toString(){
        return "AddPrivateChannelResponse{" +
                "version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", privateChannels=" + privateChannels +
                '}';
    }
}
