package distributeblocks;

public class TransactionIn {
	private String sourceId; 		// ID of the source TransactionOut
	private TransactionOut funds; 	// Pointer to the source TransactionOut
	private float exchange;			// Amount of funds being used

	public TransactionIn(String sourceId, float exchange) {
		this.sourceId = sourceId;
		this.exchange = exchange;
	}
	
	/*
	 * Returns the exchange value of this transaction
	 */
	public float getExchange() {
		return exchange;
	}
	
	/*
	 * Returns the id of this transaction's source
	 */
	public String getSourceId() {
		return sourceId;
	}
}
