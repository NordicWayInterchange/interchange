# QPID

Docker packaging of Apache QPID's Broker-j app. By default exposes AMQP on port
5672 and an HTTP web interface on port 8080.

## running

To run the image with some queues and virtual hosts preconfigured, mount the
`dev/example-vhost.json` file as `/work/default/config/default.json`. This
will set the default virtual host's name to whatever is in the `.name` field in
the config file. This name MUST be set to the public address of the QPID
server/cluster, otherwise some clients (particularly the Java debug client used
in this project) will fail to connect. Otherwise you can manually set the vhost
parameter in your AMQP calls.

## file mounts
- `config.json` is mounted to `/work/` at build. Contains port, auth and vhost
  node declarations.
- `dev/example-vhost.json` or something similar should be mounted at runtime to
  `/work/default/config/default.json`. Contains queue and exchange config.

## development environment
Use the resources in the `dev` directory to spin up the server with example file
mounts. You'll be able to address the server with vhost and TLS CN
"qpid.test.io".

## TLS authorization
You can generate new sets of server side and user side keystores and truststores
using the instructions at
<https://github.com/NordicWayInterchange/interchange/wiki/Secure-communication>
