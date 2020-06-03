package no.vegvesen.ixn.ssl;

/*-
 * #%L
 * ssl-utils
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
