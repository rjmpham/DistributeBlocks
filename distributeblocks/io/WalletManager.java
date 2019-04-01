package distributeblocks.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import distributeblocks.TransactionOut;
import distributeblocks.Wallet;
import distributeblocks.crypto.Crypto;

/*
 * Static method provider for saving and loading
 * wallets. This includes private/public key pairs,
 * as well as the wallet funds and onHold HashMaps.
 */
// TODO: probably replace the runtime exceptions with something more logical
public class WalletManager {

	/*
	 * Saves a wallets information out to a desired file path.
	 */
	public static void saveWallet(String path, Wallet wallet) {
		saveKeyPair(path, new KeyPair(wallet.getPublicKey(), wallet.getPrivateKey()));
		saveFundsHashMap(path, wallet.getFundsHashMap());
		saveOnHoldHashMap(path, wallet.getOnHoldHashMap());
	}
	
	/*
	 * Loads a wallet from the path provided.
	 */
	public static  Wallet loadWallet(String path) {
		KeyPair keys = loadKeyPair(path, Crypto.GEN_ALGORITHM);
		HashMap<String, TransactionOut> funds_HashMap = loadFundsHashMap(path);
		HashMap<String, TransactionOut> onHold_HashMap = loadOnHoldHashMap(path);
		return new Wallet(keys, funds_HashMap, onHold_HashMap);
	}
	
	/*
	 * Saves the funds_HashMap to the specified path.
	 * This will be saved into the path as "funds.json".
	 */
	public static void saveFundsHashMap(String path, HashMap<String, TransactionOut> funds_HashMap) {
		saveHashMap(System.getProperty("user.dir") + path + "/funds.json", funds_HashMap);
	}
	
	/*
	 * Saves the funds_HashMap to the specified path.
	 * This will be saved into the path as "onHold.json".
	 */
	public static void saveOnHoldHashMap(String path, HashMap<String, TransactionOut> onHold_HashMap) {
		saveHashMap(System.getProperty("user.dir") + path + "/onHold.json", onHold_HashMap);
	}
	
	/*
	 * Saves a generic HashMap to a json file.
	 * The fullPath argument is expected to be the fully qualified filename.
	 */
	private static <K, V> void saveHashMap(String fullPath, HashMap<K, V> map) {
		try {
			File file = new File(fullPath);
			file.getParentFile().mkdirs();
			
			// Write the json object to the file
			Gson gson = new Gson();
			gson.toJson(map, new FileWriter(file));
			
		} catch (JsonIOException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/*
	 * Loads the funds_HashMap from the specified path.
	 * The HashMap is expected to be in the path as "funds.json".
	 */
	public static HashMap<String, TransactionOut> loadFundsHashMap(String path) {
		return loadHashMap(System.getProperty("user.dir") + path + "/funds.json");
	}
	
	/*
	 * Loads the onHold_HashMap from the specified path.
	 * The HashMap is expected to be in the path as "funds.json".
	 */
	public static HashMap<String, TransactionOut> loadOnHoldHashMap(String path) {
		return loadHashMap(System.getProperty("user.dir") + path + "/onHold.json");
	}
	
	/*
	 * Loads a generic HashMap from a json file.
	 * The fullPath argument is expected to be the fully qualified filename.
	 */
	private static <K, V> HashMap<K, V> loadHashMap(String fullPath) {
		try {
			// Get the type of the generic map
			Type typeOfHashMap = new TypeToken<HashMap<K, V>>() { }.getType();
			
			// Load the map from the file
			Gson gson = new Gson();
			JsonReader reader = new JsonReader(new FileReader(fullPath));
			HashMap<K, V> map = gson.fromJson(reader, typeOfHashMap);
			return map;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/*
	 * Saves a KeyPair out to a file.
	 * The keys will be saved into the path as "public.key" and "private.key".
	 */
	public static void saveKeyPair(String path, KeyPair keyPair) {
		savePublicKey(System.getProperty("user.dir") + path + "/public.key", keyPair.getPublic());
		savePrivateKey(System.getProperty("user.dir") + path + "/private.key", keyPair.getPrivate());
	}
	
	
	/*
	 * Saves a PublicKey out to a file.
	 * Code adopted from: https://snipplr.com/view/18368/
	 */
	public static void savePublicKey(String fullPath, PublicKey publicKey) {
		try {
			File file = new File(fullPath);
			file.getParentFile().mkdirs();
			
			// Store Public Key
			X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
			FileOutputStream fos = new FileOutputStream(fullPath);
			fos.write(encodedKeySpec.getEncoded());
			fos.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/*
	 * Saves a PrivateKey out to a file.
	 * Code adopted from: https://snipplr.com/view/18368/
	 */
	public static void savePrivateKey(String fullPath, PrivateKey privateKey) {
		try {
			File file = new File(fullPath);
			file.getParentFile().mkdirs();
			
			// Store Private Key
			PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
			FileOutputStream fos = new FileOutputStream(fullPath);
			fos.write(encodedKeySpec.getEncoded());
			fos.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
 
	/*
	 * Reads a KeyPair in from a file.
	 */
	public static KeyPair loadKeyPair(String path, String algorithm) {
		PublicKey publicKey = loadPublicKey(System.getProperty("user.dir") + path + "/public.key", algorithm);
		PrivateKey privateKey = loadPrivateKey(System.getProperty("user.dir") + path + "/private.key", algorithm);
		return new KeyPair(publicKey, privateKey);
	}
	
	/*
	 * Reads a public key in from a file.
	 * Code adopted from: https://snipplr.com/view/18368/
	 */
	public static PublicKey loadPublicKey(String fullPath, String algorithm) {
		try {
			// Read Public Key
			File filePublicKey = new File(fullPath);
			FileInputStream fis = new FileInputStream(fullPath);
			byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
			fis.read(encodedPublicKey);
			fis.close();
			
			// Generate publicKey
			KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
			PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
			return publicKey;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/*
	 * Reads a private key in from a file.
	 * Code adopted from: https://snipplr.com/view/18368/
	 */
	public static PrivateKey loadPrivateKey(String fullPath, String algorithm) {
		try {
			// Read Public Key
			File privateKeyFile = new File(fullPath);
			FileInputStream fis = new FileInputStream(fullPath);
			byte[] encodedPrivateKey = new byte[(int) privateKeyFile.length()];
			fis.read(encodedPrivateKey);
			fis.close();
			
			// Generate publicKey
			KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
			PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
			return privateKey;
			
		} catch (Exception e) {
			e.printStackTrace();
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
