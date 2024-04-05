package no.vegvesen.ixn.properties;


import org.ietf.jgss.MessageProp;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test is meant as a quick way of verifying that the filtering mechanism works as described in
 * the spec, C-ITS IP Based Interface Profile_1.7.0_Final, section 3.3.1
 * This test checks that the isMandatory() method returns true or false as expected in the spec.
 *
 * This test has been edited to match the C-ITS Based Interface Profile_2.0.8, section 3.3.2
 */
public class MessagePropertyTest {

    @Test
    public void mandatoryFieldsForAllMessages() {

        Set<MessageProperty> mandatoryCommonProperties = new HashSet<>(Arrays.asList(
                        MessageProperty.PUBLISHER_ID,
                        MessageProperty.PUBLICATION_ID,
                        MessageProperty.ORIGINATING_COUNTRY,
                        MessageProperty.PROTOCOL_VERSION,
                        MessageProperty.MESSAGE_TYPE,
                        MessageProperty.QUAD_TREE

        ));
        Set<MessageProperty> optionalCommonProperties = new HashSet<>(Arrays.asList(
                MessageProperty.SERVICE_TYPE,
                MessageProperty.BASELINE_VERSION,
                MessageProperty.LONGITUDE,
                MessageProperty.LATITUDE,
                MessageProperty.SHARD_ID,
                MessageProperty.SHARD_COUNT

        ));

        assertThat(filterProperties(MessageProperty.commonApplicationProperties,MessageProperty::isMandatory))
                .isEqualTo(mandatoryCommonProperties);

        assertThat(filterProperties(MessageProperty.commonApplicationProperties,MessageProperty::isOptional))
                .isEqualTo(optionalCommonProperties);
    }

    @Test
    public void mandatoryFieldsForDATEX(){
        Set<MessageProperty> mandatoryDATEXProperties = new HashSet<>(Arrays.asList(
                MessageProperty.PUBLISHER_NAME,
                MessageProperty.PUBLICATION_TYPE
        ));
        Set<MessageProperty> optionalDATEXProperties = new HashSet<>(Arrays.asList(
                MessageProperty.PUBLICATION_SUB_TYPE
        ));

        assertThat(filterProperties(MessageProperty.datex2ApplicationProperties, MessageProperty::isMandatory))
                .isEqualTo(mandatoryDATEXProperties);

        assertThat(filterProperties(MessageProperty.datex2ApplicationProperties, MessageProperty::isOptional))
                .isEqualTo(optionalDATEXProperties);
    }
    @Test
    public void mandatoryFieldsForDENM() {
        Set<MessageProperty> mandatoryDENMProperties = new HashSet<>(Arrays.asList(
                MessageProperty.CAUSE_CODE,
                MessageProperty.SUB_CAUSE_CODE
        ));

        Set<MessageProperty> optionalDENMProperties = new HashSet<>(); //no optional DENM properties right now

        assertThat(filterProperties(MessageProperty.denmApplicationProperties,MessageProperty::isMandatory))
                .isEqualTo(mandatoryDENMProperties);

        assertThat(filterProperties(MessageProperty.denmApplicationProperties,MessageProperty::isOptional))
                .isEqualTo(optionalDENMProperties);
    }

    @Test
    public void mandatoryFieldsForIVIM() {
        Set<MessageProperty> mandatoryIVIMProperties = new HashSet<>(); //No mandatory IVIM properties right now

        Set<MessageProperty> optionalIVIMProperties = new HashSet<>(Arrays.asList(
                MessageProperty.IVI_TYPE,
                MessageProperty.PICTOGRAM_CATEGORY_CODE,
                MessageProperty.IVI_CONTAINER
        ));

        assertThat(filterProperties(MessageProperty.ivimApplicationProperties,MessageProperty::isOptional))
                .isEqualTo(optionalIVIMProperties);

        assertThat(filterProperties(MessageProperty.ivimApplicationProperties,MessageProperty::isMandatory))
                .isEqualTo(mandatoryIVIMProperties);
    }

    @Test
    public void mandatoryFieldsForSPATEMAndMAPEM() {
        Set<MessageProperty> mandatorySPATEMMAPEMProperties = new HashSet<>(); //No mandatory SPATEM/MAPEM properties right now

        Set<MessageProperty> optionalSPATEMMAPEMProperties = new HashSet<>(Arrays.asList(
                MessageProperty.IDS,
                MessageProperty.NAME
        ));

        assertThat(filterProperties(MessageProperty.spatemMapemApplicationProperties, MessageProperty::isMandatory))
                .isEqualTo(mandatorySPATEMMAPEMProperties);

        assertThat(filterProperties(MessageProperty.spatemMapemApplicationProperties, MessageProperty::isOptional))
                .isEqualTo(optionalSPATEMMAPEMProperties);
    }

    @Test
    public void mandatoryFieldsForSSEMAndSREM() {
        Set<MessageProperty> mandatorySSEMSREMProperties = new HashSet<>(); //No mandatory SSEN/SREM properties right now

        Set<MessageProperty> optionalSSEMSREMProperties = new HashSet<>(Arrays.asList(
                MessageProperty.IDS
        ));

        assertThat(filterProperties(MessageProperty.sremSsemApplicationProperties, MessageProperty::isMandatory))
                .isEqualTo(mandatorySSEMSREMProperties);

        assertThat(filterProperties(MessageProperty.sremSsemApplicationProperties, MessageProperty::isOptional))
                .isEqualTo(optionalSSEMSREMProperties);
    }

    @Test
    public void mandatoryFieldsForCAM() {
        Set<MessageProperty> mandatoryCAMProperties = new HashSet<>(Arrays.asList(
                MessageProperty.STATION_TYPE
        ));

        Set<MessageProperty> optionalCAMProperties = new HashSet<>(Arrays.asList(
                MessageProperty.VEHICLE_ROLE
        ));

        assertThat(filterProperties(MessageProperty.camApplicationProperties, MessageProperty::isMandatory))
                .isEqualTo(mandatoryCAMProperties);

        assertThat(filterProperties(MessageProperty.camApplicationProperties, MessageProperty::isOptional))
                .isEqualTo(optionalCAMProperties);
    }

    private Set<MessageProperty> filterProperties(Collection<MessageProperty> properties, Predicate<MessageProperty> predicate) {
       return  properties.stream().filter(predicate).collect(Collectors.toSet());
    }
}
