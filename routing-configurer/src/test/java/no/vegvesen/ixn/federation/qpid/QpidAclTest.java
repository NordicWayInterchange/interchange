package no.vegvesen.ixn.federation.qpid;

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


    @Test
    public void testDefaultACLRules() {
        QpidAcl aclList = new QpidAcl(aclRules);
        assertThat(aclList.size()).isEqualTo(8);
    }

    @Test
    public void testAddQueueReadAccess() {
        QpidAcl aclList = new QpidAcl(aclRules);
        aclList.addQueueReadAccess("routing_configurer","onramp");
        assertThat(aclList.size()).isEqualTo(9);
        assertThat(aclList.get(0)).isEqualTo("ACL ALLOW-LOG interchange ALL ALL");
        assertThat(aclList.get(7)).isEqualTo("ACL ALLOW-LOG routing_configurer CONSUME QUEUE name = \"onramp\"");
        assertThat(aclList.get(8)).isEqualTo("ACL DENY-LOG ALL ALL ALL");
    }

    @Test
    public void testAddQueueWriteAccess() {
        QpidAcl aclList = new QpidAcl(aclRules);
        aclList.addQueueWriteAccess("my_writing_client","my_private_queue");
        assertThat(aclList.size()).isEqualTo(9);
        assertThat(aclList.get(0)).isEqualTo("ACL ALLOW-LOG interchange ALL ALL");
        assertThat(aclList.get(7)).isEqualTo("ACL ALLOW-LOG my_writing_client PUBLISH EXCHANGE name = \"\" routingkey = \"my_private_queue\"");
        assertThat(aclList.get(8)).isEqualTo("ACL DENY-LOG ALL ALL ALL");

    }
}
