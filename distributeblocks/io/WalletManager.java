package distributeblocks.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import distributeblocks.Block;
import distributeblocks.Transaction;
import distributeblocks.TransactionResult;
import distributeblocks.Wallet;
import distributeblocks.crypto.Crypto;

/**
 * Static method provider for saving and loading
 * wallets. This includes private/public key pairs,
 * as well as the wallet funds and onHold HashMaps.
 */
public class WalletManager {

	/**
	 * Saves a wallets information out to a desired file path.
	 * 
	 * @param path		path to the director where the wallet files will be saved
	 * @param wallet	the Wallet to save
	 * @throws IOException 
	 */
	public static void saveWallet(String path, Wallet wallet) throws IOException {
		saveKeyPair(path, new KeyPair(wallet.getPublicKey(), wallet.getPrivateKey()));
		saveFundsHashMap(path, wallet.getFundsHashMap());
		saveOnHoldHashMap(path, wallet.getOnHoldHashMap());
		saveAlias(path, wallet.getAlias());
	}
	
	/**
	 * Loads a wallet from the path provided.
	 * 
	 * @param path		path to the directory where the wallet files will be loaded from
	 *
	 * @return the loaded Wallet
	 * @throws IOException 
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 * @throws ClassNotFoundException 
	 */
	public static Wallet loadWallet(String path) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, ClassNotFoundException {
		KeyPair keys = loadKeyPair(path, Crypto.GEN_ALGORITHM);
		HashMap<String, TransactionResult> funds_HashMap = loadFundsHashMap(path);
		HashMap<String, TransactionResult> onHold_HashMap = loadOnHoldHashMap(path);
		String alias = loadAlias(path);
		
		if (keys == null)
			return null;
		
		return new Wallet(keys, funds_HashMap, onHold_HashMap, alias);
	}
	
	/**
	 * Saves the funds_HashMap to the specified path.
	 * This will be saved into the path as "funds.json".
	 * 
	 * @param funds_HashMap		the funds_HashMap of a Wallet
	 */
	public static void saveFundsHashMap(String path, HashMap<String, TransactionResult> funds_HashMap) {
		try {
			String fullPath = path + "funds.bin";
			saveHashMap(fullPath, funds_HashMap);
			
		} catch (IOException e) {
			Console.log("Error: could not save funds_HashMap to " + path);
		}
	}
	
	/**
	 * Saves the funds_HashMap to the specified path.
	 * This will be saved into the path as "onHold.json".
	 * 
	 * @param onHold_HashMap	the onHold_HashMap of a Wallet
	 */
	public static void saveOnHoldHashMap(String path, HashMap<String, TransactionResult> onHold_HashMap) {
		try {
			String fullPath = path + "onHold.bin";
			saveHashMap(fullPath, onHold_HashMap);
		
		} catch (JsonIOException | IOException e) {
			Console.log("Error: could not save onHold_HashMap to " + path);
		}
	}
	
