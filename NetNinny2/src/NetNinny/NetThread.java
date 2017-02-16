package NetNinny;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class NetThread extends Thread {
	
	final private static String[] URL_BLIST = {"http://www.ida.liu.se/~TDTS04/labs/2011/ass2/SpongeBob.html"};
	final private static String[] CONTENT_BLIST = {"spongebob"};
	
	final private static String R_HOST = "www.ida.liu.se";
	final private static String R_URL = "http://www.ida.liu.se/~TDTS04/labs/2011/ass2/error1.html";
	//final private static String R_CONTENT = "http://www.ida.liu.se/~TDTS04/labs/2011/ass2/error2.html";
	

	final private static int RQ_BUFFER = 1024000;
	final private static int RP_BUFFER = 4096000;
	
	private boolean debug;

	private byte[] request;
	private byte[] response;
	
	private int reqBytes = 0;
	private int respBytes = 0;

	//Client
	private Socket sClient;

	private InputStream fromClient;
	private OutputStream toClient;

	//Server 
	private final int SERVER_PORT = 80;

	private Socket sServer;
	
	private InputStream fromServer;
	private OutputStream toServer;
	
	private String url;
	private String serverHost;



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
		if (getRequest()){
			sendToServer();
			sendToClient();
		}
		System.out.println("[+] ("+this.getName()+") Exiting...");
		return;
	}
	
	private void sendToClient(){
		try {
			toClient.write(response, 0, respBytes);
			respBytes = 0;
			System.out.println("[+] ("+this.getName()+") Response sent to client");
		} catch (IOException e) {
			System.out.println("[-] ("+this.getName()+") Couldn't send response");
		}
	}
	
	private void sendToServer(){
		
		String sResponse = null;
		int respSize = 0;
		byte[] tmpresponse = new byte[RP_BUFFER];
		
		try {
			toServer.write(request, 0, reqBytes);
			reqBytes = 0;
			System.out.println("[+] ("+this.getName()+") Request sent to the server "+sServer.getRemoteSocketAddress());
		} catch (IOException e) {
			System.out.println("[-] ("+this.getName()+") Couldn't send request");
		}
		
		try {
			while ((respSize = fromServer.read(tmpresponse)) != -1){
				response = sumArrays(response, tmpresponse, respBytes, respSize);
				respBytes += respSize;
				sResponse = new String(tmpresponse, StandardCharsets.UTF_8);
				System.out.println("[+] ("+this.getName()+") Got response from server "+sServer.getRemoteSocketAddress()+" (size: "+respBytes+")");
				if (debug) {
					System.out.println("[+] ("+this.getName()+") RESPONSE:");
					System.out.println(sResponse);
					System.out.println("[+] ("+this.getName()+") END RESPONSE");
				}
			}
			
			if (!checkResponse(sResponse)){
				// redirect  to error page
			}
			
			
		} catch (IOException e) {
			System.out.println("[-] ("+this.getName()+") Couldn't get response");
		}
	}
	
	private byte[] sumArrays(byte[] arr1, byte[] arr2, int size1, int size2){
		byte[] rt = new byte[RP_BUFFER];
		
		for(int i = 0; i < size1; i ++){
			rt[i] = arr1[i];
		}
		
		for(int i = 0; i < size2; i++){
			rt[i+size1] = arr2[i];
		}
		
		return rt;
	}
	
	private boolean checkResponse(String sResponse){
		
		String[] lines = sResponse.split(" ");		

		
		for (int i = 0; i < lines.length; i++ ){
			for (int j = 0; j < CONTENT_BLIST.length; j++){
				if (lines[i].toUpperCase().equals(CONTENT_BLIST[j].toUpperCase()))
					return false;
			}
		}
		
		return true;
	}

	private boolean getRequest(){

		String sRequest = null;

		try{
			while ((reqBytes = fromClient.read(request)) != -1){
				sRequest = new String(request, StandardCharsets.UTF_8);
				System.out.println("[+] ("+this.getName()+") Got request from client (size: "+reqBytes+")");
				if (debug){
					System.out.println("[+] ("+this.getName()+") REQUEST:");
					System.out.println(sRequest);
					System.out.println("[+] ("+this.getName()+") END REQUEST");
				}
				break;
			}
			
			if (sRequest == null) return false;
			
			getServerHost(sRequest);
			
			if(!checkURL(sRequest)){
				System.out.println("[+] Bad URL detected!");
				redirectToURLError(sRequest);
			}
			
			getServerConn(sRequest);
			
			return true;
			
		}catch(IOException e){
			System.out.println("[-] ("+this.getName()+") Error getting the response");
			return false;
		}
	}
	
	private void getServerHost(String sRequest){
		String split[] = sRequest.split("\r\n");
		
		for(int i = 0; i < split.length; i++){
			if(split[i].toUpperCase().contains("HOST")){
				serverHost = split[i].split(" ")[1];
				break;
			}
		}
	}
	
	private void redirectToURLError(String sRequest){
				
		System.out.println("URL "+url+" HOSTNAME "+serverHost);
		sRequest = sRequest.replace(serverHost, R_HOST);
		serverHost = R_HOST;
		
		sRequest = sRequest.replaceAll(url, R_URL);
		request = sRequest.getBytes(Charset.forName("UTF-8"));
	}
	
	private boolean checkURL(String sRequest){
		
		String[] lines = sRequest.split("\r\n");
		
		for (int i = 0; i < lines.length; i++){
			if(lines[i].toUpperCase().contains("GET")){
				url = lines[i].split(" ")[1];
				break;
			}
		}
		
		if(url != null){
			for (int i = 0; i < URL_BLIST.length; i++ ){
				if (url.equals(URL_BLIST[i])){
					return false;
				}
			}
			return true;
		}
		return true;
	}
	
	private void getServerConn(String sRequest){
		if (sRequest == null)return;
				
		try {
			sServer = new Socket(serverHost, SERVER_PORT);
			fromServer = sServer.getInputStream();
			toServer = sServer.getOutputStream();
		} catch (IOException e) {
			System.out.println("[-] ("+this.getName()+") Couldn't connect to "+serverHost);
		}
		
	}
}