package distributeblocks.io;

import distributeblocks.net.IPAddress;

import java.security.PublicKey;

public class NodeInfo {

	private IPAddress address;
	private PublicKey publicKey;
	private String alias;
	
	public NodeInfo(IPAddress address, PublicKey publicKey, String alias)
	{
		this.addr = address;
		this.publicKey = publicKey;
		this.alias = alias;
	}
	
	public IPAddress getAddress() { return addr; }
	public PublicKey getPublicKey() { return publicKey; }
	public IPAddress getAlias() { return alias; }

}
