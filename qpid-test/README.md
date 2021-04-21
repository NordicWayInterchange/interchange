This is a module containing a test version of the Qpid container. 
it's identical to the production one, with two exceptions:
The initial configuration in the test image is set up so that it doesn't check that the FQDN of the node matches 
the name of the certificate. 
The configuration of the container is mapped using a config file for a single virtualhost, not the entire config
setup as is done in the final version.
This container is also wrapped in testcontainers by the QpidContainer class in testcontianers-base, this making it
easy to set up a custom configuration of a virtualhost before tests to avoid lengthy setup routines.
