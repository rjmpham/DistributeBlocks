/*
  Calculate the hash of serializable objects
  Generates key pairs
  Digitally signs messages
  Verifies message is signed by someone
*/

package distributeblocks.crypto;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.Base64;

// required for saving and reading key files
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import distributeblocks.*;

public class Crypto{
	public static final String GEN_ALGORITHM = "DSA";

	public static KeyPair keyPairGenerator() {
		try {

			// We should look into what we want from our keys
			KeyPairGenerator generator = KeyPairGenerator.getInstance(GEN_ALGORITHM, "SUN");
			SecureRandom randy = SecureRandom.getInstance("SHA1PRNG","SUN");


			// Generate keys
			generator.initialize(1024, randy); // Key size is important for later.
	    KeyPair pair = generator.generateKeyPair();
			return pair;
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String keyToString(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}

	/*
	  Input: Takes in the private key to be used as the signature,
	  and the message to be signed as input.
	  Details: Signs the message using the DSA signature algorithm, using
	  SHA-1 message digest algorithm provided by SUN.
	  Output: Returns the digital signature of the given message.

	  Read: https://docs.oracle.com/javase/tutorial/security/apisign/index.html
	  and  https://medium.com/programmers-blockchain/creating-your-first-blockchain-with-java-part-2-transactions-2cdac335e0ce
	  and https://docs.oracle.com/javase/7/docs/api/java/security/Signature.html
	  for more information. (switched from elliptic curves to use built in dsa)
	*/
	public static byte[] signMessage(PrivateKey privateKey, String input) {
		try {
			byte[] inputBytes = input.getBytes();
			Signature dsAlgorithm = Signature.getInstance("SHA1withDSA", "SUN");
			dsAlgorithm.initSign(privateKey);
			dsAlgorithm.update(inputBytes);
			return dsAlgorithm.sign();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/*
		Input: Takes in the public key used to verify the signature,
		the data/message that was supposedly signed,
		and the signature for the given data/message as input.
		Details: Verifies the message using the DSA signature algorithm, using
	    SHA-1 message digest algorithm provided by SUN.
	    Output: Returns true if the signature + data correspond to the publicKey
	    else returns false.
	*/
	public static boolean verifySignature(PublicKey publicKey, String data, byte[] signature) {
		try {
			byte[] dataBytes = data.getBytes();
			Signature dsAlgorithm = Signature.getInstance("SHA1withDSA", "SUN");
			dsAlgorithm.initVerify(publicKey);
			dsAlgorithm.update(dataBytes);
			return dsAlgorithm.verify(signature);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	//Calculates the SHA-256 hash of the given object
	//Input must be serializable
	public static String calculateObjectHash(Object obj) throws FailedToHashException
	{
		try
		{
			String hash;
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();		//Data will be stored in the byte array here
			ObjectOutputStream objOut = new ObjectOutputStream(byteStream);
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			objOut.writeObject(obj);											//Serialize the object
			objOut.close();														//Flush stream
			byte[] hashByteArray = md.digest(byteStream.toByteArray());			//Create the hash as a byte array
			hash = Base64.getEncoder().encodeToString(hashByteArray);			//Convert the byte array hash into a string hash
			return hash;
		}
		catch (Exception e)
		{
			throw new FailedToHashException(obj,e);
		}
	}

	public static String calculateBlockHash(Block block) throws FailedToHashException
	{
		try
		{
			String hash;
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			//Feed in the fields of the object, except for the block hash and date. Timestamp and target will be done further down.
			md.update(block.getHashData().getBytes());
			md.update(block.getHashPrevious().getBytes());
			md.update(ByteBuffer.allocate(4).putInt(block.getNonce()).array());
			//Before feeding in the timestamp and target, we have to convert them into a byte array
			ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
			buf.putLong(block.getTimestamp());
			md.update(buf.array());						//Feed in the timestamp
			buf = ByteBuffer.allocate(Long.BYTES);
			buf.putLong(block.getTargetNumZeros());
			md.update(buf.array());						//Feed in the targetNumZeros
			//Output the hash of the block
			byte[] hashByteArray = md.digest();
			hash = Base64.getEncoder().encodeToString(hashByteArray);			//Convert the byte array hash into a string hash
			return hash;
		}
		catch (Exception e)
		{
			throw new FailedToHashException(block,e);
		}
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
