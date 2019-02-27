package distributeblocks.net.message;

import distributeblocks.net.processor.AbstractMessageProcessor;
import distributeblocks.net.processor.ServerCrashProcessor;

public class ServerCrashMessage extends AbstractMessage {
	@Override
	public AbstractMessageProcessor getProcessor() {
		return new ServerCrashProcessor();
	}
}
