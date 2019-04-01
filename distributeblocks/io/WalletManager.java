package distributeblocks.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import distributeblocks.Wallet;

/*
 * Static method provider for saving and loading
 * wallets. This includes private/public key pairs,
 * as well as the wallet funds and onHold HashMaps.
 */
public class WalletManager {

	/*
	 * Saves a wallets information out to a desired file path.
	 */
	// TODO: implement this. probably throw some needed exceptions
	public static void saveWallet(Wallet wallet, String path) {
	}
	
	/*
	 * Loads a wallet from the path provided
	 */
	// TODO: implement this. probably throw some needed exceptions
	public static  Wallet loadWallet(String path) {
		return null;
	}
	
	/*
	 * Saves a KeyPair out to a file.
	 * Code adopted from: https://snipplr.com/view/18368/
	 */
	public static void saveKeyPair(String path, KeyPair keyPair) {
		PrivateKey privateKey = keyPair.getPrivate();
		PublicKey publicKey = keyPair.getPublic();
		FileOutputStream fos;
 
		try {
			// Store Public Key.
			X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
			fos = new FileOutputStream(path + "/public.key");
			fos.write(x509EncodedKeySpec.getEncoded());
			fos.close();
			
			// Store Private Key.
			PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
			fos = new FileOutputStream(path + "/private.key");
			fos.write(pkcs8EncodedKeySpec.getEncoded());
			fos.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
 
	/*
	 * Reads a KeyPair in from a file.
	 * Code adopted from: https://snipplr.com/view/18368/
	 */
	public static KeyPair loadKeyPair(String path, String algorithm) {
		try {
			// Read Public Key
			File filePublicKey = new File(path + "/public.key");
			FileInputStream fis = new FileInputStream(path + "/public.key");
			byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
			fis.read(encodedPublicKey);
			fis.close();
 
			// Read Private Key
			File filePrivateKey = new File(path + "/private.key");
			fis = new FileInputStream(path + "/private.key");
			byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
			fis.read(encodedPrivateKey);
			fis.close();
	 
			// Generate KeyPair
			KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
			// Generate publicKey
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
			PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
			// Generate privateKey
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
			PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
			
			return new KeyPair(publicKey, privateKey);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	//  Used to create the coinBase keys
	/*
	public static void main(String[] arg) {
		KeyPair kp = keyPairGenerator();
		String path = System.getProperty("user.dir") + "/coinBase";
		saveKeyPair(path, kp);
		
		KeyPair kp2 = loadKeyPair(System.getProperty("user.dir") + "/coinBase", "DSA");
	}
	*/
}
