import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;

public class Master extends Thread{
	
	private BufferedReader inputFile;
	private HashMap<String, String[]> callsMap;
	private HashMap<String, Integer> callerPortMap;
	private static final int PORT_NUMBR = 5000;
	private DatagramSocket masterSocket;
	
	public Master(String filePath) {
		try {
			inputFile = new BufferedReader(new FileReader(filePath));
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		callsMap = new HashMap<String, String[]>();
		callerPortMap = new HashMap<String, Integer>();
		try {
			masterSocket = new DatagramSocket(PORT_NUMBR);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void readAndDisplayCalls(){
		String eachEntry;
		int portNumber = 1;
		try {
			System.out.println("** Calls to be made **");
			eachEntry = inputFile.readLine();
			while(eachEntry != null){
				eachEntry = eachEntry.replace("{", "");
				eachEntry = eachEntry.replace("}", "");
				eachEntry = eachEntry.replace(".", "");
				eachEntry = eachEntry.replace("]", "");
				eachEntry = eachEntry.replace("[", "");
				eachEntry = eachEntry.replaceAll("\\s+","");
				String[] callRecord = eachEntry.split(",");
				String caller = callRecord[0];
				String[] callees =  Arrays.copyOfRange(callRecord, 1, callRecord.length);
				callsMap.put(caller, callees);
				callerPortMap.put(caller, portNumber);
				System.out.println(caller + " : " + Arrays.toString(callees));
				portNumber++;
				eachEntry = inputFile.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startContactProcess(){
		for(String callerName : callsMap.keySet()){
			int portNumber = callerPortMap.get(callerName);
			String[] callees = callsMap.get(callerName);
			Contact caller = new Contact(callerName, portNumber, callees, callerPortMap);
			caller.start();
		}
	}
	
	public void startMasterProcess(){
		
		for(String caller : callerPortMap.keySet()){
			
			int calleePortNumber = callerPortMap.get(caller);
			byte[] sendBuffer = "You are initialized".getBytes();
			InetAddress calleeAddress;
			try {
				calleeAddress = InetAddress.getByName("localhost");
				DatagramPacket sendMessage = new DatagramPacket(sendBuffer, sendBuffer.length, calleeAddress, calleePortNumber);
				masterSocket.send(sendMessage);	
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			masterSocket.setSoTimeout(3000);
				while(true){
					byte[] buffer = new byte[1000];
					DatagramPacket incomingMessage = new DatagramPacket(buffer, buffer.length);
					try {
						masterSocket.receive(incomingMessage);
					} catch (SocketTimeoutException  e) {
						masterSocket.close();
						System.out.println("Master has received no replies for 1.5 seconds, ending...");
						System.exit(0);
					}
					String receivedMessage = new String(incomingMessage.getData(), 0, incomingMessage.getLength());
					System.out.println(receivedMessage);
				}	
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	@Override
	public void run() {
		readAndDisplayCalls();	
		startContactProcess();
		startMasterProcess();
	
		
	}
	
	
	
}
