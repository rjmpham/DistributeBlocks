import java.util.LinkedList;

public class Validator
{
	//Checks if a given block is valid
	//Currently does not check whether the block satisfies the target
	public static boolean isValidBlock(Block block, String hashPreviousBlock) throws FailedToHashException
	{
		try
		{
			if (!block.getHashBlock().equals(Crypto.calculateBlockHash(block)))				//If the block hash isn't correct...
				return false;
			if (block.getHashData().equals(Crypto.calculateObjectHash(block.getData())))	//If the data hash isn't correct...
				return false;
			if (block.getHashPrevious().equals(hashPreviousBlock))							//If the previous hash isn't correct...
				return false;
			return true;																	//Otherwise return true
		}
		catch (Exception e)
		{
			throw new FailedToHashException(block,e);
		}
	}

	//Checks whether an ArrayList of blocks is valid
	public static boolean isValidBlockchain(LinkedList<Block> blockchain) throws FailedToHashException
	{
		Block currentBlock = blockchain.getFirst();										//Get the genesis block
		if (!isValidBlock(currentBlock,""))												//If the genesis block is not correct...
			return false;
		String previousHashBlock = blockchain.getFirst().getHashBlock();
		for (int i = 1; i < blockchain.size(); i++)										//For all the blocks AFTER the genesis block
		{
			currentBlock = blockchain.get(i);
			if (!isValidBlock(currentBlock,previousHashBlock))							//If block "i" is invalid...
				return false;
			previousHashBlock = blockchain.get(i).getHashBlock();
		}
		return true;																	//If all the blocks are valid then return true
	}
}
