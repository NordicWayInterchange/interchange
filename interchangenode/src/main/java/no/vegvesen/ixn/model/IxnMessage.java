package no.vegvesen.ixn.model;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static no.vegvesen.ixn.MessageProperties.*;

public class IxnMessage {
    private final String who;
    private final String userID;
    private long ttl;
    private final float lat;
    private final float lon;
    private final List<String> what;
    private final String body;
    private List<String> countries = new ArrayList();

    public IxnMessage(TextMessage textMessage) throws JMSException {

        // TODO: check if you get text message ttl with getLongProperty or something else
        // TODO: check the same with userID.


        this.who = textMessage.getStringProperty(WHO);
        this.userID = textMessage.getStringProperty(USERID);
        this.lat = textMessage.getFloatProperty(LAT);
        this.lon = textMessage.getFloatProperty(LON);
        this.body = textMessage.getText();

        String whatString = textMessage.getStringProperty(WHAT);
        this.what = Arrays.asList(whatString.split("\\s*,\\s*"));

        Long ttl = textMessage.getJMSExpiration();
        setTtl(ttl);

    }

    public String getWho() {
        return who;
    }

    public String getUserID() {
        return userID;
    }

    public long getTtl(){
        return ttl;
    }

    public float getLat() {
        return lat;
    }

    public float getLon() {
        return lon;
    }

    public List<String> getWhat() {
        return what;
    }
    public String getBody(){
        return body;
    }
    public List<String> getCountries(){
        return countries;
    }

    public boolean hasCountries(){
        return (countries.size() != 0);
    }

    public void setCountries (List<String> countries){
        this.countries = countries;
    }

    public void setTtl(long ttl){
        if(ttl <= 0){
            // ttl is absent or illegal - setting to default ttl (1 day)
            this.ttl = 86_400_000l;
        }else if(ttl > 6_911_200_000l){
            // ttl is too high, setting to maximum ttl (8 days)
            this.ttl = 6_911_200_000l;
        }else{
            // ttl is in the valid range (more than 0, less than 8 days)
            this.ttl = ttl;
        }
    }

}
