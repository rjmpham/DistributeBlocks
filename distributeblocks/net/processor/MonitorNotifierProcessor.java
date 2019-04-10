package distributeblocks.net.processor;

import distributeblocks.NetworkMonitor;
import distributeblocks.net.NetworkService;
import distributeblocks.net.message.MonitorNotifierMessage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MonitorNotifierProcessor extends AbstractMessageProcessor<MonitorNotifierMessage> {

	public static Set<Long> receivedIDs = new HashSet<>();


	@Override
	public void processMessage(MonitorNotifierMessage message) {



		NetworkService.getNetworkManager().setMonitorSocket(message);

		//System.out.println("Propogating monitor notify message.");
		if (!receivedIDs.contains(message.id)) {
			NetworkService.getNetworkManager().asyncSendToAllPeers(message.copy());
		}

		receivedIDs.add(message.id);

	}
}
