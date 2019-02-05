//Calculate the hash of serializable objects

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.util.Base64;

public class Hasher{
	
	//Calculates the SHA-256 hash of the given object
	//Input must be serializable
	//IOException
	//NoSuchAlgorithmException
	public static String calculateHash(Object obj) throws FailedToHashException
	{
		try
		{
			String hash;
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();		//Data will be stored in the byte array here
			ObjectOutputStream objOut = new ObjectOutputStream(byteStream);
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			objOut.writeObject(obj);											//Serialize the object
			byte[] hashByteArray = md.digest(byteStream.toByteArray());			//Create the hash as a byte array
			hash = Base64.getEncoder().encodeToString(hashByteArray);			//Convert the byte array hash into a string hash
			return hash;
		}
		catch (Exception e)
		{
			throw new FailedToHashException(obj,e);
		}
	}
	
}