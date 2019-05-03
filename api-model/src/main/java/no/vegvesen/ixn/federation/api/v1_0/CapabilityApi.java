package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.federation.model.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;


public class CapabilityApi {

	private static final String version = "1.0";
	private String name;
	private Set<DataType> capabilities;

	private Logger logger = LoggerFactory.getLogger(CapabilityApi.class);

	public CapabilityApi() {
	}

	public CapabilityApi(String name, Set<DataType> capabilities) {
		this.name = name;
		this.capabilities = capabilities;
	}

	public static String getVersion() {
		return version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<DataType> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Set<DataType> capabilities) {
		this.capabilities = capabilities;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CapabilityApi)) return false;

		CapabilityApi that = (CapabilityApi) o;

		if (!name.equals(that.name)) return false;
		if (!capabilities.equals(that.capabilities)) return false;
		return logger.equals(that.logger);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + capabilities.hashCode();
		result = 31 * result + logger.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "CapabilityApi{" +
				"name='" + name + '\'' +
				", capabilities=" + capabilities +
				'}';
	}
}
