package no.vegvesen.ixn.federation.api.v1_0;

/*-
 * #%L
 * api-model
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

import no.vegvesen.ixn.properties.MessageProperty;

import java.util.*;

public class Datex2DataTypeApi extends DataTypeApi{

	public static final String DATEX_2 = "DATEX2";
	private String publicationType;
	private Set<String> publicationSubType = new HashSet<>();

	public Datex2DataTypeApi() {
		this.setMessageType(DATEX_2);
	}

	public Datex2DataTypeApi(String publisherId, String publisherName, String originatingCountry, String protocolVersion, String contentType, Set<String> quadTree, String publicationType, Set<String> publicationSubType) {
		super(DATEX_2, publisherId, publisherName, originatingCountry, protocolVersion, contentType, quadTree);
		this.publicationType = publicationType;
		if (publicationSubType != null) {
			this.publicationSubType.addAll(publicationSubType);
		}
	}

	public Datex2DataTypeApi(String originatingCountry) {
		super(DATEX_2, null, null, originatingCountry, null, null, Collections.emptySet());
	}

	public String getPublicationType() {
		return this.publicationType;
	}

	public void setPublicationType(String publicationType) {
		this.publicationType = publicationType;
	}

	public Set<String> getPublicationSubType() {
		return publicationSubType;
	}

	public void setPublicationSubType(Collection<String> publicationSubType) {
		this.publicationSubType.clear();
		if (publicationSubType != null) {
			this.publicationSubType.addAll(publicationSubType);
		}
	}

	@Override
	public Map<String, String> getValues() {
		Map<String, String> values = super.getValues();
		putValue(values, MessageProperty.PUBLICATION_TYPE, this.getPublicationType());
		putValue(values, MessageProperty.PUBLICATION_SUB_TYPE, this.getPublicationSubType());
		return values;
	}
}
