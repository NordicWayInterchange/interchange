package no.vegvesen.ixn;

import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.message.JmsMessage;

import javax.jms.JMSException;
import javax.jms.Session;

public class MessageBuilder {
    private JmsMessage message;
    private Session session;

    protected MessageBuilder(Session session) {
        this.session = session;
    }

    public JmsMessage build() {
        return message;
    }

    public MessageBuilder textMessage(String text) throws JMSException {
        message = (JmsMessage) session.createTextMessage(text);
        return this;
    }

    public MessageBuilder bytesMessage(byte[] messageBody) throws JMSException {
        JmsBytesMessage bytesMessage = (JmsBytesMessage) session.createBytesMessage();
        bytesMessage.writeBytes(messageBody);
        message = bytesMessage;
        return this;
    }

    public MessageBuilder userId(String user) {
        message.getFacade().setUserId(user);
        return this;
    }

    public MessageBuilder messageType(String messageType) throws JMSException {
        message.setStringProperty(MessageProperty.MESSAGE_TYPE.getName(), messageType);
        return this;
    }

    public MessageBuilder datex2MessageType() throws JMSException {
       message.setStringProperty(MessageProperty.MESSAGE_TYPE.getName(),Datex2DataTypeApi.DATEX_2);
       return this;
    }

    public MessageBuilder publisherId(String publisher) throws JMSException {
        message.setStringProperty(MessageProperty.PUBLISHER_ID.getName(), publisher);
        return this;
    }

    public MessageBuilder protocolVersion(String version) throws JMSException {
        message.setStringProperty(MessageProperty.PROTOCOL_VERSION.getName(), version);
        return this;
    }

    public MessageBuilder originatingCountry(String originatingCountry) throws JMSException {
        message.setStringProperty(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
        return this;
    }

    public MessageBuilder quadTreeTiles(String messageQuadTreeTiles) throws JMSException {
        message.setStringProperty(MessageProperty.QUAD_TREE.getName(), messageQuadTreeTiles);
        return this;
    }

    public MessageBuilder serviceType(String serviceType) throws JMSException {
        message.setStringProperty(MessageProperty.SERVICE_TYPE.getName(), serviceType);
        return this;
    }

    public MessageBuilder timestamp(long currentTimeMillis) throws JMSException {
        message.setLongProperty(MessageProperty.TIMESTAMP.getName(), currentTimeMillis);
        return this;
    }

    public MessageBuilder stringProperty(String name, String value) throws JMSException {
        message.setStringProperty(name, value);
        return this;
    }

    public MessageBuilder causeCode(String causeCode) throws JMSException {
        message.setStringProperty(MessageProperty.CAUSE_CODE.getName(), causeCode);
        return this;
    }

    public MessageBuilder subCauseCode(String subCauseCode) throws JMSException {
        message.setStringProperty(MessageProperty.SUB_CAUSE_CODE.getName(), subCauseCode);
        return this;
    }

    public MessageBuilder publicationType(String publicationType) throws JMSException {
        message.setStringProperty(MessageProperty.PUBLICATION_TYPE.getName(), publicationType);
        return this;
    }

    public MessageBuilder latitude(double latitude) throws JMSException {
        message.setDoubleProperty(MessageProperty.LATITUDE.getName(), latitude);
        return this;
    }

    public MessageBuilder longitude(double longitude) throws JMSException {
        message.setDoubleProperty(MessageProperty.LONGITUDE.getName(), longitude);
        return this;
    }

    public MessageBuilder publicationSubType(String subType) throws JMSException {
        message.setStringProperty(MessageProperty.PUBLICATION_SUB_TYPE.getName(), subType);
        return this;
    }
}
