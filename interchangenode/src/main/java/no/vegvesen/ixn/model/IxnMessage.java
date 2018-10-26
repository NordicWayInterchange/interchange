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
        this(textMessage.getStringProperty(WHO),
                textMessage.getStringProperty(USERID),
                textMessage.getJMSExpiration(),
                textMessage.getFloatProperty(LAT),
                textMessage.getFloatProperty(LON),
                parseWhat(textMessage.getStringProperty(WHAT)),
                textMessage.getText());
    }

    public IxnMessage(String who, String userID, long ttl, float lat, float lon, List<String> what, String body){
        this.who = who;
        this.userID = userID;
        this.lat = lat;
        this.lon = lon;
        this.body = body;
        this.what = what;
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

    public boolean hasWhat(){
        return (what.size() != 0);
    }

    public void setCountries (List<String> countries){
        this.countries = countries;
    }

    public void setTtl(long ttl){
        this.ttl = checkTtl(ttl);
    }

    public static List<String> parseWhat(String what){
        return Arrays.asList(what.split("\\s*,\\s*"));
    }

    public static long checkTtl(long ttl){
        if(ttl <= 0){
            // ttl is absent or illegal - setting to default ttl (1 day)
            return 86_400_000l;
        }else if(ttl > 6_911_200_000l){
            // ttl is too high, setting to maximum ttl (8 days)
            return 6_911_200_000l;
        }else{
            // ttl is in the valid range (more than 0, less than 8 days)
            return ttl;
        }
    }

}
