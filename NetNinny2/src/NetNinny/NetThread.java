package NetNinny;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class NetThread extends Thread {

	final private static int RQ_BUFFER = 1024;
	final private static int RP_BUFFER = 4096;
	
	private boolean debug;

	private byte[] request;
	private byte[] response;
	
	private int bytes;

	//Client
	private Socket sClient;

	private InputStream fromClient;
	private OutputStream toClient;

	//Server 
	private final int SERVER_PORT = 80;

	private Socket sServer;
	
	private InputStream fromServer;
	private OutputStream toServer;



	public NetThread(Socket sClient, boolean debug){
		
		request = new byte[RQ_BUFFER];
		response = new byte[RP_BUFFER];

		this.sClient = sClient;
		this.debug = debug;
		

		try{
			this.fromClient = sClient.getInputStream();
			this.toClient = sClient.getOutputStream();
		}catch(IOException e){
			System.out.println("[-] ("+this.getName()+") Error connecting to client");
		}
		

		this.start();
	}


	@Override
	public void run(){
		System.out.println("[+] ("+this.getName()+") New connection from "+sClient.getRemoteSocketAddress());
		getRequest();
		sendToServer();
		sendToClient();
		return;
	}
	
	private void sendToClient(){
		try {
			toClient.write(response, 0, bytes);
			System.out.println("[+] ("+this.getName()+") Response sent to client");
		} catch (IOException e) {
			System.out.println("[-] ("+this.getName()+") Couldn't send response");
		}
	}
	
	private void sendToServer(){
		
		String sResponse = null;
		
		try {
			toServer.write(request, 0, bytes);
			System.out.println("[+] ("+this.getName()+") Request sent to the server "+sServer.getRemoteSocketAddress());
		} catch (IOException e) {
			System.out.println("[-] ("+this.getName()+") Couldn't send request");
		}
		
		try {
			while ((bytes = fromServer.read(response)) != -1){
				sResponse = new String(response, StandardCharsets.UTF_8);
				if (debug) System.out.println(sResponse);
				System.out.println("[+] ("+this.getName()+") Got response from server "+sServer.getRemoteSocketAddress());
				break;
			}
		} catch (IOException e) {
			System.out.println("[-] ("+this.getName()+") Couldn't get response");
		}
	}

	private void getRequest(){

		String sRequest = null;

		try{
			while ((bytes = fromClient.read(request)) != -1){
				sRequest = new String(request, StandardCharsets.UTF_8);
				if (debug) System.out.println(sRequest); // debug
				System.out.println("[+] ("+this.getName()+") Got request from client");
				break;
			}
			
			getServerConn(sRequest);
			
		}catch(IOException e){
			System.out.println("[-] ("+this.getName()+") Error getting the response");
		}
	}
	
	private void getServerConn(String sRequest){
		if (sRequest == null)return;
		
		String serverHost = null;
		String split[] = sRequest.split("\r\n");
		
		for(int i = 0; i < split.length; i++){
			if(split[i].toUpperCase().contains("HOS")){
				serverHost = split[i].split(" ")[1];
				break;
			}
		}
		
		try {
			sServer = new Socket(serverHost, SERVER_PORT);
			fromServer = sServer.getInputStream();
			toServer = sServer.getOutputStream();
		} catch (IOException e) {
			System.out.println("[-] ("+this.getName()+") Couldn't connect to "+serverHost);
		}
		
	}
}