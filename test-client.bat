set USER=king_harald
set SERVER=localhost
set KEYSTORE_FILE=tmp\keys\%USER%.p12
set TRUSTSTORE_FILE=tmp\keys\truststore.jks
set PASSWORD=password
set CLIENT_JAR=debugclient\target\debugclient-1.0-SNAPSHOT-jar-with-dependencies.jar
set SERVER_URI=amqps://%SERVER%:5671
set SEND_QUEUE=onramp
set RECEIVE_QUEUE=%USER%

java -Djavax.net.ssl.keyStore=%KEYSTORE_FILE% -Djavax.net.ssl.keyStorePassword=%PASSWORD% -Djavax.net.ssl.keyStoreType=pkcs12 -Djavax.net.ssl.trustStore=%TRUSTSTORE_FILE% -Djavax.net.ssl.trustStorePassword=%PASSWORD% -DUSER=%USER% -jar %CLIENT_JAR% %SERVER_URI% %SEND_QUEUE% %RECEIVE_QUEUE%
