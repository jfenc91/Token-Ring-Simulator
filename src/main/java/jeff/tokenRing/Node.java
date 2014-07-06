package jeff.tokenRing;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Node class. This represents a generic node on the network. 
 * Its runtime operations are directed by the abstract method takeStep
 * 
 * @author jeff
 *
 */
public abstract class Node {
	Log log = null;
	
	protected static int tokenHoldingTime = 254;
	
	protected static Integer connectedNodes = 0;
	
	protected DataOutputStream out = null;
	private Socket outSocket = null;
	
	protected DataInputStream in = null;
	private ServerSocket inSocket = null;
	private Socket acceptedInSocket = null;
	
	protected NodeConfiguration c = null;
	
	boolean setupComplete = false;

	
	String name;
	
	protected boolean isFinished = false;
	
	protected boolean run = true;
	
	
	/**
	 * creates a new node for  a given configuration
	 * @param name
	 * @param c
	 */
	public Node(String name, NodeConfiguration c) {
		this.c = c;
		//we can send if this is the first node
		
		this.name = name;
		log = LogFactory.getLog(name + ":" +  c.getAddress());
	}
	
	/**
	 * performs the setup for the node
	 */
	public void init() {
		setInSocket();
		setOutSocket();
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				while ( run ) {
					runStep();
				/*	try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
				}
			}
		});
		t.start();
	}
	
	/**
	 * The socket must be connected to once we call this
	 */
	private void setInSocket() {
		//if node reads in data create a new server socket and have it wait for information.
		Thread acceptor = new Thread( new Runnable() {
			public void run() {
				try {
					inSocket = new ServerSocket(c.getInputPort());
					//	System.out.println(name + " is Waiting on port " + c.getInputPort());
					acceptedInSocket = inSocket.accept();
					//System.out.println(name + " accepted a connection.");
					in =  new DataInputStream(acceptedInSocket.getInputStream());
					synchronized (connectedNodes) {
						connectedNodes++;
					}
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					if ( e instanceof BindException) {
						System.err.println("Address localhost:" + c.getInputPort() + " already used.");
						e.printStackTrace();
					} else {
						e.printStackTrace();
					}
				}
			}
		});
		acceptor.start();
	}
	
	/**
	 * connect to another node
	 */
	private void setOutSocket() {
		//if this node needs to write to a server create a socket and try to connect to that server. 
		Thread writor = new Thread(new Runnable() {

			public void run() {
				try {
					boolean isConnected = false;
					for ( int i = 0; !isConnected && i < 3; i++ ) { //we try 3 times and wait 200ms between each retry
						try {
							outSocket = new Socket("localhost", c.getOutputPort());
							isConnected = true;
							//System.out.println(name + " Connected on " + c.getOutputPort());
						} catch (ConnectException e) {
							try {
								Thread.sleep(200);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}

					if ( !isConnected ) {
						System.out.println(name + " Failed to connect on port " + c.getOutputPort());
						throw new ConnectException();
					}
					out = new DataOutputStream(outSocket.getOutputStream());
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		writor.start();
	}
	/**
	 * report if all connections that need to be made have been made
	 * @return
	 */
	public boolean isConnectedInOut() {
		return out != null & in != null;
	}
	
	public boolean isConnectedOut() {
		return out != null;
	}
	
	public boolean isConnectedIn() {
		return in != null;
	}
	
	//do the work of the node
	public abstract void runStep();

	/**
	 * report whether or not we have completed our job.
	 * @return
	 */
	public boolean isFinished() {
		return isFinished;
	}
	
	/**
	 * clean up the resources we used. release all sockets.
	 */
	public void close() {
		if ( in != null ) {
			try {
				in.close();
				acceptedInSocket.close();
				inSocket.close();
				out.close();
				c.out.flush();
				c.out.close();
				out = null;
				in = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if ( out != null ) {
			try {
				out.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				outSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
