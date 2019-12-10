package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.exceptions.HeaderNotFoundException;
import no.vegvesen.ixn.federation.exceptions.InvalidSelectorException;
import no.vegvesen.ixn.federation.exceptions.SelectorAlwaysTrueException;
import no.vegvesen.ixn.federation.model.BaseDataType;
import no.vegvesen.ixn.federation.model.DataTypeHeader;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class BaseDataTypeSelectorMatcherTest {

    private final DataTypeHeader datex = new DataTypeHeader("messageType","DATEX2");
    private final DataTypeHeader norway = new DataTypeHeader("originatingCountry","NO");

    @Test
    public void datextIsNotDenm() {
        BaseDataType dataType = new BaseDataType(Arrays.asList(datex));
        assertThat(BaseDataTypeSelectorMatcher.matches(dataType,"messageType like 'DENM'")).isFalse();
    }

    @Test
    public void datexIsDatex() {
        BaseDataType dataType = new BaseDataType(Arrays.asList(datex));
        assertThat(BaseDataTypeSelectorMatcher.matches(dataType,"messageType like 'DATEX2'")).isTrue();
    }

    //TODO should we be allowed to match with wildcards on datatype??
    @Test
    public void datexWithWildcard() {
        BaseDataType dataType = new BaseDataType(Arrays.asList(datex));
        assertThat(BaseDataTypeSelectorMatcher.matches(dataType,"messageType like 'DATEX%'")).isTrue();
    }

    @Test
    public void dataTypeContainsCountryAndTypeButOnlySelectCountry() {
        BaseDataType dataType = new BaseDataType(Arrays.asList(datex,norway));
        assertThat(BaseDataTypeSelectorMatcher.matches(dataType,"originatingCountry like 'NO'"));
    }

    @Test
    public void dataTypeContainsCountryAndTypeSelectorHasBoth() {
        BaseDataType dataType = new BaseDataType(Arrays.asList(datex,norway));
        assertThat(BaseDataTypeSelectorMatcher.matches(dataType,"originatingCountry like 'NO' and messageType like 'DATEX2'"));
    }

 	//equals without quote seems to be matching one header against another
    //TODO this gives SelectorAlwaysTrueException with the new regime. Need to take a look at why!
	@Test(expected = HeaderNotFoundException.class)
    //@Test(expected = SelectorAlwaysTrueException.class)
	public void mathcingWithoutSingleQuotes() {
		BaseDataType dataType = new BaseDataType(Arrays.asList(datex,norway));
		BaseDataTypeSelectorMatcher.matches(dataType,"originatingCountry = NO");
	}

	@Test(expected = InvalidSelectorException.class)
    public void matchingWildcardWithoutSingleQuotes() {
        BaseDataType dataType = new BaseDataType(Arrays.asList(datex,norway));
        BaseDataTypeSelectorMatcher.matches(dataType,"messageType like DATEX%");
    }

    @Test(expected = IllegalArgumentException.class)
    public void expirationFilteringIsNotAllowed() {
        BaseDataType dataType = new BaseDataType(Arrays.asList(datex,norway));
        BaseDataTypeSelectorMatcher.matches(dataType,"JMSExpiration > 3");
    }

    @Test(expected = HeaderNotFoundException.class)
    public void unkownHeaderAttributeInSelectorNotAccepted() {
        BaseDataType dataType = new BaseDataType(Arrays.asList(datex,norway));
        BaseDataTypeSelectorMatcher.matches(dataType,"region like 'someregion%'");
    }


    /*
    TODO this really doesn't work.
    @Test(expected = SelectorAlwaysTrueException.class)
    public void alwaysTrueNotAccepted() {
        BaseDataType dataType = new BaseDataType(Arrays.asList(datex,norway));
        BaseDataTypeSelectorMatcher.matches(dataType,"messageType like 'DATEX2' or 1=1");

    }

    @Test(expected = SelectorAlwaysTrueException.class)
    public void likeAnyStringIsAlwaysTrue() {
        BaseDataType dataType = new BaseDataType(Arrays.asList(datex,norway));
        BaseDataTypeSelectorMatcher.matches(dataType,"messageType like '%'");
    }

    @Test
    public void likeAnyStringIsAlwaysTrueTake2() {
        BaseDataType dataType = new BaseDataType(Arrays.asList(datex,norway));
        BaseDataTypeSelectorMatcher.matches(dataType,"messageType = 0");
    }
*/

}
