package distributeblocks;

public class TransactionIn {
	public String id_Transaction_Out; 	// ID of the source TransactionOut
	public TransactionOut funds; 		// Pointer to the source TransactionOut
	private float exchange;				// Amount of funds being used

	public TransactionIn(String id_Transaction_Out, float exchange) {
		this.id_Transaction_Out = id_Transaction_Out;
		this.exchange = exchange;
	}
	
	/*
	 * Returns the exchange value of this transaction
	 */
	public float getExchange() {
		return exchange;
	}
}
