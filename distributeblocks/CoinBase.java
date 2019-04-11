package distributeblocks;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;

import distributeblocks.crypto.Crypto;
import distributeblocks.io.Console;
import distributeblocks.io.DirectoryManager;
import distributeblocks.io.WalletManager;

/**
 * The CoinBase provides access to the static keys used for
 * all block reward transactions. Both the private and public
 * keys of this "user" are made available to everyone so that
 * anyone can create blow reward when mining, and anyone can
 * verify a block reward transaction.
 */
public class CoinBase {
	public static final String COIN_BASE_ID = "COIN_BASE";
	public static final String COIN_BASE_DIR = "coinBase";
	public static final KeyPair COIN_BASE_KEYS = loadCoinBase();
	public static final String PARENT_TRANSACTION_ID = "blockReward"; // TODO: does this cause any hashing conficts?
	public static final float BLOCK_REWARD_AMOUNT = 5.0f;
	
	/**
	 * Loads the COIN_BASE_KEYS from the COIN_BASE_DIR.
	 * 
	 * @return	a KeyPair loaded for the coinBase
	 */
	public static KeyPair loadCoinBase() {
		String fullPath = DirectoryManager.fullPathToDir(COIN_BASE_DIR);
		try {
			KeyPair keyPair = WalletManager.loadKeyPair(fullPath, Crypto.GEN_ALGORITHM);
			System.out.println("successfully created the coinbase");
			return keyPair;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			System.out.println("Warning: failed to load CoinBase keys from " + fullPath);
			System.out.println("This node cannot mine as no block rewards can be made!");
			return null;
		}
	}
	
	/**
	 * Makes a new transaction from the COIN_BASE.
	 * The block reward transaction can go to any PublicKey, but
	 * is usually given to the creator of the block (calling node).
	 * 
	 * @param receiver		PublicKey of the receiver
	 * 
	 * @return a block reward Transaction
	 */
	public static Transaction makeBlockReward(PublicKey receiver) {
		if (CoinBase.COIN_BASE_KEYS == null) {
			Console.log("CoinBase has not been loaded! Cannot create block reward!");
			throw new NullPointerException();
		}

		TransactionResult reward = null;
		try {
			// TransactionIn comes from the CoinBase
			ArrayList<String> source = new ArrayList<String>(Arrays.asList(CoinBase.PARENT_TRANSACTION_ID));
			reward = new TransactionResult(receiver, CoinBase.BLOCK_REWARD_AMOUNT, CoinBase.COIN_BASE_ID, source);
		} catch (FailedToHashException e) {
			Console.log("Failed to hash reward transaction");
			throw new NullPointerException();
		}
		
		ArrayList<TransactionResult> transaction_ArrayList = new ArrayList<TransactionResult>();
		transaction_ArrayList.add(reward);

		// Create a block reward Transaction, gives coins to the receiver
		Transaction newTransaction = new Transaction(CoinBase.COIN_BASE_KEYS.getPrivate(),
				CoinBase.COIN_BASE_KEYS.getPublic(),
				receiver,
				CoinBase.BLOCK_REWARD_AMOUNT,
				transaction_ArrayList);
		return newTransaction;
	}
}
