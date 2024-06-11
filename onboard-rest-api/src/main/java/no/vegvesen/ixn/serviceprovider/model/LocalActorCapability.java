package no.vegvesen.ixn.serviceprovider.model;

import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitySplitApi;

import java.util.Objects;
import java.util.UUID;

public class LocalActorCapability {
    private UUID id;


    private String path;

    CapabilitySplitApi definition;

    public LocalActorCapability() {
    }

    public LocalActorCapability(UUID id, String path, CapabilitySplitApi definition) {
        this.id = id;
        this.path = path;
        this.definition = definition;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public CapabilitySplitApi getDefinition() {
        return definition;
    }

    public void setDefinition(CapabilitySplitApi definition) {
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
