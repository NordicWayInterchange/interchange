package no.vegvesen.ixn.federation;

/*-
 * #%L
 * federation-st
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.TestKeystoreHelper;
import org.junit.jupiter.api.Test;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.util.LongSummaryStatistics;
import java.util.Random;
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
		while (consumer.receive(200) != null) ; //drain out queue

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

			Message receive;
			int timeoutOneMinute = 60000;
			try {
				receive = consumer.receive(timeoutOneMinute);
			} catch (JMSException e) {
				consumer = reconnectConsumer(sinkSpTwo, consumer);
				receive = consumer.receive(timeoutOneMinute);
			}
			if (receive == null) {
				System.out.println("ERROR: lost message " + i);
			} else {
				if (i % 1000 == 0) {
					System.out.println("Received message number " + i);
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
		long numberOfMessagesReceived = stats.getCount();
		System.out.printf("Number of messages received %d, lost %d %n", numberOfMessagesReceived, numberOfMessagesToSend - numberOfMessagesReceived);
	}

	private MessageConsumer reconnectConsumer(Sink sinkSpTwo, MessageConsumer consumer) {
		System.out.println("Reconnecting to sp-two queue in 30 seconds");
		try {
			consumer.close();
			consumer = null;
		} catch (JMSException ignore) {
		}
		for (int y = 0; y < 10 && consumer == null; y++) {
			try {
				Thread.sleep(30000);
				System.out.printf("Reconnect attempt %d %n", y);
				consumer = sinkSpTwo.createConsumer();
			} catch (InterruptedException | NamingException | JMSException ex) {
				System.out.printf("Reconnect consumer attempt %d failed %s %n", y, ex.getMessage());
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

	private void sendOneMessageReconnect(Source sourceSpOne, int i, long timeToLive) throws InterruptedException, JMSException {
		String messageText = "message " + i + " " + randomString(1024 * 200);
		try {
			sourceSpOne.send(messageText, "NO", timeToLive);
		} catch (JMSException e) {
			reconnectProducer(sourceSpOne);
			sourceSpOne.send(messageText, "NO", timeToLive);
		}
	}

	private void reconnectProducer(Source sourceSpOne) throws InterruptedException{
		System.out.println("Reconnecting to onramp in 30 seconds");
		try {
			sourceSpOne.close();
		} catch (Exception ignore) {
		}
		for (int y = 0; y < 10 && !sourceSpOne.isConnected(); y++) {
			Thread.sleep(30000);
			try {
				System.out.printf("Reconnect source attempt %d %n", y);
				sourceSpOne.start();
			} catch (Exception e) {
				System.out.printf("Reconnect attempt %d failed: %s %n", y, e.getMessage());
			}
		}
		if (!sourceSpOne.isConnected()) {
			System.out.println("Could not reconnect to sp-one");
			System.exit(-2);
		}
		System.out.println("Reconnected to source");
		reconnectsProducer++;
	}

	public String randomString(int length) {
		int leftLimit = 48; // numeral '0'
		int rightLimit = 122; // letter 'z'
		Random random = new Random();
		return random.ints(leftLimit, rightLimit + 1)
				.limit(length)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
	}
}
