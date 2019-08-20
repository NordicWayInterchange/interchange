package no.vegvesen.ixn.model;

import no.vegvesen.ixn.MessageProperties;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.*;

import static no.vegvesen.ixn.MessageProperties.*;

public class IxnMessage {
    final static long DEFAULT_TTL = 86_400_000L;
    final static long MAX_TTL = 6_911_200_000L;
    private final String who;
    private final String userID;
    private long expiration;
    private final double lat;
    private final double lon;
    private final List<String> what;
    private final String body;
    private List<String> countries = new ArrayList<>();
    private String how;
    private String when;
    private Map<String, String> otherStringAttributes = new HashMap<>();

    public IxnMessage(TextMessage textMessage) throws JMSException {
        this(textMessage.getStringProperty(WHO),
                textMessage.getStringProperty(USERID),
                textMessage.getJMSExpiration(),
                textMessage.getDoubleProperty(LAT),
                textMessage.getDoubleProperty(LON),
                parseWhat(textMessage.getStringProperty(WHAT)),
                textMessage.getText());
        String how = textMessage.getStringProperty(HOW);
        if (how != null) {
            this.how = how;
        }
        String when = textMessage.getStringProperty(WHEN);
        if (when != null) {
            this.when = when;
        }

        Enumeration propertyNames = textMessage.getPropertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = (String) propertyNames.nextElement();
            String propertyValue = textMessage.getStringProperty(propertyName);
            if (!MessageProperties.isKnownProperty(propertyName) && propertyValue != null) {
                this.addOtherStringAttribute(propertyName, propertyValue);
            }
        }
    }

    public IxnMessage(String who, String userID, long expiration, double lat, double lon, List<String> what, String body){
        this.who = who;
        this.userID = userID;
        this.lat = lat;
        this.lon = lon;
        this.body = body;
        this.what = what;
        this.expiration = checkExpiration(expiration, System.currentTimeMillis());
    }

    public String getWho() {
        return who;
    }

    public String getUserID() {
        return userID;
    }

    public long getExpiration(){
        return expiration;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
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

    public static List<String> parseWhat(String what){
        return Arrays.asList(what.split("\\s*,\\s*"));
    }

    static long checkExpiration(long expiration, long currentTime){
        if(expiration <= 0){
            // expiration is absent or illegal - setting to default ttl (1 day)
            return (DEFAULT_TTL + currentTime);
        }else if(expiration > (MAX_TTL + currentTime)){
            // expiration is too high, setting to maximum ttl (8 days)
            return (MAX_TTL + currentTime);
        }else{
            // expiration is in the valid range (more than 0, less than 8 days)
            return expiration;
        }
    }

    public String getHow() {
        return how;
    }

    public String getWhen() {
        return when;
    }

    public Map<String, String> getOtherStringAttributes() {
        return this.otherStringAttributes;
    }

    public void addOtherStringAttribute(String name, String value) {
        this.otherStringAttributes.put(name, value);
    }
}
