package distributeblocks.net.message;

import distributeblocks.Node;
import distributeblocks.net.IPAddress;
import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.MonitorNotifierProcessor;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.UnknownHostException;

public class MonitorNotifierMessage extends AbstractMessage implements Serializable {

	private static long idCount = 0;

	public IPAddress monitorAddress;
	public long id;

	public MonitorNotifierMessage(long id){
		this.id = id;
	}

	public MonitorNotifierMessage() {

		id = idCount ++;

		try {
			monitorAddress = new IPAddress(Inet4Address.getLocalHost().getHostAddress(), Node.MONITOR_PORT);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Override
	public AbstractMessageProcessor getProcessor() {
		return new MonitorNotifierProcessor();
	}


	public MonitorNotifierMessage copy() {
		MonitorNotifierMessage copy = new MonitorNotifierMessage(id);
		copy.monitorAddress = monitorAddress;
		return  copy;
	}
}
