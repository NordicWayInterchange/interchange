package no.vegvesen.ixn.federation.model;

import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class BaseDataTypeTest {

    @Test
    public void baseDataTypeCanContainMandatoryHeader() {
        DataTypeHeader header = new DataTypeHeader("originatingCountry","NO");
        BaseDataType baseDataType = new BaseDataType(Arrays.asList(header));
        assertThat(baseDataType.getHeaders()).size().isEqualTo(1);
    }

    @Test
    public void baseDataTypeCanContainOptionalHeader() {
        DataTypeHeader header = new DataTypeHeader("publisherId","NO00001");
        BaseDataType baseDataType = new BaseDataType(Arrays.asList(header));
        assertThat(baseDataType.getHeaders()).size().isEqualTo(1);
    }

    //TODO make it another exception type?
    @Test(expected = IllegalArgumentException.class)
    public void baseDataTypeCannotContainUnknownHeader() {
        DataTypeHeader header = new DataTypeHeader("foo","bar");
        BaseDataType baseDataType = new BaseDataType(Arrays.asList(header));
    }

}
