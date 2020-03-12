
NodeCA:
	run makeIntermediateCSR.sh (remember the DOMAINNAME you use. You can see it in the CSR filename)
	send ca/intermediate/csr/* to rootCA
	put rootCAcert in ca/certs/
	put other certs in ca/intermediate/certs/

	use makeServerCert.sh to make a certificate for the node
	use makeClientCert.sh to make a certificate for clients


