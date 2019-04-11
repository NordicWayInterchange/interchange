package no.vegvesen.ixn.ssl;

@SuppressWarnings("WeakerAccess")
public class KeystoreDetails {
	private final String fileName;
	private final String password;
	private final KeystoreType type;
	private final String keyPassword;

	public KeystoreDetails(String fileName, String password, KeystoreType type) {
		this.fileName = fileName;
		this.password = password;
		this.type = type;
		this.keyPassword = null;
	}

	public KeystoreDetails(String fileName, String password, KeystoreType type, String keyPassword) {
		this.fileName = fileName;
		this.password = password;
		this.type = type;
		this.keyPassword = keyPassword;
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

	public String getKeyPassword() {
		return keyPassword;
	}
}
