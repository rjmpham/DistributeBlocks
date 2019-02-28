package distributeblocks.net;

import java.io.Serializable;
import java.util.Objects;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof IPAddress)) return false;
		IPAddress ipAddress = (IPAddress) o;
		return port == ipAddress.port &&
				ip.equals(ipAddress.ip);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ip, port);
	}
}
