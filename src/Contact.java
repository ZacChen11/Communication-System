
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;

public class Contact extends Thread {
	
	private String name;
	private int portNumber;
	private String[] callees;
	private HashMap<String, Integer> calleePortMap;
	DatagramSocket callerSocket;
	private int masterPortNumber;
	private InetAddress masterAddress;
	
	public Contact(String name, int portNumber, String[] callees, HashMap<String, Integer> calleePortMap) {
		this.name = name;
		this.portNumber = portNumber;
		this.callees = callees;
		this.calleePortMap = calleePortMap;
		try {
			callerSocket = new DatagramSocket(this.portNumber);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void run() {		
		startCaller();
	}
	
	public void startCaller(){
		
		try {
			
			callerSocket.setSoTimeout(2000);
			
			while(true){
				
				byte[] receiveBuffer = new byte[1000];
				DatagramPacket request = new DatagramPacket(receiveBuffer, receiveBuffer.length);
				try {
					callerSocket.receive(request);
				}catch (SocketTimeoutException  e) {
					System.out.println("Process " + name + " has received no replies for 1 seconds, ending...");
				}
				String receivedMessage = new String(request.getData(), 0, request.getLength());
		
				if(receivedMessage.contains("You are initialized")){
					masterPortNumber = request.getPort();
					masterAddress = request.getAddress();
					for(String callee : callees){
						int calleePortNumber = calleePortMap.get(callee);
						// send message out
						byte[] sendBuffer = (name + ":calls you").getBytes();
						InetAddress calleeAddress = InetAddress.getByName("localhost");
						DatagramPacket sendMessage = new DatagramPacket(sendBuffer, sendBuffer.length, calleeAddress, calleePortNumber);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						callerSocket.send(sendMessage);
					}
				}
				
				if(receivedMessage.contains("calls you")){
					// send message to master
					String caller = receivedMessage.split(":")[0];
					long timeStamp = System.nanoTime();
//					long timeStamp = System.currentTimeMillis();
					String messageToMaster = name + " received intro message from " + caller + "[" +Long.toString(timeStamp) + "]";
					byte[] sendBufferToMaster = messageToMaster.getBytes();
					DatagramPacket sendMessageToMaster = new DatagramPacket(sendBufferToMaster, sendBufferToMaster.length, masterAddress, masterPortNumber);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					callerSocket.send(sendMessageToMaster);
					
					// send message to caller
					String messageToCaller = name + ":replies to you:[" + Long.toString(timeStamp) + "]";;
					byte[] sendBufferToCaller = messageToCaller.getBytes();
					int callerPortNumber = calleePortMap.get(caller);
					InetAddress callerAddress = request.getAddress();
					DatagramPacket sendMessageToCaller = new DatagramPacket(sendBufferToCaller, sendBufferToCaller.length, callerAddress, callerPortNumber);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					callerSocket.send(sendMessageToCaller);
				}
				
				if(receivedMessage.contains("replies to you")){
					// send message to master
					String callee = receivedMessage.split(":")[0];
					String timeStamp = receivedMessage.split(":")[2];
					String messageToMaster = name + " received reply message from " + callee + timeStamp;
					byte[] sendBufferToMaster = messageToMaster.getBytes();
					DatagramPacket sendMessageToMaster = new DatagramPacket(sendBufferToMaster, sendBufferToMaster.length, masterAddress, masterPortNumber);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					callerSocket.send(sendMessageToMaster);
				}
			}
			
			
			
			
			
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	
}
