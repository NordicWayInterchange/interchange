package no.vegvesen.ixn.federation.messagecollector;

/*-
 * #%L
 * message-collector
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

import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.Neighbour;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.SSLContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
public class MessageCollectorRemoteListenerIT extends QpidDockerBaseIT {
    private static final SSLContext SSL_CONTEXT = TestKeystoreHelper.sslContext("jks/localhost.p12", "jks/truststore.jks");
    private static Logger logger = LoggerFactory.getLogger(MessageCollectorRemoteListenerIT.class);

    @SuppressWarnings("rawtypes")
	@Container
    public GenericContainer localContainer = getQpidContainer("docker/consumer", "jks", "my_ca.crt", "localhost.crt", "localhost.key");

    @SuppressWarnings("rawtypes")
	@Container
    public GenericContainer remoteContainer = getQpidContainer("docker/producer", "jks", "my_ca.crt", "localhost.crt", "localhost.key");

	@Test
    public void stoppingRemoteContainerStopsListener() {
		String remoteAmqpsUrl = String.format("amqps://localhost:%s", remoteContainer.getMappedPort(AMQPS_PORT));
		CollectorCreator collectorCreator = new CollectorCreator(SSL_CONTEXT, "localhost", localContainer.getMappedPort(AMQPS_PORT).toString(), "fedEx");
        Neighbour remote = mock(Neighbour.class);
        when(remote.getMessageChannelUrl()).thenReturn(remoteAmqpsUrl);
        MessageCollectorListener remoteForwardListener = collectorCreator.setupCollection(remote);
        remoteContainer.stop();

        assertThat(remoteForwardListener.isRunning()).isFalse();
    }

}
