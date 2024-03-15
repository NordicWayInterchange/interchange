package no.vegvesen.ixn.federation.server;


import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@SpringBootTest
public class NeighbourRestControllerIT {

    @Autowired
    NeighbourRestController neighbourRestController;

    @Autowired
    WebApplicationContext context;
    @Test
    public void genSwagger() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andDo((result -> {
                    System.out.println(result.getResponse().getContentAsString());
                    Files.deleteIfExists(Paths.get("target/swagger/swagger.json"));
                    Files.createDirectories(Paths.get("target/swagger"));
                    try(FileWriter fileWriter = new FileWriter("target/swagger/swagger.json")){
                        fileWriter.write(result.getResponse().getContentAsString());
                    }

                }));
    }
}
