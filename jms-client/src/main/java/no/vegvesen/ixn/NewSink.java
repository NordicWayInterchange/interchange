package no.vegvesen.ixn;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

public class NewSink {


    private final Context context;

    public NewSink(String sinkFactoryKey, String url) throws NamingException {
        context = getSinkJmsContext(sinkFactoryKey,url);

    }

    private static Context getSinkJmsContext(String sinkFactoryKey, String amqpsUrl) throws NamingException {
        Hashtable<Object,Object> props = new Hashtable<>();
        props.put(Context.INITIAL_CONTEXT_FACTORY,"org.apache.qpid.jms.jndi.JmsInitialContextFactory");
        props.put("connectionFactory." + sinkFactoryKey, amqpsUrl);
        return new InitialContext(props);
    }


    public Context getContext() {
        return context;
    }
}
