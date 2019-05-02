package no.vegvesen.ixn.federation.forwarding;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MessageForwarderIT {


    @Autowired
    MessageForwarder forwarder;

    @Test
    public void testFoo() throws JMSException, NamingException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        forwarder.setupConnectionsToNewNeighbours();
    }

}
