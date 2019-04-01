package distributeblocks;

public class NodeService {

	private static Node nodeInstance;


	public static void init(Node node){
		nodeInstance = node;
	}

	public static Node getNode(){

		if (nodeInstance == null){
			throw new RuntimeException("Call init for the NodeService before trying to use it.");
		}

		return nodeInstance;
	}

}
