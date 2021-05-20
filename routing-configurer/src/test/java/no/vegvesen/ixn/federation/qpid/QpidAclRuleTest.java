package no.vegvesen.ixn.federation.qpid;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class QpidAclRuleTest {

    private String publishQueueRule = "ACL ALLOW-LOG service-providers PUBLISH EXCHANGE routingkey = \"onramp\" name = \"\"";
    private String publishQueueReversedProperties = "ACL ALLOW-LOG my_writing_client PUBLISH EXCHANGE name = \"\" routingkey = \"my_private_queue\"";
    private String allowAllAcl = "ACL ALLOW-LOG administrators ALL ALL";

    @Test
    public void userOrGroupNameForQueueWriteRule() {
       AclRule aclRule = AclRule.parse(publishQueueRule);
       assertThat(aclRule.getUserOrGroup()).isEqualTo("service-providers");
       AclRule reversed = AclRule.parse(publishQueueReversedProperties);
       assertThat(reversed.getUserOrGroup()).isEqualTo("my_writing_client");
    }

    @Test
    public void isQueueWriteRule() {
        AclRule aclRule = AclRule.parse(publishQueueRule);
        assertThat(AclRule.isQueueWriteRule(aclRule)).isTrue();
        AclRule reversed = AclRule.parse(publishQueueReversedProperties);
        assertThat(AclRule.isQueueWriteRule(reversed)).isTrue();
    }

    @Test
    public void queueNameForQueueWriteRule() {
        AclRule aclRule = AclRule.parse(publishQueueRule);
        assertThat(AclRule.getQueueName(aclRule)).isEqualTo("onramp");
        AclRule reversed = AclRule.parse(publishQueueReversedProperties);
        assertThat(AclRule.getQueueName(reversed)).isEqualTo("my_private_queue");
    }

    @Test
    public void allowAllAclTest() {
        AclRule rule = AclRule.parse(allowAllAcl);
        assertThat(rule.toRuleString()).isEqualTo("ACL ALLOW-LOG administrators ALL ALL");
    }

    @Test
    public void createRuleFromTextAndParts() {
        AclRule ruleFromText = AclRule.parse(publishQueueRule);
        HashMap<String,String> properties = new HashMap<>();
        properties.put("name","");
        properties.put("routingkey","onramp");
        AclRule ruleFromPieces = new AclRule("ALLOW-LOG","service-providers","PUBLISH","EXCHANGE",properties);

        assertThat(ruleFromText).isEqualTo(ruleFromPieces);
    }

    @Test
    public void twoRulesWithPropertiesOrderReversedAreEqual() {
        AclRule rule = AclRule.parse("ACL ALLOW-LOG service-providers PUBLISH EXCHANGE routingkey = \"onramp\" name = \"\"");
        AclRule changedRule = AclRule.parse("ACL ALLOW-LOG service-providers PUBLISH EXCHANGE name = \"\" routingkey = \"onramp\"");
        assertThat(rule).isEqualTo(changedRule);
    }
}
