package distributeblocks.util;

import java.util.LinkedList;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import distributeblocks.*;
import distributeblocks.crypto.*;
import distributeblocks.io.Console;
import distributeblocks.net.NetworkService;

public class Validator
{	
	
	/**
	 * Compares a transaction against a HashMap of all
	 * verified transactions. The returned ValidationData
	 * will tell whether the transaction is a doubleSpend, and
	 * whether all its inputs exist.
	 * 
	 * @param transaction				transaction to check
	 * @param verifiedTransactions		transactions to check against
	 * @return	ValidationData containing resulting transaction state
	 */
	public static ValidationData getValidationData(Transaction transaction, HashMap<String, Transaction> verifiedTransactions) {
		ValidationData validationData = new ValidationData();
		
		// Get a list of ids from every transaction that has every been spent
		HashSet<String> parentIds =  new HashSet<String>();
		for (Transaction t: verifiedTransactions.values()) {
			for (TransactionIn i: t.getInput()) {
				parentIds.add(i.getParentId());
			}
		}
		
		// for each input used, check if its known, and if it's been seen before
		for (TransactionIn i: transaction.getInput()) {
			if (!verifiedTransactions.containsKey(i.getParentId()))
				validationData.inputsAreKnown = false;
			if (parentIds.contains(i.getParentId()))
				validationData.isDoubleSpend = true;
			
			// break if we've set both booleans (no need to keep looking)
			if (!validationData.inputsAreKnown && validationData.isDoubleSpend)
				break;
		}
		return validationData;
	}
	
	/**
	 * Check to see if the transaction is valid from the point of view of a
	 * local user. This means that this method will not accept any transaction
	 * whose inputs are not known, because a user should not be able to use
	 * inputs it doesn't know about. This method SHOULD NOT be called on transactions
	 * received from other peers, because they may know about verified transactions
	 * we do not.
	 * 
	 * @param transaction			the transaction to check
	 * 
	 * @return true if the transaction's inputs are known and the transaction isn't a double spend,
	 * 		   or true if it is a valid block reward transaction
	 */
	public static boolean isValidTransaction(Transaction transaction) {
		// check if it is a valid block reward
		if(transaction.getPublicKeySender().equals(CoinBase.COIN_BASE_KEYS.getPublic()) 
				&& transaction.getExchange() == CoinBase.BLOCK_REWARD_AMOUNT){
			return true;
		}
		
		// compare the transaction to all that have been verified so far
		ValidationData validationData = getValidationData(transaction, NetworkService.getNetworkManager().getVerifiedTransactions());
		if(!validationData.inputsAreKnown) 
			return false; 
		if(validationData.isDoubleSpend) {
			return false;
		}
		return true;
	}
	
	/**
	 * Checks to see if a transaction is trying to use funds
	 * that have already been used within a verified block of
	 * the blockchain.
	 * 
	 * @param transaction	the transaction to check
	 * 
	 * @return true if the transaction tries to use any already spent funds
	 */
	public static boolean isDoubleSpend(Transaction transaction) {
		// compare the transaction to all that have been verified so far
		ValidationData validationData = getValidationData(transaction, NetworkService.getNetworkManager().getVerifiedTransactions());
		if(validationData.isDoubleSpend) 
			return true;
		else
			return false;
	}
	
	/**
	 * Checks to see if a transaction is using input funds
	 * which are known and verified within a verified block
	 * of the blockchain.
	 * 
	 * @param transaction	the transaction to check
	 * 
	 * @return true if the transaction's inputs are all known
	 */
	public static boolean inputsAreKnown(Transaction transaction) {
		// compare the transaction to all that have been verified so far
		ValidationData validationData = getValidationData(transaction, NetworkService.getNetworkManager().getVerifiedTransactions());
		if(validationData.inputsAreKnown) 
			return true;
		else
			return false;
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
		if (!isValidBlock(currentBlock,""))								//If the genesis block is not correct...
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
