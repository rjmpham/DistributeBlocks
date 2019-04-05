package distributeblocks.net.processor;

import distributeblocks.NetworkMonitor;
import distributeblocks.net.NetworkService;
import distributeblocks.net.message.MonitorNotifierMessage;

public class MonitorNotifierProcessor extends AbstractMessageProcessor<MonitorNotifierMessage> {

	private static long recievedCounter = -1;


	@Override
	public void processMessage(MonitorNotifierMessage message) {


		if (recievedCounter < message.id) {



			if (recievedCounter == -1) {
				NetworkService.getNetworkManager().setMonitorSocket(message);
			}

			recievedCounter = message.id;
			System.out.println("Propogating monitor notify message.");
			NetworkService.getNetworkManager().asyncSendToAllPeers(message.copy());
		} else {
			System.out.println("NOT Propogating monitor notify message.");
		}
	}
}
