package no.vegvesen.ixn.serviceprovider.model;

import java.util.List;
import java.util.Objects;

public class ListPrivateChannelPeers {

    private String version = "1.0";

    private String name;

    private List<PrivateChannelPeerApi> privateChannels;

    public ListPrivateChannelPeers() {
    }

    public ListPrivateChannelPeers(String name, List<PrivateChannelPeerApi> privateChannels) {
        this.name = name;
        this.privateChannels = privateChannels;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PrivateChannelPeerApi> getPrivateChannels() {
        return privateChannels;
    }

    public void setPrivateChannels(List<PrivateChannelPeerApi> privateChannels) {
        this.privateChannels = privateChannels;
    }

    @Override
    public int hashCode(){
        return Objects.hash(version, name, privateChannels);
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        ListPrivateChannelPeers that = (ListPrivateChannelPeers) o;
        return Objects.equals(name, that.name) && Objects.equals(version, that.version) && Objects.equals(privateChannels, that.privateChannels);
    }

    @Override
    public String toString(){
        return "ListPrivateChannelPeers{" +
                "version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", privateChannels=" + privateChannels +
                '}';
    }

}
