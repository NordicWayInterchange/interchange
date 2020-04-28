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

public class FederationSystemST {

	SSLContext spOneSslContext = TestKeystoreHelper.sslContext("jks/sp-one.p12", "jks/truststore.jks");
	SSLContext spTwoSslContext = TestKeystoreHelper.sslContext("jks/sp-two.p12", "jks/truststore.jks");

	@Test
	public void localMessageGetsForwardedAfterServiceDiscovery() throws JMSException, NamingException, InterruptedException {
		Sink sinkSpTwo = new Sink("amqps://bouvet-two.bouvetinterchange.no", "sp-two.bouvetinterchange.no", spTwoSslContext);
		MessageConsumer consumer = sinkSpTwo.createConsumer();
		//noinspection StatementWithEmptyBody
		while(consumer.receive(200) != null); //drain out queue

		Source sourceSpOne = new Source("amqps://bouvet-one.bouvetinterchange.no", "onramp", spOneSslContext);
		sourceSpOne.start();
		LongStream.Builder latencyStatisticsBuilder = LongStream.builder();
		for (int i = 0; i < 2000; i++) {
			long start = System.currentTimeMillis();
			long timeToLive = 60000L;
			try {
				sourceSpOne.send("beste fisken i verden - " + i, "NO", timeToLive);
			} catch (JMSException e) {
				System.out.println("Reconnecting to onramp in 30 seconds");
				Thread.sleep(30000);
				sourceSpOne.start();
				sourceSpOne.send("beste fisken i verden (resend) - " + i, "NO", timeToLive);
			}
			long expectedExpiry = System.currentTimeMillis() + timeToLive;

			Message receive = null;
			try {
				receive = consumer.receive((5 * 60000));
			} catch (JMSException e) {
				System.out.println("Reconnecting to sp-two queue in 30 seconds");
				Thread.sleep(30000);
				consumer = sinkSpTwo.createConsumer();
				System.out.println("Reconnected to sp-two queue");
				receive = consumer.receive(60000);
			}
			if (receive == null) {
				System.out.println("ERROR: lost message " + i);
			} else {
				System.out.printf("Got message %d: %s with expiration %d vs expected %d (%d) %n",
						i,
						receive.getBody(String.class),
						receive.getJMSExpiration(),
						expectedExpiry,
						expectedExpiry - receive.getJMSExpiration());
				latencyStatisticsBuilder.add(System.currentTimeMillis() - start);
			}
		}
		LongStream latencies = latencyStatisticsBuilder.build();
		LongSummaryStatistics stats = latencies.summaryStatistics();

		System.out.println("max " + stats.getMax());
		System.out.println("min " + stats.getMin());
		System.out.println("avg " + stats.getAverage());

		System.out.println("latencies " + stats);
	}
}
