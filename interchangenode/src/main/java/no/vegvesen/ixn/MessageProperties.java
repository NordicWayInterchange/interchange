package no.vegvesen.ixn;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class MessageProperties {

    public final static String LAT = "lat";
    public final static String LON = "lon";
    public final static String WHAT = "what";
    public final static String WHERE = "where1";
    public final static String WHO = "who";
    public final static String USERID = "JMSXUserID";
    public final static String HOW = "how";
    public final static String WHEN = "when";

    private static Set<String> knownProperties = new LinkedHashSet<>(Arrays.asList(LAT, LON, WHAT, WHERE, WHO, USERID, HOW, WHEN));

    public static boolean isKnownProperty(String propertyName) {
        return knownProperties.contains(propertyName);
    }
}
