package no.vegvesen.ixn.serviceprovider.model;

import java.util.Set;

public class AddServiceProvidersResponse {
    private Set<AddServiceProvidersApi> apis;

    public AddServiceProvidersResponse() {
    }

    public AddServiceProvidersResponse(Set<AddServiceProvidersApi> apis) {
        this.apis = apis;
    }

    public Set<AddServiceProvidersApi> getApis() {
        return apis;
    }

    public void setApis(Set<AddServiceProvidersApi> apis) {
        this.apis = apis;
    }

    public void addApi(AddServiceProvidersApi api) {
        this.apis.add(api);
    }
}
