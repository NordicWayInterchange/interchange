package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.model.capability.CamApplication;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationEqualsAndHashCodeTest {

    @Test
    public void testCam() {
        CamApplication app1 = new CamApplication(
                "pub-1",
                "pub-1-1",
                "no",
                "1.0",
                Set.of()
        );
        CamApplication app2 = new CamApplication(
                "pub2",
                "pub-1-1",
                "no",
                "1.0",
                Set.of()
        );
        assertThat(app1).isNotEqualTo(app2);
        CamApplication app3 = new CamApplication(
                "pub-1",
                "pub-1-1",
                "no",
                "1.0",
                Set.of()
        );
        assertThat(app1).isEqualTo(app3);
        assertThat(app3).isEqualTo(app1);

    }

}
