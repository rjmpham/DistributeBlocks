package distributeblocks.net.message;

import distributeblocks.net.processor.AbstractMessageProcessor;

import java.io.Serializable;

public abstract class AbstractMessage implements Serializable {

	public String senderIP;

	/**
	 * Get a processor for the paticular message type.
	 *
	 * @return
	 *   The processor class type that will be used for processing all messages the given type type.
	 */
	public abstract AbstractMessageProcessor getProcessor();

}
