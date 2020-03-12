if [ "$#" -ne 1 ]; then
	echo "USAGE: $0 <ca domain name>"
	exit 1 
fi
keytool \
	-import \
	-trustcacerts \
	-file ca/certs/ca.${1}.crt.pem -keystore truststore.jks -storepass password -noprompt
