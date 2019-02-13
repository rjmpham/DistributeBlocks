// Calculate the hash of serializable objects
// Generates key pairs

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
