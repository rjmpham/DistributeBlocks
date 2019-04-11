package distributeblocks.net.io;

import distributeblocks.net.IPAddress;

import java.security.PublicKey;

public class PeerInfoMessage {

	private IPAddress address;
	private PublicKey publicKey;
	private String alias;
	
	public PeerInfoMessage(IPAddress address, PublicKey publicKey, String alias)
	{
		this.addr = address;
		this.publicKey = publicKey;
		this.alias = alias;
	}
	
	public IPAddress getAddress() { return addr; }
	public PublicKey getPublicKey() { return publicKey; }
	public IPAddress getAlias() { return alias; }

}
