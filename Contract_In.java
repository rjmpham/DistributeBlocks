public class Contract_In {
  public String id_Contract_Out; // Refers to Contract_Out -> ID_Contract
	public Contract_Out funds; //Your unspent funds

	public Contract_In(String id_Contract_Out) {
		this.id_Contract_Out = id_Contract_Out;
	}
}
