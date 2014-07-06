package jeff.tokenRing;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Random;

import javax.sql.rowset.spi.SyncResolver;

import org.apache.commons.logging.Log;

/**
 * this is a user node on the network. It sends messages given in the input files.
 * @author jeff
 *
 */
public class UserNode extends Node {
	protected boolean canSend = false;
	
	public UserNode(NodeConfiguration c) {
		super("node", c);
	}

	@Override
	public void runStep() {
		try {
			// we can always try to read from our connection
			if (in != null ) {
				while ( out == null ) {
					Thread.sleep(10);
				}
				// System.out.println(name + " Receiving");
				byte msg[] = new byte[NodeConfiguration.maxMessageSize]; // max
																			// message
																			// size
				
				int totalRead = 0;
				while ( totalRead < 5 ) {
					totalRead += in.read(msg, totalRead , 5 - totalRead );
				}

				int totalSize = (0x00ff & msg[4]) + 1 + 5;
				while ( totalRead < totalSize) {
					totalRead += in.read(msg, totalRead , totalSize - totalRead);
				}
				Frame frame = Frame.unmarshal(msg);

				if (frame.isToken()) {
					log.trace("received Token");
					if (!isFinished) {
						sendMessage(frame);
					} else {
						log.trace("passing token");
						out.write(frame.marshal());
						if ( frame.isShutdownRequested() ) {
							//handles shutting down
							log.debug("Shutting down");
							this.close();
							run = false;
						}
					}

				} else {
					if (frame.getDestinationAddress() == c.getAddress()) {
						// receive the message
						log.trace("receiving Message");
						receiveMessage(frame);
					} else if (frame.getSourceAddress() == c.getAddress()) {
						// drain the frame
						log.trace("draining message");
						drainFrame(frame);	
					} else{
						// pass the message on
						out.write(frame.marshal());
						log.trace("passing Message");
					}

				}
			} else {
				if ( connectedNodes != c.getNodeCount() && !setupComplete) {
					//play nicely while the network is setting up.
					Thread.sleep(100);
				} else if (connectedNodes != c.getNodeCount()) {
					setupComplete = true;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static Integer sent = 0;
	String fullLine = null;  
	int tokenTime = 0;
	private void sendMessage(Frame f) {
		if ( fullLine == null ) {
			fullLine = c.getNextLine();
		}
		

		if ( fullLine != null ) {
			String[] line =fullLine.split(",");
			
			//check if we can send. if not dont send.
			if ( Integer.parseInt(line[1]) + tokenTime > tokenHoldingTime ) {
				tokenHoldingTime = 0;
				try {
					out.write(Frame.defaultTokenFrame(f.getPriority()).marshal());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}
				
			log.trace("sending New message: " + fullLine);
			fullLine = null;
			synchronized(sent) {
				sent++;
				if ( sent % 100 == 0 ){
					log.debug("Sent: " + sent);
				}
			}
			Frame fsend = new Frame(f.getPriority(), f.getPriority(), false, Integer.parseInt(line[0]) -1,  c.getAddress(), line[2], false, false);
			try {
				out.write(fsend.marshal());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			isFinished = true;
			try {
				out.write(Frame.defaultTokenFrame(f.getPriority()).marshal());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	Random r = new Random(0);
	static Integer received = 0;
	private void receiveMessage(Frame f) {
		//fail 1/10 the time
		if ( r.nextInt() % 10 != 0 ) {
			log.trace("successfully received");
			synchronized (received) {
				received++;
				if ( received % 100 == 0 ){
					log.debug("Received: " + received);
				}
			}
			c.writeFrame(f);
			f.setDestReceived(true);
			f.setDestAccepted(true);
			try {
				out.write(f.marshal());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				f.setDestReceived(true);
				f.setDestAccepted(false);
				out.write(f.marshal());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void drainFrame(Frame f) {
		if ( f.isDestAccepted() ) {
			sendMessage(f);

		} else {
			log.trace("Resending Message");
			f.setDestReceived(false);
			try{
				out.write(f.marshal());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
