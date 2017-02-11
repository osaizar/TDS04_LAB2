package NetNinny;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class NetThread extends Thread {

	final private static int RQ_BUFFER = 1024;
	final private static int RP_BUFFER = 4096;

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



	public NetThread(Socket sClient){

		request = new byte[RQ_BUFFER];
		response = new byte[RP_BUFFER];

		this.sClient = sClient;
		

		try{
			this.fromClient = sClient.getInputStream();
			this.toClient = sClient.getOutputStream();
		}catch(IOException e){
			System.out.println("[-] Error connecting to client");
			e.printStackTrace();
		}
		

		this.start();
	}


	@Override
	public void run(){
		System.out.println("[+] New connection from "+sClient.getRemoteSocketAddress());
		getRequest();
		sendToServer();
		sendToClient();
	}
	
	private void sendToClient(){
		try {
			toClient.write(response, 0, bytes);
		} catch (IOException e) {
			System.out.println("[-] Couldn't send response");
			e.printStackTrace();
		}
	}
	
	private void sendToServer(){
		
		String sResponse = null;
		
		try {
			toServer.write(request, 0, bytes);
		} catch (IOException e) {
			System.out.println("[-] Couldn't send request");
			e.printStackTrace();
		}
		
		try {
			while ((bytes = fromServer.read(response)) != -1){
				sResponse = new String(response, StandardCharsets.UTF_8);
				System.out.println(sResponse); // debug
				break;
			}
		} catch (IOException e) {
			System.out.println("[-] Couldn't get response");
			e.printStackTrace();
		}
	}

	private void getRequest(){

		String sRequest = null;

		try{
			while ((bytes = fromClient.read(request)) != -1){
				sRequest = new String(request, StandardCharsets.UTF_8);
				System.out.println(sRequest); // debug
				break;
			}
			
			getServerConn(sRequest);
			
		}catch(IOException e){
			System.out.println("[-] Error getting the response");
			e.printStackTrace();
		}
	}
	
	private void getServerConn(String sRequest){
		if (sRequest == null)return;
		
		String serverHost = null;
		String split[] = sRequest.split("\n");
		
		for(int i = 0; i < split.length; i++){
			if(split[i].toUpperCase().contains("HOST:")){
				serverHost = split[i].split(" ")[1];
				break;
			}
		}
		
		try {
			sServer = new Socket(serverHost, SERVER_PORT); //couldn't connect!
			fromServer = sServer.getInputStream();
			toServer = sServer.getOutputStream();
			
			System.out.println("[+] Connected to "+serverHost);
		} catch (IOException e) {
			System.out.println("[-] Couldn't connect to "+serverHost);
			e.printStackTrace();
		}
		
	}
}
