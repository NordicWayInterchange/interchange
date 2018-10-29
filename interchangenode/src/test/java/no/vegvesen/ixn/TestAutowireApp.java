package no.vegvesen.ixn;

import no.vegvesen.ixn.geo.GeoLookup;
import org.apache.qpid.jms.provider.amqp.AmqpProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Primary;

@SpringBootApplication(exclude = {AmqpProvider.class})
@Primary
public class TestAutowireApp {
	@Autowired
	GeoLookup geoLookup;

}
