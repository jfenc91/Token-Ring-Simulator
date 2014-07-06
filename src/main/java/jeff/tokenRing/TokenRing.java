package jeff.tokenRing;

import java.util.ArrayList;
import java.util.Collection;

/**
 * this is the main class. it sets up and monitors all nodes then requests a shut down
 * @author jeff
 *
 */
public class TokenRing {
	//collection of all the nodes
	Collection<Node> nodes = new ArrayList<Node>();
	
	int totalNodes = 10;
	
	
	MonitorNode monitor = null;
	

	/**
	 * constructor to set the total nodes
	 */
	public TokenRing(int totalNodes) {
		this.totalNodes = totalNodes;
	}
	
	/**
	 * Initializes the nodes required for this project
	 */
	public void initNodes() {
		for ( int i = 0; i < totalNodes; i++ ) {
			NodeConfiguration c = new NodeConfiguration("input/", false, true);
			Node n = new UserNode(c);
			nodes.add(n);
		}
		
		NodeConfiguration c = new NodeConfiguration("input/", true, false);
		nodes.add(monitor = new MonitorNode(c));
	}
	
	/**
	 * starts a thread to run each node
	 */
	public void startNodes() { 
		for ( final Node n : nodes ) {
			n.init();
		}
	}
	
	/**
	 * Checks to see if all nodes are finished
	 * @return
	 */
	public boolean isFinished() {
		for ( Node n : nodes ) {
			if ( !n.isFinished() ) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * shuts down all nodes. Hopefully this is called after completion of everything
	 */
	public void closeNetwork() {
		monitor.shutdown();
	}
	
	/**
	 * Main program
	 * 
	 * @param args argument 0 should be the number of nodes
	 */
	public static void main(String[] args) {
		TokenRing n = new TokenRing(Integer.parseInt(args[0]));
		
		//initialize and start nodes
		n.initNodes();
		n.startNodes();
	
		//wait until everyone is done
		while ( !n.isFinished() ) {
			try {
				//System.out.println("Waiting for finished");
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		//clean things up
		n.closeNetwork();
	}
	
}
