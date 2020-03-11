curl \
 --key <service-provider>.key.pem \
 --cert chain.<service-provider>.crt.pem \
 --cacert ca.rootCA.crt.pem \
 --header 'Content-Type:application/json' \
 --data @subscriptions.json \
  https://bouveta-fed.itsinterchange.eu:4141/subscription
