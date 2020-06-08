package no.vegvesen.ixn.federation.ssl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class TestSslAppOnly {

	final
	TestSslSystemSettingsConfig testSslConfig;

	public TestSslAppOnly(TestSslSystemSettingsConfig testSslConfig) {
		this.testSslConfig = testSslConfig;
	}


	@PostConstruct
	void postConstruct(){
		 testSslConfig.setSystemSettingsForTest();
	}

	public static void main(String[] args) {
		SpringApplication.run(TestSslAppOnly.class, args);
	}
}