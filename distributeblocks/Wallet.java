package distributeblocks;

import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import distributeblocks.crypto.*;

public class Wallet {

	public PrivateKey privateKey;
	public PublicKey publicKey;
	public HashMap<String,TransactionOut> funds_HashMap = new HashMap<String,TransactionOut>(); //funds in this wallet.

	//Getter methods
	public PrivateKey getPrivateKey(){return privateKey;}
	public PublicKey getPublicKey(){return publicKey;}

	public Wallet(){
	  KeyPair pair = Crypto.keyPairGenerator();
	  privateKey = pair.getPrivate();
	  publicKey = pair.getPublic();
	}

	public float getFunds(){
		float sum = 0;
		for (Map.Entry<String,TransactionOut> i: testDriver.funds_HashMap.entrySet()){
			TransactionOut funds = i.getValue();

			//check to see if the funds have this publicKey as owner
			if(funds.isMine(publicKey)){
				//if yes, account for them as part of the funds
				funds_HashMap.put(funds.id,funds);
				sum += funds.exchange;
			}
		}
		return sum;
	}

	// Makes a new contract from this wallet to send money
	public Transaction makeContract(PublicKey receiver, float amount){
		if(getFunds()< amount){
			//check funds to see if a contract is possible
			System.out.println("Insuficient funds to generate contract.");
			return null;
		}
		// To access contract inputs
		ArrayList<TransactionIn> contract_ArrayList = new ArrayList<TransactionIn>();
		float sum = 0;
		for (Map.Entry<String, TransactionOut> item: funds_HashMap.entrySet()){
			TransactionOut funds = item.getValue();
			sum += funds.getExchange();
			// All funds available have been given to the wallet owner and therefore are exchanges.
			contract_ArrayList.add(new TransactionIn(funds.id));
			if(sum > amount) break;
		}
		Transaction newContract = new Transaction(privateKey, publicKey, receiver , amount, contract_ArrayList);

		//signatures not implemented
		//newContract.generateSignature(privateKey); no longer necessary as contracts are singed in constructor now, delete once confidence is gained

		for(TransactionIn i: contract_ArrayList){
			funds_HashMap.remove(i.id_Transaction_Out);
		}
		return newContract;
	}

}
