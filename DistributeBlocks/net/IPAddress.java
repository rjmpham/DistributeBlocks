package distributeblocks.net;

import java.io.Serializable;

public class IPAddress implements Serializable {


	public String ip;
	public int port;

	public IPAddress(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	@Override
	public String toString() {
		return ip + ":" + port;
	}
}
