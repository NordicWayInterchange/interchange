package no.vegvesen.ixn.federation.ssl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class SslJavaxSettingsApp {

	final
	TestSslSystemSettingsConfig testSslConfig;

	public SslJavaxSettingsApp(TestSslSystemSettingsConfig testSslConfig) {
		this.testSslConfig = testSslConfig;
	}

	@PostConstruct
	void postConstruct(){
		 testSslConfig.setSystemSettingsForTest();
	}

	public static void main(String[] args) {
		SpringApplication.run(SslJavaxSettingsApp.class, args);
	}
}