	/**
	 * Saves a generic HashMap to a json file.
	 * The fullPath argument is expected to be the fully qualified filename.
	 * 
	 * @param fullPath		fully qualified path to save the map at
	 * @param map			The HashMap to save
	 * 
	 * @throws JsonIOException, IOException
	 */
	private static <K, V> void saveHashMap(String fullPath, HashMap<K, V> map) throws JsonIOException, IOException {
		File file = new File(fullPath);
		file.getParentFile().mkdirs();
		
		// Write the json object to the file 	<-- does not work with Interfaces (PublicKey and PrivateKey)! 
		//Gson gson = new Gson();
		//gson.toJson(map, new FileWriter(file));

		// Write out a serialized version of the hashmap
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fullPath));
		out.writeObject(map);
		out.close();
	}
	
	/**
	 * Loads the funds_HashMap from the specified path.
	 * The HashMap is expected to be in the path as "funds.json".
	 * 
	 * @param path		path to the director where the funds.json file is
	 * 
	 * @return the loaded funds_HashMap	
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static HashMap<String, TransactionResult> loadFundsHashMap(String path) throws ClassNotFoundException, IOException {
		String fullPath = path + "funds.bin";
		return loadHashMap(fullPath);
	}
	
	/**
	 * Loads the onHold_HashMap from the specified path.
	 * The HashMap is expected to be in the path as "funds.json".
	 * 
	 * @param path		path to the director where the onHold.json file is
	 * 
	 * @return the loaded onHold_HashMap	
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static HashMap<String, TransactionResult> loadOnHoldHashMap(String path) throws ClassNotFoundException, IOException {
		String fullPath = path + "onHold.bin";
		return loadHashMap(fullPath);
	}
	
	/**
	 * Loads a generic HashMap from a json file.
	 * The fullPath argument is expected to be the fully qualified filename.
	 * 
	 * @param fullPath		fully qualified path to to the HashMap json file
	 * 
	 * @return loaded HashMap
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	private static <K, V> HashMap<K, V> loadHashMap(String fullPath) throws IOException, ClassNotFoundException {
		// Get the type of the generic map
		Type typeOfHashMap = new TypeToken<HashMap<K, V>>() { }.getType();
		
		// Load the map from the file  	<-- does not work with Interfaces (PublicKey and PrivateKey)!
		// Gson gson = new Gson();
		// JsonReader reader = new JsonReader(new FileReader(fullPath));
		//  HashMap<K, V> map = gson.fromJson(reader, typeOfHashMap);
		
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(fullPath));
		HashMap<K, V> map = (HashMap<K, V>) in.readObject();
		return map;
	}
	
	/**
	 * Saves a KeyPair out to a file.
	 * The keys will be saved into the path as "public.key" and "private.key".
	 * 
	 * @param path		path to the directory where the keyPair will be saved
	 * @param keyPair	KeyPair to save
	 * @throws IOException 
	 */
	public static void saveKeyPair(String path, KeyPair keyPair) throws IOException {
		savePublicKey(path + "public.key", keyPair.getPublic());
		savePrivateKey(path + "private.key", keyPair.getPrivate());
	}
	
	
	/**
	 * Saves a PublicKey out to a file.
	 * Code adopted from: https://snipplr.com/view/18368/
	 * 
	 * @param fullPath	the fully qualified path where the publicKey will be saved
	 * 
	 * @throws IOException
	 */
	public static void savePublicKey(String fullPath, PublicKey publicKey) throws IOException {
		File file = new File(fullPath);
		file.getParentFile().mkdirs();
		
		// Store Public Key
		X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
		FileOutputStream fos = new FileOutputStream(fullPath);
		fos.write(encodedKeySpec.getEncoded());
		fos.close();
	}
	
	/**
	 * Saves a PrivateKey out to a file.
	 * Code adopted from: https://snipplr.com/view/18368/
	 * 
	 * @param fullPath	the fully qualified path where the privateKey will be saved
	 * 
	 * @throws IOException
	 */
	public static void savePrivateKey(String fullPath, PrivateKey privateKey) throws IOException {
		File file = new File(fullPath);
		file.getParentFile().mkdirs();
		
		// Store Private Key
		PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
		FileOutputStream fos = new FileOutputStream(fullPath);
		fos.write(encodedKeySpec.getEncoded());
		fos.close();
	}
 
	/**
	 * Reads a KeyPair in from a file.
	 * 
	 * @param path			path to the directory where the public.key and private.key files are
	 * @param algorithm		the algorithm used to create the KeyPair
	 * 
	 * @return the loaded KeyPair
	 * @throws IOException 
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	public static KeyPair loadKeyPair(String path, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		PublicKey publicKey = loadPublicKey(path + "public.key", algorithm);
		PrivateKey privateKey = loadPrivateKey(path + "private.key", algorithm);
		return new KeyPair(publicKey, privateKey);
	}
	
	/**
	 * Reads a public key in from a file.
	 * Code adopted from: https://snipplr.com/view/18368/
	 * 
	 * @param fullPath		the fully qualified path to the public key file
	 * @param algorithm		the algorithm used to create the PublicKey
	 * 
	 * @return the loaded PublicKey
	 * 
	 * @throws IOException, NoSuchAlgorithmException, InvalidKeySpecException
	 */
	public static PublicKey loadPublicKey(String fullPath, String algorithm) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
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
	}
	
	/**
	 * Reads a private key in from a file.
	 * Code adopted from: https://snipplr.com/view/18368/
	 * 
	 * @param fullPath		the fully qualified path to the private key file
	 * @param algorithm		the algorithm used to create the PrivateKey
	 * 
	 * @return the loaded PrivateKey
	 * 
	 * @throws IOException, NoSuchAlgorithmException, InvalidKeySpecException
	 */
	public static PrivateKey loadPrivateKey(String fullPath, String algorithm) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
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
	}
	
	/**
	 * Saves an alias to a file
	 * 
	 * @param path		the path where the alias.txt file will be created in
	 * 
	 * @throws IOException
	 */
	public static void saveAlias(String path, String alias) throws IOException {
		File file = new File(path + "alias.txt");
		file.getParentFile().mkdirs();
		
		// Store alias
		FileWriter writer = new FileWriter(path);
		writer.write(alias);
		writer.close();
	}
	
	/**
	 * Loads an alias from a file
	 * 
	 * @param path		the path where the alias.txt file will be created in
	 * 
	 * @throws IOException
	 */
	public static String loadAlias(String path) throws IOException {
		File file = new File(path + "alias.txt");
		file.getParentFile().mkdirs();
		
		// Read Alias
		String alias;
		FileReader reader = new FileReader(path);
		reader.read(alias);
		reader.close();
		
		return alias;
	}
}
