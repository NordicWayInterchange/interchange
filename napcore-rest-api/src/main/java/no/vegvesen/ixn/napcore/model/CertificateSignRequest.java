package no.vegvesen.ixn.napcore.model;

public record CertificateSignRequest(String csr) {

    @Override
    public String toString() {
        return "CertificateSignRequest{" +
                "csr='" + csr + '\'' +
                '}';
    }
}
