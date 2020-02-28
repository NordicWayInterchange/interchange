package no.vegvesen.ixn.federation;


import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.TestKeystoreHelper;
import org.junit.Test;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.util.LongSummaryStatistics;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

public class FederationSystemST {

	SSLContext spOneSslContext = TestKeystoreHelper.sslContext("jks/sp-one.p12", "jks/truststore.jks");
	SSLContext spTwoSslContext = TestKeystoreHelper.sslContext("jks/sp-two.p12", "jks/truststore.jks");

	@Test
	public void localMessageGetsForwardedAfterServiceDiscovery() throws JMSException, NamingException {
		Sink sinkSpTwo = new Sink("amqps://bouvet-two.bouvetinterchange.no", "sp-two.bouvetinterchange.no", spTwoSslContext);
		MessageConsumer consumer = sinkSpTwo.createConsumer();
		//noinspection StatementWithEmptyBody
		while(consumer.receive(200) != null); //drain out queue

		Source sourceSpOne = new Source("amqps://bouvet-one.bouvetinterchange.no", "onramp", spOneSslContext);
		sourceSpOne.start();
		LongStream.Builder latBuilder = LongStream.builder();
		for (int i = 0; i < 20; i++) {
			long start = System.currentTimeMillis();
			sourceSpOne.send("beste fisken i verden", "NO", null);

			Message receive = consumer.receive(2000);
			latBuilder.add(System.currentTimeMillis() - start);
			assertThat(receive).isNotNull();
		}
		LongStream latencies = latBuilder.build();
		LongSummaryStatistics stats = latencies.summaryStatistics();

		System.out.println("max " + stats.getMax());
		System.out.println("min " + stats.getMin());
		System.out.println("avg " + stats.getAverage());

		System.out.println("latencies " + stats);
	}
}
