import java.util.ArrayList;

public class testDriver
{
	public static void main (String[] args)
	{
		ArrayList<Block> blockchain = new ArrayList<Block>();
		String data = "Hello I am data.";
		try
		{
		Block genesis = new Block(data, "", 0);
		System.out.println(genesis.getData());
		System.out.println(genesis.getHashBlock());
		blockchain.add(genesis);
		blockchain.add(new Block("I am more data.", genesis.getHashBlock(),5));
		System.out.println(blockchain.get(1).getData());
		System.out.println(blockchain.get(1).getHashBlock());
		System.out.println(blockchain.get(1).getHashPrevious());
		System.out.println(blockchain.get(1).getTargetNumZeros());
		}
		catch (FailedToHashException e)
		{
			System.out.println("Failed to create block.");
		}
		
		
	}
}