package no.vegvesen.ixn.federation.adminserver;

import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class MockSslBundle {
        @Bean
        public SslBundles sslBundles() {
            SslBundles mockSslBundles = mock(SslBundles.class);
            SslBundle mockSslBundle = mock(SslBundle.class);
            when(mockSslBundles.getBundle(anyString())).thenReturn(mockSslBundle);
            return mockSslBundles;
        }

}
