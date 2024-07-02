package no.vegvesen.ixn.napcore.model;

import java.util.List;

public record CertificateSignResponse(List<String> chain) {

    @Override
    public String toString() {
        return "CertificateSignResponse{" +
                "chain='" + chain + '\'' +
                '}';
    }
}
