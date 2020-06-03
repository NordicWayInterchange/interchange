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

import java.util.HashSet;
import java.util.Set;


public class CapabilityApi{

	private String version = "1.0";
	private String name;
	private Set<DataTypeApi> capabilities = new HashSet<>();

	public CapabilityApi() {
	}

	public CapabilityApi(String name, Set<? extends DataTypeApi> capabilities) {
		this.name = name;
		this.capabilities.addAll(capabilities);
	}

	public String getVersion() {
		return version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<DataTypeApi> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Set<? extends DataTypeApi> capabilities) {
		if (this.capabilities == null) {
			this.capabilities = new HashSet<>();
		}
		this.capabilities.clear();
		this.capabilities.addAll(capabilities);
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "CapabilityApi{" +
				"version='" + version + '\''+
				", name='" + name + '\'' +
				", capabilities=" + capabilities +
				'}';
	}
}
