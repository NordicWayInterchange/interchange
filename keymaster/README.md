#Keymaster

The keymaster i s responsible for generating keys and certs for CA, signing (and possibly issue) 
keys for Interchanges, as well as signing CSRs for Service Providers.

It's a container (or a series of containers) that runs each of the stages.

###Generate CA keys
The container persists the keys to a volume bound to the ca_keys subfolder in this folder. 
If this does not exist, you need to make ift before running the container.
Typical docker run:

``
docker run -it -v "$(pwd)"/ca_keys:/ca_keys --rm keymaster test.no NO
``

This will create files
```
ca/ca.test.no.crt.pem
ca/ca.test.no.key.pem
```
under the ca_keys folder
