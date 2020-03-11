curl \
 --key <service-provider>.key.pem \
 --cert chain.<service-provider>.crt.pem \
 --cacert ca.rootCA.crt.pem \
 --header 'Content-Type:application/json' \
 --data '{"name": "service-provider-name", "subscriptions": [{ "selector": "where = 'DK' and how like 'datex2%'" }]}' \
  https://bouveta-fed.itsinterchange.eu:4141/subscription
