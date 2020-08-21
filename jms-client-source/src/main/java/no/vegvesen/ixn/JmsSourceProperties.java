package no.vegvesen.ixn;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix ="source")
public class JmsSourceProperties {

    public JmsSourceProperties() {}

    /**
     * The amqps URL to connect to
     */
    private String url;

    /**
     * The name of the queue to send to
     */
    private String sendQueue;

    /**
     * Path of the key store
     */
    private String keystorePath;

    /**
     * Key store password
     */
    private String keystorePass;

    /**
     * Password for the key
     */
    private String keyPass;

    /**
     * Path of the trust store
     */
    private  String trustStorepath;

    /**
     * Trust store password
     */
    private String trustStorepass;

    public String getUrl() {
        return url;
    }

    public String getSendQueue() {
        return sendQueue;
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

    public String getTrustStorepath() {
        return trustStorepath;
    }

    public String getTrustStorepass() {
        return trustStorepass;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSendQueue(String sendQueue) {
        this.sendQueue = sendQueue;
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

    public void setTrustStorepath(String trustStorepath) {
        this.trustStorepath = trustStorepath;
    }

    public void setTrustStorepass(String trustStorepass) {
        this.trustStorepass = trustStorepass;
    }
}
