package no.vegvesen.ixn.napcore.model;

import java.util.List;

public class CertificateSignResponse {

    /*
    PEM encoded certificate chain
     */
    List<String> certificates;

    public CertificateSignResponse() {

    }

    public CertificateSignResponse(List<String> certificates) {
        this.certificates = certificates;
    }

    public List<String> getChain() {
        return certificates;
    }

    public void setChain(List<String> chain) {
        this.certificates = chain;
    }

    @Override
    public String toString() {
        return "CertificateSignResponse{" +
                "chain='" + certificates + '\'' +
                '}';
    }
}
