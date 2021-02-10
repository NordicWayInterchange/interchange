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

}
