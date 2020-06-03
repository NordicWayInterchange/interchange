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

public class IviDataTypeApi extends EtsiDataTypeApi{
	public static final String IVI = "IVI";

	private Integer iviType;
	private Set<Integer> pictogramCategoryCodes;

	public IviDataTypeApi() {
		this.setMessageType(IVI);
	}

	public IviDataTypeApi(String publisherId, String publisherName, String originatingCountry,
						  String protocolVersion, String contentType, Set<String> quadTree,
						  String serviceType, Integer iviType, Set<Integer> pictogramCategoryCodes) {
		super(IVI, publisherId, publisherName, originatingCountry, protocolVersion, contentType, quadTree, serviceType);
		this.iviType = iviType;
		this.pictogramCategoryCodes = pictogramCategoryCodes;
	}


	public Integer getIviType() {
		return iviType;
	}

	public void setIviType(Integer iviType) {
		this.iviType = iviType;
	}

	@SuppressWarnings("WeakerAccess")
	public Set<Integer> getPictogramCategoryCodes() {
		return pictogramCategoryCodes;
	}

	public void setPictogramCategoryCodes(Set<Integer> pictogramCategoryCodes) {
		this.pictogramCategoryCodes = pictogramCategoryCodes;
	}

	@Override
	public Map<String, String> getValues() {
		Map<String, String> values = super.getValues();
		putValue(values, MessageProperty.IVI_TYPE, this.getIviType());
		putIntegerValue(values, MessageProperty.PICTOGRAM_CATEGORY_CODE, this.getPictogramCategoryCodes());
		return values;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof IviDataTypeApi)) return false;
		if (!super.equals(o)) return false;

		IviDataTypeApi that = (IviDataTypeApi) o;

		if (iviType != null ? !iviType.equals(that.iviType) : that.iviType != null) return false;
		return pictogramCategoryCodes != null ? pictogramCategoryCodes.equals(that.pictogramCategoryCodes) : that.pictogramCategoryCodes == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (iviType != null ? iviType.hashCode() : 0);
		result = 31 * result + (pictogramCategoryCodes != null ? pictogramCategoryCodes.hashCode() : 0);
		return result;
	}
}
