/*
  Calculate the hash of serializable objects
  Generates key pairs
  Digitally signs messages
  Verifies message is signed by someone
*/

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.Base64;
import java.util.ArrayList;

public class Crypto{


	public static KeyPair keyPairGenerator() {
		try {

			// We should look into what we want from our keys
			KeyPairGenerator generator = KeyPairGenerator.getInstance("DSA", "SUN");
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
}
