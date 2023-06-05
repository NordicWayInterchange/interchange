package no.vegvesen.ixn.napcore.model;

public class CertificateSignResponse {

    /*
    PEM encoded certificate chain
     */
    String chain;

    public CertificateSignResponse() {

    }

    public CertificateSignResponse(String chain) {
        this.chain = chain;
    }

    public String getChain() {
        return chain;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    @Override
    public String toString() {
        return "CertificateSignResponse{" +
                "chain='" + chain + '\'' +
                '}';
    }
}
