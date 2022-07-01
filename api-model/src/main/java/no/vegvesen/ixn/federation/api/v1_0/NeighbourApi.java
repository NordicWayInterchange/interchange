package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static no.vegvesen.ixn.federation.api.v1_0.CapabilityApi.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true,
        property = "neighbour")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NeighbourApi {

    private String name;
    private Integer id;

    public NeighbourApi(String name, Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID can not be null");
        }
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID can not be null");
        }
        this.id = id;
    }

    @Override
    public String toString() {
        return "NeighbourApi{" +
                "name='" + name + '\'' +
                ", id=" + id +
                '}';
    }
}
