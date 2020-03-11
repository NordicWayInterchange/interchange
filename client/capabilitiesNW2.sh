curl \
 --key <service-provider>.key.pem \
 --cert chain.<service-provider>.crt.pem \
 --cacert ca.rootCA.crt.pem \
 --header 'Content-Type:application/json' \
 --data '{"name":"<service-provider>", "capabilities":[{ "where": "DK", "how": "datex2;2.3" }] }' \
  https://bouveta-fed.itsinterchange.eu:4141/capabilities
