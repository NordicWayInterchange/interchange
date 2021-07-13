import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.serviceprovider.model.SelectorApi;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SelectorApiTest {

    @Test
    public void someConstructorBasedTests() {
        assertThat(new SelectorApi("A",null).isCreateNewQueue()).isFalse();
        assertThat(new SelectorApi("B",false).isCreateNewQueue()).isFalse();
        assertThat(new SelectorApi("C",true).isCreateNewQueue()).isTrue();
    }

    @Test
    public void testGenerateSelectorApi() throws JsonProcessingException {
        SelectorApi api = new SelectorApi(
                "countryCode = 'SE' and messageType = 'DENM'",
                null
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(api));
    }
}
