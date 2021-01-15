package no.vegvesen.ixn;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sink")
public class JmsImageSinkProperties {
    /**
     * The AMQPS URL to connect to
     */
    private String url;

    /**
     * The name of the queue to read messages from
     */
    private String receiveQueue;

    /**
     * Path of the keystore
     */
    private String keystorePath;

    /**
     * Key store password
     */
    private String keystorePass;

    /**
     * Password for the private key
     */
    private String keyPass;

    /**
     * The path of the trust store
     */
    private String trustStorePath;

    /**
     * The password of the trust store
     */
    private String truststorePass;

    public String getUrl() {
        return url;
    }

    public String getReceiveQueue() {
        return receiveQueue;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public String getKeystorePass() {
        return keystorePass;
    }

    public String getKeyPass() {
        return keyPass;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public String getTruststorePass() {
        return truststorePass;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setReceiveQueue(String receiveQueue) {
        this.receiveQueue = receiveQueue;
    }

    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    public void setKeystorePass(String keystorePass) {
        this.keystorePass = keystorePass;
    }

    public void setKeyPass(String keyPass) {
        this.keyPass = keyPass;
    }

    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    public void setTruststorePass(String truststorePass) {
        this.truststorePass = truststorePass;
    }
}
