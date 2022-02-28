package no.vegvesen.ixn.federation.model;

import javax.persistence.*;

@Entity
@Table(name = "outgoing_matches")
public class OutgoingMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "out_matches_seq")
    private Integer id;

    @Enumerated(EnumType.STRING)
    private OutgoingMatchStatus status;

    private String serviceProviderName;

    public OutgoingMatch() {

    }
}
