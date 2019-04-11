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

// TODO: complete block validation, and add javadocs to it
public class Validator
{	
	
	/**
	 * Compares a transaction against a HashMap of all
	 * verified transaction results. The returned ValidationData
	 * will tell whether the transaction is a doubleSpend, and
	 * whether all its inputs exist.
	 * 
	 * @param transaction				transaction to check
	 * @param verifiedTransactions		transaction results to check against
	 * 
	 * @return	ValidationData containing resulting transaction state
	 */
	public static ValidationData getValidationData(Transaction transaction, HashMap<String, TransactionResult> verifiedTransactions) {
		ValidationData validationData = new ValidationData();
		
		// Get a list of ids from every transaction that has every been spent
		HashSet<String> parentIds =  new HashSet<String>();
		for (TransactionResult t: verifiedTransactions.values()) {
			parentIds.addAll(t.getSourceIds());
		}


		
		// for each input used, check if its known, and if it's been seen before
		for (TransactionResult i: transaction.getInput()) {
			for(String key: i.getSourceIds()) {
				if (!verifiedTransactions.containsKey(key))
					validationData.inputsAreKnown = false;
				if (parentIds.contains(key))
					validationData.isDoubleSpend = true;
			
				// break if we've set both booleans (no need to keep looking)
				if (!validationData.inputsAreKnown && validationData.isDoubleSpend)
					break;
			}
		}
		return validationData;
	}
	
	/**
	 * Compares a transaction against a HashMap of all
	 * verified transactions. The returned ValidationData
	 * will tell whether the transaction is a doubleSpend, and
	 * whether all its inputs exist.
	 * 
	 * @param transaction				transaction to check
	 * @param verifiedTransactions		transactions to check against
	 * 
	 * @return	ValidationData containing resulting transaction state
	 */
	public static ValidationData getValidationDataAlt(Transaction transaction, HashMap<String, Transaction> verifiedTransactions) {
		HashMap<String, TransactionResult> verifiedTransactionResults = new HashMap<>();
		
		for(Transaction t: verifiedTransactions.values()) {
			for(TransactionResult r: t.getInput()) {
				verifiedTransactionResults.put(r.getId(), r);
			}
		}
		return getValidationData(transaction, verifiedTransactionResults);
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
		Console.log("Validating transaction " + transaction.getTransactionId());
		// check if it is a valid block reward
		if(transaction.getPublicKeySender().equals(CoinBase.COIN_BASE_KEYS.getPublic()) 
				&& transaction.getExchange() == CoinBase.BLOCK_REWARD_AMOUNT){
			return true;
		}
		
		// compare the transaction to all that have been verified so far
		BlockChain blockChain = new BlockChain();
		ValidationData validationData = getValidationData(transaction, blockChain.getAllTransactionResults());
		if(!validationData.inputsAreKnown) {
			Console.log("Unknown inputs for transaction " + transaction.getTransactionId());
			return false;
		} 
		if(validationData.isDoubleSpend) {
			Console.log("Double spend attempt detected on transaction " + transaction.getTransactionId());
			return false;
		}

		if (validationData.alreadyOnBlock){
			Console.log("Block was already on the chain for transaction  " + transaction.getTransactionId());
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
		ValidationData validationData = getValidationData(transaction, (new BlockChain()).getAllTransactionResults());
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
		ValidationData validationData = getValidationData(transaction, (new BlockChain()).getAllTransactionResults());
		if(validationData.inputsAreKnown) 
			return true;
		else
			return false;
	}

	/**
	 * Checks if a given block is valid
	 * Currently does not check whether the block satisfies the target
	 *
	 * @param block is the block to check
	 * @param verifiedTransactions is a list of transactions that appear before said block on the fork of said block
	 * @return true if the block has all the required variables
	 * @throws FailedToHashException happens when the crypto methods fails to use their hashing algorithm
	 */
	public static boolean isValidBlock(Block block,HashMap<String, Transaction> verifiedTransactions) throws FailedToHashException
	{
		try {
			if (!block.getHashBlock().equals(Crypto.calculateBlockHash(block))) {       	 	  //If the block hash isn't correct
				return false;
			}
			if (!(block.getHashData().equals(Crypto.calculateObjectHash(block.getData())))) {     //If the data hash isn't correct
				return false;
			}
			if (!(block.getTargetNumZeros()==Node.HASH_DIFFICULTY)){                              //if the hash difficulty is different
				return false;
			}
			if (!block.isBlockMined())    {     							 			       	  //If the block nonce is off
				return false;
			}

			/* run a for loop that checks every transaction on the block to check it's validity against
			 * strictly OLDER blocks
			 */
			int blockReward = 0;
			HashMap<String, Transaction> data = block.getData();
			for (Transaction t : data.values()){


				//if the transaction is a valid coinbase transaction
				if (Crypto.verifySignature(CoinBase.COIN_BASE_KEYS.getPublic(),t.getTransactionId(),t.getSignature())){
					blockReward++;
				}
				if (blockReward>1){
					return false;
				}

				/* Use the validation data to determine if the transaction is valid or not
				 */
				ValidationData validationData = getValidationData(t, verifiedTransactions);
				if(!validationData.inputsAreKnown){
					return false;
				}
				if(!validationData.isDoubleSpend){
					return false;
				}

			}

			return true;													       			      //Otherwise return true
		}
		catch (Exception e)
		{
			return false;   
		}
	}
}
