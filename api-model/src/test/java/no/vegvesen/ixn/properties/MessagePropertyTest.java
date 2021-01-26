package no.vegvesen.ixn.properties;


import no.vegvesen.ixn.properties.MessageProperty;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test is meant as a quick way of verifying that the filtering mechanism works as described in the
 * the spec, C-ITS IP Based Interface Profile_1.7.0_Final, section 3.3.1
 * This test checks that the isMandatory() method returns true or false as expected in the spec.
 */
public class MessagePropertyTest {

    @Test
    public void mandatoryFieldsForAllMessages() {

        Set<MessageProperty> mandatoryCommonProperties = new HashSet<>(Arrays.asList(
                        MessageProperty.PUBLISHER_ID,
                        MessageProperty.ORIGINATING_COUNTRY,
                        MessageProperty.PROTOCOL_VERSION,
                        MessageProperty.MESSAGE_TYPE,
                        MessageProperty.QUAD_TREE

        ));
        Set<MessageProperty> optionalCommonProperties = new HashSet<>(Arrays.asList(
                MessageProperty.SERVICE_TYPE,
                MessageProperty.LONGITUDE,
                MessageProperty.LATITUDE

        ));
        assertThat(filterProperties(MessageProperty.commonApplicationProperties,MessageProperty::isMandatory))
                .isEqualTo(mandatoryCommonProperties);
        assertThat(filterProperties(MessageProperty.commonApplicationProperties,MessageProperty::isOptional))
                .isEqualTo(optionalCommonProperties);
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
        assertThat(filterProperties(optionalDENMProperties,MessageProperty::isMandatory))
                .isEqualTo(optionalDENMProperties);
    }

    @Test
    public void mandatoryFieldsForIVI() {
        Set<MessageProperty> mandatoryIVIProperties = new HashSet<>(); //No mandatory IVI properties right now

        Set<MessageProperty> optionalIVIProperties = new HashSet<>(Arrays.asList(
                MessageProperty.IVI_TYPE,
                MessageProperty.PICTOGRAM_CATEGORY_CODE,
                MessageProperty.IVI_CONTAINER
        ));

        assertThat(filterProperties(mandatoryIVIProperties,MessageProperty::isMandatory))
                .isEqualTo(mandatoryIVIProperties);

        assertThat(filterProperties(optionalIVIProperties,MessageProperty::isOptional))
                .isEqualTo(optionalIVIProperties);

        assertThat(filterProperties(MessageProperty.iviApplicationProperties,MessageProperty::isMandatory))
                .isEqualTo(mandatoryIVIProperties);
    }

    //TODO COM, SPATEM, MAPEM, SSEM, SREM



    private Set<MessageProperty> filterProperties(Collection<MessageProperty> properties, Predicate<MessageProperty> predicate) {
       return  properties.stream().filter(predicate).collect(Collectors.toSet());
    }
}
