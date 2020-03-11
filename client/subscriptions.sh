curl \
 --key <service-provider>.key.pem \
 --cert chain.<service-provider>.crt.pem \
 --cacert ca.rootCA.crt.pem \
 --header 'Content-Type:application/json' \
 --data '{ "originatingCountry": "DK", "messageType": "DATEX2"}' \
  https://bouveta-fed.itsinterchange.eu:4141/<service-provider-name>/subscriptions
