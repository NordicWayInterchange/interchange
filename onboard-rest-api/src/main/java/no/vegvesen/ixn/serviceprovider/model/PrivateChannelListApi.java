package no.vegvesen.ixn.serviceprovider.model;

import java.util.ArrayList;
import java.util.List;

public class PrivateChannelListApi {
    private List<PrivateChannelApi> privateChannelApis = new ArrayList<>();

    public PrivateChannelListApi() {

    }

    public PrivateChannelListApi(List<PrivateChannelApi> privateChannelApis) {
        this.privateChannelApis = privateChannelApis;
    }

    public List<PrivateChannelApi> getPrivateChannelApis() {
        return privateChannelApis;
    }
}
