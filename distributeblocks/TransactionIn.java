package distributeblocks;

public class TransactionIn {
  public String id_Transaction_Out; // Refers to TransactionOut -> ID_Contract
	public TransactionOut funds; //Your unspent funds

	public TransactionIn(String id_Transaction_Out) {
		this.id_Transaction_Out = id_Transaction_Out;
	}
}
