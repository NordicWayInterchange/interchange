package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.federation.qpid.AclRule;
import no.vegvesen.ixn.federation.qpid.QpidAcl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QpidAclTest {


    String aclRules = "ACL ALLOW-LOG interchange ALL ALL\n" +
            "ACL ALLOW-LOG administrators ALL ALL\n" +
            "ACL ALLOW-LOG service-providers PUBLISH EXCHANGE routingkey = \"onramp\" name = \"\"\n" +
            "ACL ALLOW-LOG service-providers ACCESS VIRTUALHOST name = \"localhost\"\n" +
            "ACL ALLOW-LOG federated-interchanges ACCESS VIRTUALHOST name = \"localhost\"\n" +
            "ACL ALLOW-LOG clients-private-channels ACCESS VIRTUALHOST name = \"localhost\"\n" +
            "ACL ALLOW-LOG interchange CONSUME QUEUE name = \"onramp\"\n" +
            "ACL DENY-LOG ALL ALL ALL\n";

    String aclRulesWithQueueWriteAccess = "ACL ALLOW-LOG interchange ALL ALL\n" +
            "ACL ALLOW-LOG administrators ALL ALL\n" +
            "ACL ALLOW-LOG service-providers PUBLISH EXCHANGE routingkey = \"onramp\" name = \"\"\n" +
            "ACL ALLOW-LOG service-providers ACCESS VIRTUALHOST name = \"localhost\"\n" +
            "ACL ALLOW-LOG federated-interchanges ACCESS VIRTUALHOST name = \"localhost\"\n" +
            "ACL ALLOW-LOG clients-private-channels ACCESS VIRTUALHOST name = \"localhost\"\n" +
            "ACL ALLOW-LOG interchange CONSUME QUEUE name = \"onramp\"\n" +
            "ACL ALLOW-LOG my_writing_client PUBLISH EXCHANGE name = \"\" routingkey = \"my_private_queue\"\n" +
            "ACL DENY-LOG ALL ALL ALL\n";

    String aclRulesWithQueueReadAccess = "ACL ALLOW-LOG interchange ALL ALL\n" +
            "ACL ALLOW-LOG administrators ALL ALL\n" +
            "ACL ALLOW-LOG service-providers PUBLISH EXCHANGE routingkey = \"onramp\" name = \"\"\n" +
            "ACL ALLOW-LOG service-providers ACCESS VIRTUALHOST name = \"localhost\"\n" +
            "ACL ALLOW-LOG federated-interchanges ACCESS VIRTUALHOST name = \"localhost\"\n" +
            "ACL ALLOW-LOG clients-private-channels ACCESS VIRTUALHOST name = \"localhost\"\n" +
            "ACL ALLOW-LOG interchange CONSUME QUEUE name = \"onramp\"\n" +
            "ACL ALLOW-LOG my_reading_client CONSUME QUEUE name = \"some_queue\"\n" +
            "ACL DENY-LOG ALL ALL ALL\n";


    @Test
    public void testDefaultACLRules() {
        QpidAcl aclList = QpidAcl.parseRules(aclRules);
        assertThat(aclList.size()).isEqualTo(8);
    }

    @Test
    public void testCreateQueueWriteAccessRule() {
        assertThat(QpidAcl.createQueueWriteAccessRule("my_writing_client","my_private_queue"))
                .isEqualTo(AclRule.parse("ACL ALLOW-LOG my_writing_client PUBLISH EXCHANGE name = \"\" routingkey = \"my_private_queue\""));

    }

    @Test
    public void testCreateQueueReadAccessRule() {
        assertThat(QpidAcl.createQeueReadAccessRule("my_reading_client","my_queue"))
                .isEqualTo(AclRule.parse("ACL ALLOW-LOG my_reading_client CONSUME QUEUE name = \"my_queue\""));
    }

    @Test
    public void testAddQueueReadAccess() {
        QpidAcl aclList = QpidAcl.parseRules(aclRules);
        aclList.addQueueReadAccess("my_reading_client","onramp");
        assertThat(aclList.size()).isEqualTo(9);
        assertThat(aclList.get(0)).isEqualTo(AclRule.parse("ACL ALLOW-LOG interchange ALL ALL"));
        assertThat(aclList.get(7)).isEqualTo(AclRule.parse("ACL ALLOW-LOG my_reading_client CONSUME QUEUE name = \"onramp\""));
        assertThat(aclList.get(8)).isEqualTo(AclRule.parse("ACL DENY-LOG ALL ALL ALL"));
    }

    @Test
    public void testAddQueueWriteAccess() {
        QpidAcl aclList = QpidAcl.parseRules(aclRules);
        aclList.addQueueWriteAccess("my_writing_client","my_private_queue");
        assertThat(aclList.size()).isEqualTo(9);
        assertThat(aclList.get(0)).isEqualTo(AclRule.parse("ACL ALLOW-LOG interchange ALL ALL"));
        assertThat(aclList.get(7)).isEqualTo(AclRule.parse("ACL ALLOW-LOG my_writing_client PUBLISH EXCHANGE name = \"\" routingkey = \"my_private_queue\""));
        assertThat(aclList.get(8)).isEqualTo(AclRule.parse("ACL DENY-LOG ALL ALL ALL"));

    }

    @Test
    public void testRemoveQueueWriteAccess() {
        QpidAcl qpidAcl = QpidAcl.parseRules(aclRulesWithQueueWriteAccess);
        assertThat(qpidAcl.removeQueueWriteAccess("my_writing_client","my_private_queue")).isTrue();
        assertThat(qpidAcl.size()).isEqualTo(8);
        assertThat(qpidAcl.get(0)).isEqualTo(AclRule.parse("ACL ALLOW-LOG interchange ALL ALL"));
        assertThat(qpidAcl.get(7)).isEqualTo(AclRule.parse("ACL DENY-LOG ALL ALL ALL"));
    }

    @Test
    public void testRemoveQueueWriteAccessAttributesSwitched() {
        QpidAcl qpidAcl = QpidAcl.parseRules(aclRulesWithQueueWriteAccess);
        assertThat(qpidAcl.removeQueueWriteAccess("service-providers","onramp")).isTrue();


    }


    @Test
    public void testRemoveReadAccess() {
        QpidAcl qpidAcl = QpidAcl.parseRules(aclRulesWithQueueReadAccess);
        assertThat(qpidAcl.removeQueueReadAccess("my_reading_client","some_queue")).isTrue();
        assertThat(qpidAcl.size()).isEqualTo(8);
        assertThat(qpidAcl.get(0)).isEqualTo(AclRule.parse("ACL ALLOW-LOG interchange ALL ALL"));
        assertThat(qpidAcl.get(7)).isEqualTo(AclRule.parse("ACL DENY-LOG ALL ALL ALL"));

    }

}
