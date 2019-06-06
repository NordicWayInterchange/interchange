package no.vegvesen.ixn.federation.dbhelper;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix ="db-helper")
public class DbHelperProperties {

	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "DbHelperProperties{" +
				"type='" + type + '\'' +
				'}';
	}
}
