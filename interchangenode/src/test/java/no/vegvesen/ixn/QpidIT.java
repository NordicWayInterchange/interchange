package no.vegvesen.ixn;

import no.vegvesen.ixn.messaging.IxnMessageProducer;
import no.vegvesen.ixn.model.IxnMessage;
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
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;

@RunWith(SpringRunner.class)
@SpringBootTest
public class QpidIT {

    @Autowired
    IxnMessageProducer producer;

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


    public void sendMessageOneCountry(String queueName){

        long systemTime = System.currentTimeMillis();
        long timeToLive = 3_600_000; // 5 hrs
        long expiration = systemTime+timeToLive;

        IxnMessage message = new IxnMessage(
                "The great traffic testers",
                "quest",
                expiration,
                63.0f,
                10.0f,
                Collections.singletonList("Obstruction"),
                "Testing the sending of one message");
        message.setCountries(Collections.singletonList("NO"));
        producer.sendMessage(queueName, message);
    }

    public void sendMessageTwoCountries(String queueName){
        long systemTime = System.currentTimeMillis();
        long timeToLive = 3_600_000;
        long expiration = systemTime+timeToLive;

        IxnMessage message = new IxnMessage(
                "The great traffic testers",
                "quest",
                expiration,
                59.09f,
                11.25f,
                Collections.singletonList("Obstruction"),
                "Testing splitting of messages");
        message.setCountries(Arrays.asList("NO", "SE"));
        producer.sendMessage(queueName, message);

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
        clearQueue("test-out");
        clearQueue("dlqueue");
        Thread.sleep(2*1000);
    }

    @Test
    public void checkIfMessageSentToDlqueueGivesCorrectQueueDepthInQueue() throws Exception{
        // Send one message to the queue
        sendMessageOneCountry("onramp");
        Thread.sleep(2*1000);

        // Expecting 1 message on the queue if 1 message is sent.
        Assert.assertEquals(1, checkQueueDepth("test-out"));
    }


    /*
    Denne testen klarer av en eller annen rar grunn å spinne opp to instanser av IxnMessageProducer,
    noe som resulterer i fire meldinger sendt til test-out i stedet for to. Disse testene burde endres
    til å inkludere en refaktorert debug klient.
     */
    @Test
    public void checkDulicationOfMessageWithTwoCountries() throws Exception{
        sendMessageTwoCountries("onramp");
        Thread.sleep(2*1000);

        Assert.assertEquals(2, checkQueueDepth("test-out") );
    }



}
