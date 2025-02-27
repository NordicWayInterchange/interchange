package no.vegvesen.ixn;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

public class NewSink {

    public static Context getSinkJmsContext(String sinkFactoryKey, String amqpsUrl) throws NamingException {
        Hashtable<Object,Object> props = new Hashtable<>();
        props.put(Context.INITIAL_CONTEXT_FACTORY,"org.apache.qpid.jms.jndi.JmsInitialContextFactory");
        props.put("connectionFactory." + sinkFactoryKey, amqpsUrl);
        return new InitialContext(props);
    }
}
