java -Djavax.net.ssl.keyStore=nw_bouvet.p12 -Djavax.net.ssl.keyStorePassword=<password> -Djavax.net.ssl.keyStoreType=pkcs12 -Djavax.net.ssl.trustStore=truststore.jks -Djavax.net.ssl.trustStorePassword=<password> -DPASSWORD=<user password>-jar target\debugclient-1.0-SNAPSHOT-jar-with-dependencies.jar