package no.vegvesen.ixn.serviceprovider.model;
import java.util.List;

public class ListPrivateChannelsResponse {
    private String version = "1.0";
    private String name;
    private List<PrivateChannelApi> privateChannels;

    public ListPrivateChannelsResponse() {
    }


    public ListPrivateChannelsResponse(String name, List<PrivateChannelApi> privateChannels) {
        this.name = name;
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
    public String toString(){
        return "ListPrivateChannelsRespone{" +
                "version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", privateChannels=" + privateChannels +
                '}';
    }
}
