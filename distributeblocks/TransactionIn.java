package distributeblocks;

public class TransactionIn {
	public String id_Transaction_Out; // Refers to TransactionOut -> id_Transaction
	public TransactionOut funds; //Your unspent funds

	public TransactionIn(String id_Transaction_Out) {
		this.id_Transaction_Out = id_Transaction_Out;
	}
}
