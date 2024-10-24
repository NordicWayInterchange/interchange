package no.vegvesen.ixn.serviceprovider.model;

import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilityApi;

import java.util.Objects;

public class LocalActorCapability {
    private String id;


    private String path;
    CapabilityApi definition;

    public LocalActorCapability() {
    }

    public LocalActorCapability(String id, String path, CapabilityApi definition) {
        this.id = id;
        this.path = path;
        this.definition = definition;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public CapabilityApi getDefinition() {
        return definition;
    }

    public void setDefinition(CapabilityApi definition) {
        this.definition = definition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalActorCapability that = (LocalActorCapability) o;
        return Objects.equals(id, that.id) && Objects.equals(path, that.path) && Objects.equals(definition, that.definition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, path, definition);
    }

    @Override
    public String toString() {
        return "LocalActorCapability{" +
                "id='" + id + '\'' +
                ", path='" + path + '\'' +
                ", definition=" + definition +
                '}';
    }
}
