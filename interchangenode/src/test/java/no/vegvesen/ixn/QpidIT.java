package no.vegvesen.ixn;

import no.vegvesen.ixn.messaging.TestOnrampMessageProducer;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

@RunWith(SpringRunner.class)
@SpringBootTest
public class QpidIT {


    @Autowired
    TestOnrampMessageProducer producer;

    // These test require HTTP basic authentication to be enabled in the qpid broker
    // and that the qpid port 8080 is bound to localhost 8080.

    public void clearQueue(String queueName) throws Exception{
        // Emptying the queue
        String url = "http://localhost:8080/api/latest/queue/default/default/" + queueName + "/clearQueue";
        URL sourceURL = new URL(url);
        HttpURLConnection source = (HttpURLConnection) sourceURL.openConnection();

        // HTTP Basic Authentication.
        String userpass = "admin:admin";
        String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
        source.setRequestProperty ("Authorization", basicAuth);

        source.setRequestMethod("POST");
        source.setRequestProperty("User-Agent", "Java client");
        source.setDoOutput(true);
        source.setRequestProperty("Content-Type", "application/json");

        // Empty json object - toString gives {}
        JSONObject emptyJson = new JSONObject();

        DataOutputStream wr = new DataOutputStream(source.getOutputStream());
        wr.writeBytes(emptyJson.toString());
        wr.close();


        // Necessary to update the queues
        InputStream is;

        try {
            is = source.getInputStream();
        } catch (IOException ioe) {
            if (source instanceof HttpURLConnection) {
                HttpURLConnection httpConn = (HttpURLConnection) source;
                int statusCode = httpConn.getResponseCode();
                if (statusCode != 200) {
                    is = httpConn.getErrorStream();
                }
            }
        }
    }

    public void sendMessageOneCountry(){
        long systemTime = System.currentTimeMillis();
        long timeToLive = 3_600_000; // 5 hrs
        long expiration = systemTime+timeToLive;

        producer.sendMessage(63.0f,
                10.0f,
                "Statens Vegvesen",
                "1234",
                "Obstruction",
                "Message with one country",
                expiration);
    }

    public void sendMessageTwoCountries(){
        long systemTime = System.currentTimeMillis();
        long timeToLive = 3_600_000;
        long expiration = systemTime+timeToLive;

        producer.sendMessage(59.09f,
                11.25f,
                "Statens Vegvesen",
                "1234",
                "Obstruction",
                "Message with two countries",
                expiration);
    }

    public void sendBadMessage(){
        long systemTime = System.currentTimeMillis();
        long timeToLive = 3_600_000;
        long expiration = systemTime+timeToLive;

        // Missing 'who' gives invalid message.
        producer.sendMessage(58f,
                11f,
                null,
                "1234",
                "Obstruction",
                "Message with two countries",
                expiration);
    }


    public int checkQueueDepth(String queueName) throws Exception{

        // HOWTO: Using a variable in a string
        // Query the qpid REST api for queue depth
        String url = ("http://localhost:8080/api/latest/queue/default/default/" + queueName);
        URL sourceUrl = new URL(url);
        HttpURLConnection source = (HttpURLConnection) sourceUrl.openConnection();

        // HTTP Basic Authentication.
        String userpass = "admin:admin";
        String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
        source.setRequestProperty ("Authorization", basicAuth);

        // Getting the response and closing the connection.
        BufferedReader in = new BufferedReader(new InputStreamReader(source.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputline;
        while((inputline = in.readLine()) != null){
            response.append(inputline);
        }

        JSONObject json = new JSONObject(response.toString());
        JSONObject statistics = json.getJSONObject("statistics");

        System.out.println("JSON statistics: " + statistics.toString());

        int queueDepth = statistics.getInt("queueDepthMessages");

        return queueDepth;
    }

    @Before
    public void before()throws Exception{
        clearQueue("NO-out");
        clearQueue("NO-Obstruction");
        clearQueue("SE-out");
        clearQueue("dlqueue");
        Thread.sleep(2*1000);
    }

    @Test
    public void messageToNorwayGoesToNorwayQueue() throws Exception{
        sendMessageOneCountry(); // NO
        Thread.sleep(2*1000);
        // The queue should have one message
        Assert.assertEquals(1, checkQueueDepth("NO-out"));
    }


    @Test
    public void messageWithTwoCountriesGoesToTwoQueues() throws Exception{
        sendMessageTwoCountries(); // NO and SE
        Thread.sleep(2*1000);
        // Each queue should have one message
        Assert.assertEquals(1, checkQueueDepth("NO-out"));
        Assert.assertEquals(1, checkQueueDepth("SE-out"));
    }

    @Test
    public void messageWithCountryAndSituationGoesToRightQueue() throws Exception{
        sendMessageOneCountry(); // NO and Obstruction
        Thread.sleep(2*1000);
        Assert.assertEquals(1, checkQueueDepth("NO-Obstruction"));
    }

    @Test
    public void badMessageGoesDoDeadLetterQueue() throws Exception{
        sendBadMessage();
        Thread.sleep(2*1000);
        // Expecting one message on dlqueue because message is invalid.
        Assert.assertEquals(1, checkQueueDepth("dlqueue"));
    }
}
