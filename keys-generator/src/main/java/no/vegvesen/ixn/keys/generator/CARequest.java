package no.vegvesen.ixn.keys.generator;

import java.util.List;

/**
 * A CA request represents the intent to create certificates and keys for entities in a CA tree.
 * A CA can issue subCA's, host keypairs and client (Service Provider) keypairs, this making
 * a tree of CA's and sub CA's.
 * <p />
 * This structure makes it easier to test and debug certificate structures and situations that involves
 * cert structure.
 *
 *
 * @param name Name of the CA
 * @param country CA country
 * @param subCaRequests Further CA's issued from this CA
 * @param hostRequests Host certificates and keys issued under this CA
 * @param clientRequests Client certificates under this CA
 */
public record CARequest(String name, String country, List<CARequest> subCaRequests, List<HostRequest> hostRequests, List<ClientRequest> clientRequests) {
}
