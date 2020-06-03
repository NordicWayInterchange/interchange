package no.vegvesen.ixn.federation.capability;

/*-
 * #%L
 * neighbour-model
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import no.vegvesen.ixn.federation.exceptions.HeaderNotFilterable;
import no.vegvesen.ixn.federation.exceptions.HeaderNotFoundException;
import no.vegvesen.ixn.federation.exceptions.InvalidSelectorException;
import no.vegvesen.ixn.federation.exceptions.SelectorAlwaysTrueException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class JMSSelectorFilterFactoryTest {

	//equals without quote seems to be matching one header against another
	@Test
	public void mathcingWithoutSingleQuotes() {
		assertThatExceptionOfType(HeaderNotFoundException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("originatingCountry = NO");
		});
	}

	@Test
	public void mathcingOriginatingCountry() {
		assertThat(JMSSelectorFilterFactory.isValidSelector("originatingCountry = 'NO'")).isTrue();
	}

	@Test
	public void mathcingUnknownAttributeIsNotValid() {
		assertThat(JMSSelectorFilterFactory.isValidSelector("illegalAttribute = 'NO'")).isFalse();
	}

	@Test
	public void mathingWildcardWithoutSingleQuotes() {
		assertThatExceptionOfType(InvalidSelectorException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("messageType like datex%");
		});
	}

	@Test
	public void expirationFilteringIsNotSupported() {
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("JMSExpiration > 3");
		});
	}

	@Test
	public void unknownHeaderAttributeNotAccepted() {
		assertThatExceptionOfType(HeaderNotFoundException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("region like 'some region%'");
		});
	}

	@Test
	public void alwaysTrueIsNotAccepted() {
		assertThatExceptionOfType(SelectorAlwaysTrueException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("messageType like 'spat%' or 1=1");
		});
	}

	@Test
	public void likeAnyStringIsAlwaysTrueHenceNotAccepted() {
		assertThatExceptionOfType(SelectorAlwaysTrueException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("originatingCountry like '%'");
		});
	}

	@Test
	public void invalidSyntaxIsNotAccepted() {
		assertThatExceptionOfType(InvalidSelectorException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("messageType flike 'spat%'");
		});
	}

	@Test
	public void filterWithDoubleQuotedStringValueIsNotAllowed() {
		assertThatExceptionOfType(InvalidSelectorException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("originatingCountry = \"NO\"");
		});
	}

	@Test
	public void testMinusOneFiter() {
		assertThatExceptionOfType(SelectorAlwaysTrueException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("originatingCountry like '-1%'");
		});
	}

	@Test
	public void testSelectorWithBackTick() {
		assertThatExceptionOfType(InvalidSelectorException.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("originatingCountry like `NO`");
		});
	}

	@Test
	public void latitudeIsNotPossibleToFilterOn() {
		assertThatExceptionOfType(HeaderNotFilterable.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("latitude > 1");
		});
	}

	@Test
	public void longitudeIsNotPossibleToFilterOn() {
		assertThatExceptionOfType(HeaderNotFilterable.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("longitude > 1");
		});
	}

	@Test
	public void timestampIsNotPossibleToFilterOn() {
		assertThatExceptionOfType(HeaderNotFilterable.class).isThrownBy(() -> {
			JMSSelectorFilterFactory.get("timestamp > 1");
		});
	}

	@Test
	public void emptySelectorWillMatchEverythingAndThereforeNotAllowed() {
		assertThat(JMSSelectorFilterFactory.isValidSelector("")).isFalse();
	}
}
