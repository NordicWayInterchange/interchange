package no.vegvesen.ixn;

import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.model.IllegalMessageException;
import no.vegvesen.ixn.model.MessageValidator;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.message.JmsMessage;
import org.apache.qpid.jms.provider.amqp.message.AmqpMessageSupport;

import javax.jms.JMSException;
import javax.jms.Session;

public class MessageBuilder {
    private JmsMessage message;
    private Session session;

    protected MessageBuilder(Session session) {
        this.session = session;
    }

    public JmsMessage build() {
        MessageValidator messageValidator = new MessageValidator();
        if (! messageValidator.isValid(message)) {
            throw new IllegalMessageException("Message is not valid");
        }
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
        if (user != null) {
            message.getFacade().setUserId(user);
        }
        return this;
    }

    //Common message properties

    public MessageBuilder publisherId(String publisher) throws JMSException {
        if (publisher != null) {
            message.setStringProperty(MessageProperty.PUBLISHER_ID.getName(), publisher);
        }
        return this;
    }

    public MessageBuilder publicationId(String publicationId) throws JMSException {
        if (publicationId != null) {
            message.setStringProperty(MessageProperty.PUBLICATION_ID.getName(), publicationId);
        }
        return this;
    }

    public MessageBuilder originatingCountry(String originatingCountry) throws JMSException {
        if (originatingCountry != null) {
            message.setStringProperty(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
        }
        return this;
    }

    public MessageBuilder protocolVersion(String version) throws JMSException {
        if (version != null) {
            message.setStringProperty(MessageProperty.PROTOCOL_VERSION.getName(), version);
        }
        return this;
    }

    public MessageBuilder serviceType(String serviceType) throws JMSException {
        if (serviceType != null) {
            message.setStringProperty(MessageProperty.SERVICE_TYPE.getName(), serviceType);
        }
        return this;
    }

    public MessageBuilder baselineVersion(String baselineVersion) throws JMSException {
        if (baselineVersion != null) {
            message.setStringProperty(MessageProperty.BASELINE_VERSION.getName(), baselineVersion);
        }
        return this;
    }

    public MessageBuilder messageType(String messageType) throws JMSException {
        if (messageType != null) {
            message.setStringProperty(MessageProperty.MESSAGE_TYPE.getName(), messageType);
        }
        return this;
    }

    public MessageBuilder longitude(double longitude) throws JMSException {
        message.setDoubleProperty(MessageProperty.LONGITUDE.getName(), longitude);
        return this;
    }

    public MessageBuilder latitude(double latitude) throws JMSException {
        message.setDoubleProperty(MessageProperty.LATITUDE.getName(), latitude);
        return this;
    }

    public MessageBuilder quadTreeTiles(String messageQuadTreeTiles) throws JMSException {
        if (messageQuadTreeTiles != null) {
            message.setStringProperty(MessageProperty.QUAD_TREE.getName(), messageQuadTreeTiles);
        }
        return this;
    }

    public MessageBuilder shardId(Integer shardId) throws JMSException {
        if (shardId != null) {
            message.setIntProperty(MessageProperty.SHARD_ID.getName(), shardId);
        }
        return this;
    }

    public MessageBuilder shardCount(Integer shardCount) throws JMSException {
        if (shardCount != null) {
            message.setIntProperty(MessageProperty.SHARD_COUNT.getName(), shardCount);
        }
        return this;
    }

    public MessageBuilder timestamp(long currentTimeMillis) throws JMSException {
        message.setLongProperty(MessageProperty.TIMESTAMP.getName(), currentTimeMillis);
        return this;
    }

    //DATEX2 message properties

    public MessageBuilder publicationType(String publicationType) throws JMSException {
        if (publicationType != null) {
            message.setStringProperty(MessageProperty.PUBLICATION_TYPE.getName(), publicationType);
        }
        return this;
    }

    public MessageBuilder publicationSubType(String subType) throws JMSException {
        if (subType != null) {
            message.setStringProperty(MessageProperty.PUBLICATION_SUB_TYPE.getName(), subType);
        }
        return this;
    }

    //DENM message properties

    public MessageBuilder causeCode(Integer causeCode) throws JMSException {
        if (causeCode != null) {
            message.setIntProperty(MessageProperty.CAUSE_CODE.getName(), causeCode);
        }
        return this;
    }

    public MessageBuilder subCauseCode(Integer subCauseCode) throws JMSException {
        if (subCauseCode != null) {
            message.setIntProperty(MessageProperty.SUB_CAUSE_CODE.getName(), subCauseCode);
        }
        return this;
    }

    //IVIM message properties

    public MessageBuilder iviType(String iviType) throws JMSException {
        if (iviType != null) {
            message.setStringProperty(MessageProperty.IVI_TYPE.getName(), iviType);
        }
        return this;
    }

    public MessageBuilder pictogramCategoryCode(String pictogramCode) throws JMSException {
        if (pictogramCode != null) {
            message.setStringProperty(MessageProperty.PICTOGRAM_CATEGORY_CODE.getName(), pictogramCode);
        }
        return this;
    }

    public MessageBuilder iviContainer(String iviContainer) throws JMSException {
        if (iviContainer != null) {
            message.setStringProperty(MessageProperty.IVI_CONTAINER.getName(), iviContainer);
        }
        return this;
    }

    //SPATEM/MAPEM and SSEM/SREM message property

    public MessageBuilder id(String id) throws JMSException {
        if (id != null) {
            message.setStringProperty(MessageProperty.IDS.getName(), id);
        }
        return this;
    }

    //SPATEM/MAPEM additional message property

    public MessageBuilder name(String name) throws JMSException {
        if (name != null) {
            message.setStringProperty(MessageProperty.NAME.getName(), name);
        }
        return this;
    }

    //CAM message properties

    public MessageBuilder stationType(Integer stationType) throws JMSException {
        if (stationType != null) {
            message.setIntProperty(MessageProperty.STATION_TYPE.getName(), stationType);
        }
        return this;
    }

    public MessageBuilder vehicleRole(Integer vehicleRole) throws JMSException {
        if (vehicleRole != null) {
            message.setIntProperty(MessageProperty.VEHICLE_ROLE.getName(), vehicleRole);
        }
        return this;
    }

    public MessageBuilder ttl(long ttl) throws JMSException {
        message.setLongProperty(AmqpMessageSupport.JMS_AMQP_TTL,ttl);
        return this;
    }
}
