/**
 * Copyright 2023 Infosys Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package saralarchan.org.core;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import saralarchan.org.core.constants.GlobalConstants;


/**
 * Encryption Utility.

 */
public class CryptoEncryption {

	private CryptoEncryption() {
	}

	private static final Logger LOG = LoggerFactory.getLogger(CryptoEncryption.class);
	private static final String ENCRYPT_ALGO = "AES";

	/**
	 * Method to encode Secret Key
	 * @param str
	 * @return
	 */
	public static String encodeKey(String str) {
		byte[] encoded = Base64.getEncoder().encode(str.getBytes());
		return new String(encoded);
	}

	/**
	 * Method to decode Encrypted Secret Key
	 * @param str
	 * @return
	 */
	public static String decodeKey(String str) {
		byte[] decoded = Base64.getDecoder().decode(str.getBytes());
		return new String(decoded);
	}

	/**
	 * Method to decypt a data string
	 * @param strToDecrypt
	 * @return
	 */
	public static String decrypt(String strToDecrypt) {
		try {
			Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
			SecretKeySpec key = new SecretKeySpec(GlobalConstants.CIPHER_KEY.getBytes(), "AES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
		} catch (Exception e) {
			LOG.error("Error while decrypting: {}", e.toString());
		}
		return null;
	}

	/**
	 * Generate a random key based on desired key size.
	 * @return
	 */
	public static SecretKey getAESKey(int keysize) throws NoSuchAlgorithmException {
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(keysize, SecureRandom.getInstanceStrong());
		return keyGen.generateKey();
	}

	/**
	 * Method to encypt a data string
	 * @param data
	 * @return
	 */
	public static String encrypt(String data) {
		String encryptedValue = data;
		try {
			Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
			SecretKeySpec key = new SecretKeySpec(GlobalConstants.CIPHER_KEY.getBytes(), "AES");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			encryptedValue = new String(Base64.getEncoder().encode(cipher.doFinal(data.getBytes())));
		} catch (Exception e) {
			LOG.error("Error while encrypting: {}", e.toString());
		}
		return encryptedValue;
	}

}
