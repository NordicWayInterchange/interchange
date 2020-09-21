# key-gen

TLS key generation for use in development using a common CA that is being stored in truststore.jks.
Each generated user or server certificates is stored in separate .p12-files.

Passwords for the truststore and keystores is "password".

##generate-keys.sh
The generate-keys.sh script can be run separately in order to test the shell script alone.

####CA_CN
default value: _my_ca_
####KEY_CNS
default value: _localhost king_gustaf king_harald_

The script default values is used for all parameters except the folder where keys will be stored, 
so a test can be run by executing the script like this:
```
$ mkdir /tmp/keys
$ ./generate-keys.sh /tmp/keys
```
will produce these files
```
ls /tmp/keys
king_gustaf.crt
king_gustaf.key
king_gustaf.p12
king_harald.crt
king_harald.key
king_harald.p12
localhost.crt
localhost.key
localhost.p12
my_ca.crt
my_ca.key
my_ca.srl
truststore.jks
```

##Docker container
To generate keys by running key-gen as a docker container follow these steps: 
```
mkdir /tmp/keys
docker build . -t key-gen
docker run -it -e CA_CN=my-own-new-ca -e KEY_CNS="my-new-host-name" -v /tmp/keys/:/jks/keys key-gen:latest
```