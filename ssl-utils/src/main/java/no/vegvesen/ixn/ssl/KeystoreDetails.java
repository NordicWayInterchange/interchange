package no.vegvesen.ixn.ssl;

public class KeystoreDetails {
	private final String fileName;
	private final String password;
	private final KeystoreType type;

	public KeystoreDetails(String fileName, String password, KeystoreType type) {
		this.fileName = fileName;
		this.password = password;
		this.type = type;
	}

	public KeystoreDetails(String fileName, String password, KeystoreType type, String keyPassword) {
		this.fileName = fileName;
		this.password = password;
		this.type = type;
	}

	public String getFileName() {
		return fileName;
	}

	public String getPassword() {
		return password;
	}

	public KeystoreType getType() {
		return type;
	}

}
