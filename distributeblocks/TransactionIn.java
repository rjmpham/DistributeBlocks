package distributeblocks;

import java.io.Serializable;

/**
 * TransactionIn is used to keep track of a
 * previously created transactions whose funds
 * are being used to create a new transaction.
 * 
 * This is one of the required pieces for a
 * full Transaction.
 */
public class TransactionIn implements Serializable {
	private String sourceId; 		// ID of the source TransactionOut
	private TransactionOut funds; 	// Pointer to the source TransactionOut
	private float exchange;			// Amount of funds being used

	/**
	 * TransactionIn constructor
	 * 
	 * @param sourceId	id of the TransactionOut used
	 * @param exchange	amount from the TransactionOut
	 */
	public TransactionIn(String sourceId, float exchange) {
		this.sourceId = sourceId;
		this.exchange = exchange;
	}
	
	// Getter methods
	public float getExchange() { return exchange; }
	public String getSourceId() { return sourceId; }
}
