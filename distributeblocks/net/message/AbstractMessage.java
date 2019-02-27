package distributeblocks.net.message;

import distributeblocks.net.IPAddress;
import distributeblocks.net.PeerNode;
import distributeblocks.net.processor.AbstractMessageProcessor;

import java.io.Serializable;


/**
 *
 * README:
 *
 * To add new messages, simply make a class extending AbstractMessage.
 *
 * 		Lets say you make HappyBdayMessage which extends AbstractMessage.
 *
 * 		Step 2 is create a new class extending AbstractMessageProcessor, and set the type to HappyBdayMessage.
 *
 * 			public class HappyBdayProcessor extends AbstractMessageProcessor<HappyBdayMessage>
 *
 * 		Implement getProcessor() from AbstractMessage, returning the new processor.
 *
 * 	This is all you have to do! Incomming messages of that type will automaticaly be handed off to your message processor.
 *
 *
 */
public abstract class AbstractMessage implements Serializable {

	public PeerNode senderNode;

	public AbstractMessage() {


	}

	/**
	 * Get a processor for the paticular message type.
	 *
	 * @return
	 *   The processor class type that will be used for processing all messages the given type type.
	 */
	public abstract AbstractMessageProcessor getProcessor();

}
