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

	int reconnectsProducer = 0;
	int reconnectsConsumer = 0;

	@Test
	public void localMessageGetsForwardedAfterServiceDiscovery() throws JMSException, NamingException, InterruptedException {
		Sink sinkSpTwo = new Sink("amqps://bouvet-two.bouvetinterchange.no", "sp-two.bouvetinterchange.no", spTwoSslContext);
		MessageConsumer consumer = sinkSpTwo.createConsumer();
		//noinspection StatementWithEmptyBody
		while(consumer.receive(200) != null); //drain out queue

		Source sourceSpOne = new Source("amqps://bouvet-one.bouvetinterchange.no", "onramp", spOneSslContext);
		sourceSpOne.start();
		LongStream.Builder latencyStatisticsBuilder = LongStream.builder();
		LongStream.Builder ttlOffsetBuilder = LongStream.builder();
		int numberOfMessagesToSend = 20000;
		for (int i = 0; i < numberOfMessagesToSend; i++) {
			long start = System.currentTimeMillis();
			long timeToLive = 600000L;
			sendOneMessageReconnect(sourceSpOne, i, timeToLive);
			long expectedExpiry = System.currentTimeMillis() + timeToLive;

			Message receive = null;
			try {
				receive = consumer.receive((5 * 60000));
			} catch (JMSException e) {
				consumer = reconnectConsumer(sinkSpTwo, consumer);
				receive = consumer.receive(60000);
			}
			if (receive == null) {
				System.out.println("ERROR: lost message " + i);
			} else {
				if (i % 100 == 0) {
					System.out.printf("*");
				}
				long expiryOffset = expectedExpiry - receive.getJMSExpiration();
				ttlOffsetBuilder.add(expiryOffset);
				latencyStatisticsBuilder.add(System.currentTimeMillis() - start);
			}
		}
		try {
			consumer.close();
		} catch (JMSException ignore) {
		}
		try {
			sinkSpTwo.close();
		} catch (Exception ignore) {
		}
		sourceSpOne.close();


		LongStream latencies = latencyStatisticsBuilder.build();
		LongSummaryStatistics stats = latencies.summaryStatistics();
		System.out.println("latencies " + stats);
		System.out.println("ttlOffsets " + ttlOffsetBuilder.build().summaryStatistics());
		System.out.printf("Number of reconnects producer %d, consumer %d %n", this.reconnectsProducer, this.reconnectsConsumer);
		long numberOfMessagesReceived = latencies.summaryStatistics().getCount();
		System.out.printf("Number of messages received %d, lost %d %n", numberOfMessagesReceived, numberOfMessagesToSend - numberOfMessagesReceived);
	}

	private MessageConsumer reconnectConsumer(Sink sinkSpTwo, MessageConsumer consumer) {
		System.out.println("Reconnecting to sp-two queue in 30 seconds");
		try {
			consumer.close();
			consumer = null;
		} catch (JMSException ignore) {
		}
		for (int y=0; y < 10 && consumer == null; y++) {
			try {
				Thread.sleep(30000);
				System.out.printf("Reconnect attempt %d %n", y);
				consumer = sinkSpTwo.createConsumer();
			} catch (InterruptedException | NamingException | JMSException ex) {
				System.out.println("Reconnect consumer " + ex.getMessage());
			}
		}
		if (consumer == null) {
			System.out.println("Could not reconnect to sp-two");
			System.exit(-1);
		}
		reconnectsConsumer++;
		System.out.println("Reconnected to sp-two queue");
		return consumer;
	}

	private void sendOneMessageReconnect(Source sourceSpOne, int i, long timeToLive) throws InterruptedException, NamingException, JMSException {
		try {
			sourceSpOne.send("beste fisken i verden - " + i, "NO", timeToLive);
		} catch (JMSException e) {
			sourceSpOne = reconnectProducer(sourceSpOne);
			sourceSpOne.send("beste fisken i verden (resend) - " + i, "NO", timeToLive);
		}
	}

	private Source reconnectProducer(Source sourceSpOne) throws InterruptedException, NamingException, JMSException {
		System.out.println("Reconnecting to onramp in 30 seconds");
		try {
			sourceSpOne.close();
		} catch (Exception ignore) {
		}
		Thread.sleep(30000);
		for (int y=0; y < 10 && !sourceSpOne.isConnected(); y++) {
			System.out.printf("Reconnect source attempt %d %n", y);
			sourceSpOne.start();
		}
		if (!sourceSpOne.isConnected()) {
			System.out.println("Could not reconnect to sp-one");
			System.exit(-2);
		}
		reconnectsProducer++;
		return sourceSpOne;
	}


}
