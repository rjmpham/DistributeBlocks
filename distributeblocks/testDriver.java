package distributeblocks;
import java.util.LinkedList;
import java.security.Security;
import java.util.HashMap;
import java.io.IOException;
import distributeblocks.crypto.*;

public class testDriver
{

	//list of Contract with funds
	public static HashMap<String,Contract_Out> funds_HashMap = new HashMap<String,Contract_Out>();
	public static Wallet wallet_Alice;
	public static Wallet wallet_Bob;
	public static int mineDifficulty = 3; // TODO: Determine this value somehow.
	public static float minimumContractAmount = 0.1f;
	public static int stepThrough = 0;

	public static void main (String[] args)
	{
		LinkedList<Block> blockchain = new LinkedList<Block>();

		//If there are two arguements, stepThrough
		if(args.length == 1){
			stepThrough = 1;
		}

		if (stepThrough == 1) {
			System.out.println("\nPress Enter to continue");
			try {
				System.in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		/*
		 * Checking the creating of wallets, their keys and their signatures.
		 */

		wallet_Alice = new Wallet();
		wallet_Bob = new Wallet();
		System.out.println("\nCheck Alice's keys:");

		// Commands to observe the keys themselves
		//System.out.println(Crypto.keyToString(wallet_Alice.getPrivateKey()));
		//System.out.println(Crypto.keyToString(wallet_Alice.getPublicKey()));
		byte[] signatureCheck = Crypto.signMessage(wallet_Alice.getPrivateKey(),"hello world");

		if (Crypto.verifySignature(wallet_Alice.getPublicKey(),"hello world",signatureCheck)) {
			System.out.println("Alice signature works!");
		} else {
			System.out.println("Alice fails her signature check!");
		}


		if (stepThrough == 1) {
			System.out.println("\nPress Enter to continue");
			try {
				System.in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("\nChecking genesis block");
		String data = "Hello I am data.";
		try
		{
		Block genesis = new Block(data, "", 2);
		System.out.println(genesis.getData());
		System.out.println(genesis.getHashBlock());

		if (stepThrough == 1) {
			System.out.println("\nPress Enter to continue");
			try {
				System.in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("\nMining genesis block");

		System.out.println("Block mined: " + genesis.isBlockMined());
		genesis.mineBlock();
		System.out.println("Block mined: " + genesis.isBlockMined());
		blockchain.add(genesis);
		Block secondBlock = new Block("I am more data.", genesis.getHashBlock(),mineDifficulty);

		if (stepThrough == 1) {
			System.out.println("\nPress Enter to continue");
			try {
				System.in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("\nChecking second block if meets mining target");
		System.out.println("Block mined: " + secondBlock.isBlockMined());
		System.out.println("Explicitly mining second block");

		secondBlock.mineBlock();
		System.out.println("Block mined: " + secondBlock.isBlockMined());
		blockchain.add(secondBlock);

		if (stepThrough == 1) {
			System.out.println("\nPress Enter to continue");
			try {
				System.in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("\nPrinting data of the second block : ");
		System.out.println("Block's data : \n" + blockchain.get(1).getData());
		System.out.println("Block's hash : \n" +blockchain.get(1).getHashBlock());
		System.out.println("Block's previous hash : " +blockchain.get(1).getHashPrevious());
		System.out.println("Block's target number of zeros : " + blockchain.get(1).getTargetNumZeros());

		System.out.println();
		}
		catch (FailedToHashException e)
		{
			System.out.println("Failed to create block.");
		}


	}
}
