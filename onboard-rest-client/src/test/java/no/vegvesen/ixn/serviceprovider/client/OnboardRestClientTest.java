package no.vegvesen.ixn.serviceprovider.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.serviceprovider.model.SelectorApi;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class OnboardRestClientTest {

    @Test
    void testObjectMapperReturnsRightApiObject() throws IOException {
        File file = new File("localSubscription_dat_no.json");

        ObjectMapper mapper = new ObjectMapper();
        SelectorApi subscription = mapper.readValue(file,SelectorApi.class);

        assertThat(subscription.isCreateNewQueue()).isTrue();
    }
}
