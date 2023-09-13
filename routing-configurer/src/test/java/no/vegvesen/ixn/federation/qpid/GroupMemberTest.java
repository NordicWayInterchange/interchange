package no.vegvesen.ixn.federation.qpid;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.type.CollectionType;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class GroupMemberTest {

    @Test
    public void readGroupMembersFromJson() throws IOException {
        Path inputFile = Paths.get("src","test","resources","sp-group.json");
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory typeFactory = mapper.getTypeFactory();

        CollectionType collectionType = typeFactory.constructCollectionType(
                List.class, GroupMember.class);

        List<GroupMember> groupMembers = mapper.readValue(inputFile.toFile(),collectionType);
        for (GroupMember groupMember : groupMembers) {
            System.out.println(groupMember.getName());
        }

    }
}
