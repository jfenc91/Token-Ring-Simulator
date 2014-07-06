package jeff.tokenRing;

import java.io.IOException;

/**
 * this is the monitor node. It starts a token going on the network and shutsdwon
 * the token ring when requested.
 * @author jeff
 *
 */
public class MonitorNode extends Node {

	//used to indicate that the monitor node should send out a token
	boolean firstStep = true;
	
	//used to request a shutdown of the network
	volatile boolean shutdown = false; 
	//intermediate variable in shutting down
	volatile boolean  shuttingDown = false;
	//indicates a succesfull shutdown
	volatile boolean  isShutdown = false;
	
	/**
	 * generic constructor
	 * @param c 
	 */
	public MonitorNode(NodeConfiguration c) {
		super("monitor", c);
	}

	
	/**
	 * this method is a blocking method that shuts down the network
	 */
	public void shutdown() {
		log.debug("shutting down set");
		shutdown = true;
		

		//wait until the shutdown is complete
		while(!isShutdown){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		log.debug("exiting");
	}
	
	@Override
	public void runStep() {
		if (out != null) {
			//if it is the first step send out a token.
			if ( firstStep ) {
				log.debug("Sending first token");
				try {
					out.write(Frame.defaultTokenFrame(0).marshal());
					isFinished = true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				firstStep = false;
			}
		}

		try {
			if (in != null ) {
	
					byte msg[] = new byte[NodeConfiguration.maxMessageSize]; // max
					// message
					// size
					int totalRead = 0;
					while (totalRead < 5) {
						int read = in.read(msg, totalRead, 5 - totalRead);
						totalRead += read;
					}

					int totalSize = (0x00ff & msg[4]) + 1 + 5;
					while (totalRead < totalSize) {
						int read = in.read(msg, totalRead, totalSize
								- totalRead);
						totalRead += read;
					}

					Frame frame = Frame.unmarshal(msg);
					if (!shutdown) {
						//pass the frame on here because the monitor cannot
						//receive messages
						out.write(frame.marshal());
						try {
							Thread.sleep(3);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						if (frame.isToken() & !shuttingDown) {
							//send out a shut down request
							frame.setShutdownRequested(true);
							out.write(frame.marshal());
							shuttingDown = true;
						} else if (shutdown) {
							//do not pass the message along. 
							//all nodes have been shut down
							log.debug("shut down complete");
							isShutdown = true;
							close();
							run = false;
						}
					}
				
				
			} else {
				try {
					if ( connectedNodes != c.getNodeCount() & !setupComplete) {
						//play nicely while the network is setting up.
						Thread.sleep(100);
					} else if (connectedNodes != c.getNodeCount()) {
						setupComplete = true;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
