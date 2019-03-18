package distributeblocks;

import java.io.Serializable;

public class BlockHeader implements Serializable {


	// Not actualy sure whats supposed to go in here...
	public String blockHash;
	public int blockHeight;

	public BlockHeader(String blockHash, int blockHeight) {
		this.blockHash = blockHash;
		this.blockHeight = blockHeight;
	}
}
