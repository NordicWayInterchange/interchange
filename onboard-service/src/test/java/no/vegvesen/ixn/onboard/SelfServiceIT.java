package no.vegvesen.ixn.onboard;


import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import javax.transaction.Transactional;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Transactional
public class SelfServiceIT {

    @Autowired
    private SelfService selfService;

    @Test
    public void foo() {

    }
}
