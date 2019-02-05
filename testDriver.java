public class testDriver
{
	public static void main (String[] args)
	{
		String data = "Hello I am data.";
		try
		{
		Block genesis = new Block(data, "");
		System.out.println(genesis.getData());
		System.out.println(genesis.getHashBlock());
		}
		catch (FailedToHashException e)
		{
			System.out.println("Failed to create block.");
		}
	}
}