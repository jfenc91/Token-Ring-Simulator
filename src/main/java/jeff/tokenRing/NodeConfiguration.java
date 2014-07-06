package jeff.tokenRing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

/**
 * this is the base configuration for all nodes.
 * @author jeff
 *
 */
public class NodeConfiguration {
	public static final int maxMessageSize = 254 + 5 + 1;
	protected static int portOffset = 13000;
	protected static int nodeCount= 0;
	

	
	protected int nodeNum = 0;
	
	protected int outputPort;
	protected int inputPort;
	
	
	protected BufferedReader in;
	protected BufferedWriter out;
	
	/**
	 * set the offset. ports are assigned by address + offset
	 * @param offset
	 */
	public static void setPortOffset(int offset) {
		if ( nodeCount == 0 ) {
			portOffset = offset;
		} else {
			throw new UnsupportedOperationException("Error: Nodes Already Created");
		}
	}
	/**
	 * Constructor for node configuraiton
	 * @param pathName the location of the configuration file for the node
	 * @param inputPort The input port to listen to 
	 * @param outputPort The port to write to
	 */
	public NodeConfiguration(String pathPrefix, boolean isLast, boolean hasInput) {
		nodeNum = nodeCount++;
		File f = new File(pathPrefix + "input-file-" + (nodeNum + 1));
		File fout = new File(pathPrefix + "output-file-" + (nodeNum + 1));
		try {			
			if ( hasInput ) {
				in = new BufferedReader(new FileReader(f));
			}
			out = new BufferedWriter(new FileWriter(fout));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.inputPort = portOffset + nodeNum;
		
		if ( isLast ) {
			outputPort = portOffset; //make a ring
		} else {
			this.outputPort = portOffset + nodeNum + 1;
		}
	}
	
	
	public int getNodeCount() {
		return nodeCount;
	}
	/**
	 * gets the next line from the config file. This is our message to send.
	 * @return
	 */
	public String getNextLine() {
		try {
			return in.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void writeFrame( Frame f ) {
		try {
			out.write((f.getSourceAddress() +1)  + "," + (1+f.getDestinationAddress()) + "," + f.getData().length() + "," + f.getData() + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * this is the out port that we were given by the config file
	 * @return
	 */
	public int getOutputPort() {
		return outputPort;
	}
	
	public int getAddress() {
		return nodeNum;
	}

	/**
	 * this is the in port we were given by the config file
	 * @return
	 */
	public int getInputPort() {
		return inputPort;
	}
	
	
	public int getOutputAddress() {
		return outputPort - portOffset;
	}
	
	/**
	 * cleans up resources used by our file reader. 
	 */
	public void close() {
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
