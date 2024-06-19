# Key structure in the Interchange network

The interchange network uses a custom Certificate Authority (CA), not the standard CA's 
that you typically use for web pages and in browsers. This is due to the use of mutual TLS (mTLS)
between interchanges, and between end-users and their interchange.

## Key Structure

A root CA is maintained by the owners of the project, and issues a sub-CA for each of the interchanges
that is connected. The owner is typically the same as the owner of the DNS lookup entry.

Each interchange issues host certificate(s) for its node, and client certificates for the users (service providers).

In addition, we have a separate, private CA for the internal services in the interchange.
