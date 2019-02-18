//Indicates there was a problem with producing the hash of a certain object

package distributeblocks;

public class FailedToHashException extends Exception{
	
	Object targetObject;					//Reference to the object that was being hashed when this exception was raised
	Exception originalException;			//Reference to the original exception that caused this to be thrown
	
	public FailedToHashException(Object targetObject, Exception exception)
	{
		this.targetObject = targetObject;
		this.originalException = originalException;
	}
	
}