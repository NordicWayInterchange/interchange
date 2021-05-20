package no.vegvesen.ixn.federation.qpid;

import java.util.*;

public class AclRule {

    private static String ALL = "ALL";

    private String permission;
    private String userOrGroupName;
    private String action;
    private String object;
    private Map<String,String> properties;

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

    public static AclRule parse(String ruleString) {
        String[] tokens = ruleString.split(" ");
        //first token should be "ACL"
        String permission = tokens[1];
        String userOrGroupName = tokens[2];
        String action = tokens[3];
        String object = tokens[4];

        Map<String, String> properties = new HashMap<>();
        String[] propertyTokens = Arrays.copyOfRange(tokens,5,tokens.length);
        for (int i = 0; i < (propertyTokens.length - 2); i = i + 3) {
            String propertyName = propertyTokens[i].replace("\"","");
            //i + 1 should be '='
            String tokenName = propertyTokens[i + 2].replace("\"","");
            properties.put(propertyName,tokenName);
        }
        return new AclRule(permission,userOrGroupName,action,object,properties);
    }

    /**
     * Convenience method to get the user for queueWrite and queueRead
     * @return
     */
    public static String getQueueName(AclRule rule) {
        if (isQueueWriteRule(rule)) {
            return rule.getProperties().get("routingkey");
        } else {
            return rule.getProperties().get("name");

        }
    }

    public static boolean isQueueWriteRule(AclRule rule) {
        return rule.getAction().equals("PUBLISH") && rule.getObject().equals("EXCHANGE");
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
