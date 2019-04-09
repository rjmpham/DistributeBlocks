package distributeblocks;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import distributeblocks.crypto.Crypto;
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
	public static final String COIN_BASE_DIR = "./coinBase/";
	public static final KeyPair COIN_BASE_KEYS = loadCoinBase();
	public static final String PARENT_TRANSACTION_ID = "blockReward"; // TODO: does this cause any hashing conficts?
	public static final float BLOCK_REWARD_AMOUNT = 5.0f;
	
	/**
	 * Loads the COIN_BASE_KEYS from the COIN_BASE_DIR.
	 * 
	 * @return	a KeyPair loaded for the coinBase
	 */
	public static KeyPair loadCoinBase() {
		System.out.println("creating the coinbase");
		String fullPath = System.getProperty("user.dir") + COIN_BASE_DIR;
		try {
			return WalletManager.loadKeyPair(fullPath, Crypto.GEN_ALGORITHM);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			System.out.println("Warning: failed to load CoinBase keys from " + fullPath);
			System.out.println("This node cannot mine as no block rewards can be made!");
			return null;
		}
	}
}
