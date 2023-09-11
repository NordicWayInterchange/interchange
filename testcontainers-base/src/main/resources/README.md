# Keymaster

The keymaster is responsible for generating keys and certs for CA, signing (and possibly issue) 
keys for Interchanges, as well as signing CSRs for Service Providers. 
This implementation is for testing purposes. A production-ready solution would typically
use a cloud provider's DNS, CA and key management services, secured with IAM restrictions and encryption on the stored 
data and execution.

The keymaster is a set of containers that each runs one of the following stages:
 * Generate root CA keys
 * Generate Interchange node CA keys plus certificate chain
 * Generate CSR from Interchange Node CA keys for root CA to sign 
 * Sign a CSR from an Interchange Node CA, creating a signed certificate for Interchange Node CA with trust chain.

Input and output folders are expected to be exposed to the container at given filesystem paths, specified for each 
container.

### TODO 
 * Are there other files we should keep track of? index.txt-files are somehow used as database of issued keys.


