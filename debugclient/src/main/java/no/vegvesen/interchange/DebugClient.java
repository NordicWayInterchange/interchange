package no.vegvesen.interchange;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.message.JmsMessage;
import org.apache.qpid.jms.message.JmsTextMessage;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DebugClient implements MessageListener
{
	private static final String USER = System.getProperty("USER");
    private static final String PASSWORD = System.getProperty("PASSWORD");
	private Connection connection;
	private Session session;
	private MessageProducer messageProducer;
	
    void init()
    {
        System.setProperty("javax.net.debug","ssl:handshake");

		try
		{
			Context context = new InitialContext();
			
			JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup("myFactoryLookupTLS");
			factory.setPopulateJMSXUserID(true);
			
			System.out.println("Connecting to: "+factory.getRemoteURI());
			
            Destination queueR = (Destination) context.lookup("myQueueLookup3");
            Destination queueS = (Destination) context.lookup("myQueueLookup");
            System.out.println((char)27+"[36m rece queue: "+queueR.toString());
            System.out.println((char)27+"[36m send queue: "+queueS.toString());

            System.out.println(USER + PASSWORD);
            connection = factory.createConnection(USER, PASSWORD);
            //connection.setExceptionListener(new MyExceptionListener());
            connection.start();
            
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageConsumer messageConsumer = session.createConsumer(queueR);
            messageProducer = session.createProducer(queueS);
            System.out.println((char)27+"[33m Waiting for messages..");
            //System.out.println((char)27+"[0m ");
            messageConsumer.setMessageListener(this);
            
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
    }
    
	@Override
	public void onMessage(Message msg)
	{	
		try
		{	
			
			msg.acknowledge();
			//System.out.println("type: "+msg.getClass().getName());
			//JmsBytesMessage bm = (JmsBytesMessage)msg;
			//JmsTextMessage bm = (JmsTextMessage)msg;
			
			
			int delay = -1;
			if(msg.getStringProperty("when") != null) 
			{
				try
				{
					delay = (int) ZonedDateTime.parse(msg.getStringProperty("when")).until(ZonedDateTime.now(), ChronoUnit.MILLIS);
				} 
				catch (Exception e)
				{
					System.err.println("Could not parse \"when\"-field to calculate delay; "+msg.getStringProperty("when"));
				}
				
			}
	        
			System.out.println((char)27+"[1;32m Got message from "+msg.getStringProperty("who") +" @ delay="+delay+"ms:");
			System.out.println((char)27+"[1;32m (Msg type: "+msg.getStringProperty("what")+")\n");
			System.out.println((char)27+"[1;32m Msg props:");
			System.out.println((char)27+"[1;32m UserID:"+((JmsMessage)msg).getFacade().getUserId());
			System.out.println((char)27+"[1;32m Type:"+((JmsMessage)msg).getFacade().getType());
			System.out.println((char)27+"[1;32m Expiration:"+((JmsMessage)msg).getFacade().getExpiration());
			System.out.println((char)27+"[1;32m App props:");
			System.out.println((char)27+"[1;32m who: "+msg.getStringProperty("who"));
			System.out.println((char)27+"[1;32m how: "+msg.getStringProperty("how"));
			System.out.println((char)27+"[1;32m what: "+msg.getStringProperty("what"));
			try
			{
			System.out.println((char)27+"[1;32m lat: "+msg.getStringProperty("lat"));
			System.out.println((char)27+"[1;32m lon: "+msg.getStringProperty("lon"));
			}
			catch(Exception e) {}
			try
			{
			System.out.println((char)27+"[1;32m lat: "+msg.getDoubleProperty("lat"));
			System.out.println((char)27+"[1;32m lon: "+msg.getDoubleProperty("lon"));
			}
			catch(Exception e) {}
			try
			{
			System.out.println((char)27+"[1;32m lat: "+msg.getFloatProperty("lat"));
			System.out.println((char)27+"[1;32m lon: "+msg.getFloatProperty("lon"));
			}
			catch(Exception e) {}
			System.out.println((char)27+"[1;32m where1: "+msg.getStringProperty("where1"));
			System.out.println((char)27+"[1;32m when: "+msg.getStringProperty("when")+"\n");
	        
	        System.out.print("\t"+(char)27+"[37m");
			/*for (int i = 0; i < bm.getBodyLength(); i++)
			{
				char c = (char)bm.readByte();
				if(c == '\n') System.out.print(c+"\t");
				else System.out.print(c);
			}*/
	        try
	        {
	        	System.out.println(((JmsTextMessage)msg).getText());
	        }
	        catch(ClassCastException e)
	        {
	        	for (int i = 0; i < ((JmsBytesMessage)msg).getBodyLength(); i++)
				{
					char c = (char)((JmsBytesMessage)msg).readByte();
					if(c == '\n') System.out.print(c+"\t");
					else System.out.print(c);
				}
	        }
	        System.out.println((char)27+"[1;32m\n End of message from "+msg.getStringProperty("who") +"\n");
			System.out.println((char)27+"[0m ");
	        
		} 
        catch (Exception e)
		{
			System.err.println(e.getMessage());
			//System.exit(1);
		}
	}
	
	private void sendMessage(String msg)
	{
		sendMessage("no", msg);
	}
	
	private void sendMessage(String where, String msg)
	{
		try
		{
			JmsTextMessage message = (JmsTextMessage) session.createTextMessage(msg);//new String(msg.getBytes(), StandardCharsets.UTF_8)+"");
			//JmsBytesMessage message = (JmsBytesMessage) session.createBytesMessage();
			//message.writeBytes(msg.getBytes());
			
			//message.getFacade().setType("text/plain");
			message.getFacade().setUserId(USER);
			
			/*String testmsg = "";
			for(int i = 0; i<1000; i++)
			{
				testmsg+=i+"";
			}*/
			
	         message.setStringProperty("who", "Norwegian Public Roads Administration");//"Norwegian Public Roads Authority");
	         message.setStringProperty("how", "Datex2");
	         message.setStringProperty("what", "Conditions");
	         message.setStringProperty("lat", "63.0");
	         message.setStringProperty("lon", "10.0");
	         message.setStringProperty("where1", where);
	         message.setStringProperty("when", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
	         //message.setStringProperty("body", testmsg);
	         
	         
	         System.out.println((char)27+"[1;33m sending message");
	         System.out.println((char)27+"[0m ");
	         messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);

         
		} catch (JMSException e)
		{
			e.printStackTrace();
		}
	}
	
	void close()
	{
		try
		{
			System.out.println("closing");
			System.out.println((char)27+"[0m ");
			connection.close();
		} 
		catch (JMSException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{

		DebugClient c = new DebugClient();
		c.init();
		
		BufferedReader commandLine = new BufferedReader(new InputStreamReader(System.in));
		
		while(true)
        {
			try
			{
				String s = commandLine.readLine();
			
	            if (s.equalsIgnoreCase("exit") || s.equalsIgnoreCase("e") ||  s.equalsIgnoreCase("c")) 
	            {
	                c.close();
	                System.out.println("exiting..");
	                System.exit(0);
	            }
	            else if(s.startsWith("s ") || s.startsWith("send "))
	            {
	            	c.sendMessage(s.substring(s.indexOf(" ")));
	            }
	            else if(s.startsWith("d"))
	            {
	            	int firstSpace = s.indexOf(" ");
	            	int secSpace = s.indexOf(" ", firstSpace+1);
	            	
	            	String dest = s.substring(firstSpace, secSpace).trim();
	            	System.out.println("dest: "+dest);
	            	String msg = s.substring(secSpace).trim();
	            	System.out.println("msg: "+msg);
	            	c.sendMessage(dest, msg);
	            }
	            else if(s.startsWith("f "))
	            {
	            	FileInputStream is = new FileInputStream(s.substring(s.indexOf(" ")).trim());
	            	InputStreamReader isr = new InputStreamReader(is, "UTF-8");
	            	
	            	try(BufferedReader br = new BufferedReader(isr)) {
	            	    StringBuilder sb = new StringBuilder();
	            	    String line = br.readLine();

	            	    while (line != null) {
	            	        sb.append(line);
	            	        sb.append(System.lineSeparator());
	            	        line = br.readLine();
	            	    }
	            	    String everything = sb.toString();
	            	    
	            	    c.sendMessage(everything);
	            	}
	            }
            } catch (IOException e)
			{
				e.printStackTrace();
			}
        }
	}


}
