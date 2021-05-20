package no.vegvesen.ixn.federation.qpid;

import java.util.*;

public class AclRule {

    private static String ALL = "ALL";

    private String permission;
    private String userOrGroupName;
    private String action;
    private String object;
    private Map<String,String> properties;


    public AclRule(String ruleString) {
        properties = new HashMap<>();
        parse(ruleString);
    }

    public AclRule(String permission,String userOrGroupName, String action, String object, Map<String,String> properties) {
        this.permission = permission;
        this.userOrGroupName = userOrGroupName;
        this.action = action;
        this.object = object;
        this.properties = properties;
    }

    public String getPermission() {
        return permission;
    }

    public String getUserOrGroup() {
        return userOrGroupName;
    }

    public String getAction() {
        return action;
    }

    public String getObject() {
        return object;
    }

    public Map<String,String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public boolean isQueueWriteRule() {
        return action.equals("PUBLISH") && object.equals("EXCHANGE");
    }

    public String getQueueName() {
        if (isQueueWriteRule()) {
            return properties.get("routingkey");
        } else {
            return properties.get("name");

        }
    }

    private void parse(String ruleString) {
       String[] tokens = ruleString.split(" ");
       //first token should be "ACL"
       permission = tokens[1];
       userOrGroupName = tokens[2];
       action = tokens[3];
       object = tokens[4];

       String[] propertyTokens = Arrays.copyOfRange(tokens,5,tokens.length);
       for (int i = 0; i < (propertyTokens.length - 2); i = i + 3) {
           String propertyName = propertyTokens[i].replace("\"","");
           //i + 1 should be '='
           String tokenName = propertyTokens[i + 2].replace("\"","");
           properties.put(propertyName,tokenName);
       }
    }

    public String toRuleString() {
        StringBuilder builder = new StringBuilder("ACL")
                .append(" ")
                .append(permission)
                .append(" ")
                .append(userOrGroupName)
                .append(" ")
                .append(action)
                .append(" ")
                .append(object);
        for (String key : properties.keySet()) {
            builder.append(" ")
                    .append(key)
                    .append(" = ")
                    .append("\"")
                    .append(properties.get(key))
                    .append("\"");
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return toRuleString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AclRule aclRule = (AclRule) o;
        return Objects.equals(permission, aclRule.permission) && Objects.equals(userOrGroupName, aclRule.userOrGroupName) && Objects.equals(action, aclRule.action) && Objects.equals(object, aclRule.object) && Objects.equals(properties, aclRule.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(permission, userOrGroupName, action, object, properties);
    }
}
