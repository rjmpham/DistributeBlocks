import java.util.ArrayList;
import java.security.Security;
import java.util.HashMap;

public class testDriver
{

	//list of Contract with funds
	public static HashMap<String,Contract_Out> funds_HashMap = new HashMap<String,Contract_Out>();
	public static Wallet wallet_Alice;
	public static Wallet wallet_Bob;
	public static int mineDifficulty = 3; // TODO: Determine this value somehow.
	public static float minimumContractAmount = 0.1f;
	public static void main (String[] args)
	{
		ArrayList<Block> blockchain = new ArrayList<Block>();


		wallet_Alice = new Wallet();
		wallet_Bob = new Wallet();

		System.out.println("Check Alice's keys:");
		System.out.println(Crypto.keyToString(wallet_Alice.privateKey));
		System.out.println(Crypto.keyToString(wallet_Alice.publicKey));
		String data = "Hello I am data.";
		try
		{
		Block genesis = new Block(data, "", 2);
		System.out.println(genesis.getData());
		System.out.println(genesis.getHashBlock());

		System.out.println("Block mined: " + genesis.isBlockMined());
		genesis.mineBlock();
		System.out.println("Block mined: " + genesis.isBlockMined());
		blockchain.add(genesis);
		Block secondBlock = new Block("I am more data.", genesis.getHashBlock(),mineDifficulty);
		System.out.println("Block mined: " + secondBlock.isBlockMined());
		secondBlock.mineBlock();
		System.out.println("Block mined: " + secondBlock.isBlockMined());
		blockchain.add(secondBlock);
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
