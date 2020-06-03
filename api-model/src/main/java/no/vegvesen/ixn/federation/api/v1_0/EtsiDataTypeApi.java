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

import java.util.Map;
import java.util.Set;

public abstract class EtsiDataTypeApi extends DataTypeApi{

	private String serviceType;

	EtsiDataTypeApi() {
	}

	EtsiDataTypeApi(String messageType, String publisherId, String publisherName, String originatingCountry, String protocolVersion, String contentType, Set<String> quadTree, String serviceType) {
		super(messageType, publisherId, publisherName, originatingCountry, protocolVersion, contentType, quadTree);
		this.serviceType = serviceType;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	@Override
	public Map<String, String> getValues() {
		Map<String, String> values = super.getValues();
		putValue(values, MessageProperty.SERVICE_TYPE, this.getServiceType());
		return values;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof EtsiDataTypeApi)) return false;
		if (!super.equals(o)) return false;

		EtsiDataTypeApi that = (EtsiDataTypeApi) o;

		return serviceType != null ? serviceType.equals(that.serviceType) : that.serviceType == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (serviceType != null ? serviceType.hashCode() : 0);
		return result;
	}
}
