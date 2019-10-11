RootCA:
	run makeRootCA.sh
	
	get CSR from nodeCA
	run signCSR.sh
	send back ca/certs/* and ca/intermediate/certs/*