package no.vegvesen.ixn.federation.transformer;

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

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Self;
import org.springframework.stereotype.Component;

@Component
public class CapabilityTransformer {

	private final DataTypeTransformer dataTypeTransformer = new DataTypeTransformer();

	public Capabilities capabilityApiToCapabilities(CapabilityApi capabilityApi) {
		Capabilities capabilities = new Capabilities();
		capabilities.setDataTypes(dataTypeTransformer.dataTypeApiToDataType(capabilityApi.getCapabilities()));
		capabilities.setStatus(Capabilities.CapabilitiesStatus.KNOWN);
		return capabilities;
	}

	public CapabilityApi selfToCapabilityApi(Self self) {
		CapabilityApi capabilityApi = new CapabilityApi();
		capabilityApi.setName(self.getName());
		capabilityApi.setCapabilities(dataTypeTransformer.dataTypesToDataTypeApis(self.getLocalCapabilities()));
		return capabilityApi;
	}

}
