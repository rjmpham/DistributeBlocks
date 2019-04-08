package distributeblocks.util;

import java.util.LinkedList;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.HashMap;

import distributeblocks.*;
import distributeblocks.crypto.*;
import distributeblocks.io.WalletManager;

public class Validator
{
	// Coin base keys are used for signing block reward transactions from a static source
	// TODO: move these to a standard location and grab them from there. same changes to wallet
	private static final String COIN_BASE_ID = "COIN_BASE";
	private static final String COIN_BASE_DIR = "/coinBase";
	private static final KeyPair COIN_BASE_KEYS = WalletManager.loadKeyPair(COIN_BASE_DIR, Crypto.GEN_ALGORITHM);
	private static final float BLOCK_REWARD_AMOUNT = 5.0f;
	
	/*
	 * Input: Transaction to check validity of, blockchain to check transaction against
	 * Output: True if transaction inputs exist in the blockchain and are unspent, false otherwise
	 * Details: Checks if transaction inputs exist in the blockchain and are unspent
	 * If the transaction is a block reward then return true
	 */
	public boolean isValidTransaction(Transaction transaction, LinkedList<Block> blockchain)
	{
		if(transaction.getPublicKeySender().equals(COIN_BASE_KEYS.getPublic()) && transaction.getExchange() == BLOCK_REWARD_AMOUNT){
			return true;
		}
		if(!isUnspent(transaction, blockchain))
		{
			return false;
		}
		
		return true;
	}
	
	/*
	 * Input: Transaction to check if unspent, blockchain to compare against
	 * Output: True if unspent, false if one or more inputs were spent
	 * Details: Loops through every transaction input, for ever transaction, for every block and checks if the given transaction's inputs were used as another transactions inputs
	 */
	public static boolean isUnspent(Transaction transaction, LinkedList<Block> blockchain)
	{
		Block currentBlock;
		
		//Get all the transaction input id's for the transaction
		ArrayList<String> inputs = new ArrayList<String>();
		transaction.getTransactionInputs().forEach(i -> inputs.add(i.getParentId()));
		
		//Loop through each block until every transaction input is found
		for(int i = blockchain.size() - 1; i >= 0; i--)
		{
			currentBlock = blockchain.get(i);
			HashMap<String, Transaction> blockTransactions = currentBlock.getData();
			
			//Loop through each transaction in the given block looking for the transaction input id's
			for (String id : blockTransactions.keySet()) 
			{
				//Get the transaction inputs for the transaction we're comparing to that already exists in the blockchain
				ArrayList<String> existingInputs = new ArrayList<String>();
				blockTransactions.get(id).getTransactionInputs().forEach(z -> existingInputs.add(z.getParentId()));
				
				//Loop through every transaction INPUT id that has not yet been found
				for(int n = 0; n < inputs.size(); i++)
				{
					//Loop through every transaction INPUT for existing transactions
					//If a match is found then it was spent
					for(int m = 0; m < existingInputs.size(); m++)
					{
						if(existingInputs.get(m).equals(inputs.get(n)))
						{
							return false;
						}
					}
					
					//if found transaction id corresponding to an input, record its block and mark as found, remove it from inputs list and move to next transaction
					if(id.equals(inputs.get(n))) 
					{
						inputs.remove(n);
						if(inputs.isEmpty())
						{
							return true;
						}
						break;
					}
				}
			}
		}
		//if we somehow reach here then the transaction was not even found
		System.out.println("One or more transaction inputs were not found");
		return false;
	}
	
	/*
	 * Input: Transaction and blockchain to check for existing transaciton inputs
	 * Output: True if all transaction inputs for the given transaction were found in the blockchain
	 * Details: finds the transaction inputs in the blockchain, and checks if the number found is the same as the number of transaction inputs
	 */
	public static boolean containsValidTransactionInputs(Transaction transaction, LinkedList<Block> blockchain)
	{
		//Search for the transaction inputs in the blockchain
		ArrayList<String> foundTransactionInputs = findTransactionInputsBlocks(transaction, blockchain);
		
		//Get all the transaction input id's for the transaction
		ArrayList<String> inputs = new ArrayList<String>();
		transaction.getTransactionInputs().forEach(i -> inputs.add(i.getParentId()));
		
		//Compare number of inputs found to number of actual inputs
		if(foundTransactionInputs.size() == inputs.size()){
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/*
	 * Input: Transaction, and blockchain linkedlist to find the transaction inputs in
	 * Output: Arraylist of the block id's the transaction inputs were found in
	 * Details: cycles through every transaction in every block from the newest block in the chain until it finds all the transaction inputs
	 */
	public static ArrayList<String> findTransactionInputsBlocks(Transaction transaction, LinkedList<Block> blockchain)
	{
		ArrayList<String> foundTransactionInputs = new ArrayList<String>();
		Block currentBlock;
		
		//Get all the transaction input id's for the transaction
		ArrayList<String> inputs = new ArrayList<String>();
		transaction.getTransactionInputs().forEach(i -> inputs.add(i.getParentId()));
		
		//Loop through each block until every transaction input is found
		for(int i = blockchain.size() - 1; i >= 0; i--)
		{
			currentBlock = blockchain.get(i);
			HashMap<String, Transaction> blockTransactions = currentBlock.getData();
			
			//Loop through each transaction in the given block looking for the transaction input id's
			for (String id : blockTransactions.keySet()) 
			{
				//Loop through every transaction INPUT id that has not yet been found
				for(int n = 0; n < inputs.size(); i++)
				{
					//if found transaction id corresponding to an input, record its block and mark as found, remove it from inputs list and move to next transaction
					if(id.equals(inputs.get(n))) 
					{
						foundTransactionInputs.add(currentBlock.getHashBlock());
						inputs.remove(n);
						if(inputs.isEmpty())
						{
							return foundTransactionInputs;
						}
						break;
					}
				}
			}
		}
		
		return foundTransactionInputs;
	}
	
	//Checks if a given block is valid
	//Currently does not check whether the block satisfies the target
	public static boolean isValidBlock(Block block, String hashPreviousBlock) throws FailedToHashException
	{
		try
		{
			if (!block.getHashBlock().equals(Crypto.calculateBlockHash(block)))				//If the block hash isn't correct...
				return false;
			if (block.getHashData().equals(Crypto.calculateObjectHash(block.getData())))	//If the data hash isn't correct...
				return false;
			if (block.getHashPrevious().equals(hashPreviousBlock))							//If the previous hash isn't correct...
				return false;
			return true;																	//Otherwise return true
		}
		catch (Exception e)
		{
			throw new FailedToHashException(block,e);
		}
	}

	//Checks whether an ArrayList of blocks is valid
	public static boolean isValidBlockchain(LinkedList<Block> blockchain) throws FailedToHashException
	{
		Block currentBlock = blockchain.getFirst();										//Get the genesis block
		if (!isValidBlock(currentBlock,""))												//If the genesis block is not correct...
			return false;
		String previousHashBlock = blockchain.getFirst().getHashBlock();
		for (int i = 1; i < blockchain.size(); i++)										//For all the blocks AFTER the genesis block
		{
			currentBlock = blockchain.get(i);
			if (!isValidBlock(currentBlock,previousHashBlock))							//If block "i" is invalid...
				return false;
			previousHashBlock = blockchain.get(i).getHashBlock();
		}
		return true;																	//If all the blocks are valid then return true
	}
}
