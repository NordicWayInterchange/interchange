package no.vegvesen.ixn.napcore.model;

public class CertificateSignRequest {

    /*
    PEM encoded certificate signing request (CSR)
     */
    String csr;

    public CertificateSignRequest() {

    }

    public CertificateSignRequest(String csr) {
        this.csr = csr;
    }

    public String getCsr() {
        return csr;
    }

    public void setCsr(String csr) {
        this.csr = csr;
    }

    @Override
    public String toString() {
        return "CertificateSignRequest{" +
                "csr='" + csr + '\'' +
                '}';
    }
}
