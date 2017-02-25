package NetNinny;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class NetThread extends Thread{

	final private static String[] URL_BLIST = {"http://www.ida.liu.se/~TDTS04/labs/2011/ass2/SpongeBob.html"};
	final private static String[] CONTENT_BLIST = {"spongebob"};

	final private static String R_URL = "http://www.ida.liu.se/~TDTS04/labs/2011/ass2/error1.html";
	final private static String R_CONTENT = "http://www.ida.liu.se/~TDTS04/labs/2011/ass2/error2.html";

	final private static String R_CONTENT_RESPONSE = "HTTP/1.1 302 Found\r\nLocation: "+R_CONTENT+"\r\n\r\n";
	final private static String R_URL_RESPONSE = "HTTP/1.1 302 Found\r\nLocation: "+R_URL+"\r\n\r\n";

	final private static int RQ_BUFFER = 102400;
	final private static int RP_BUFFER = 409600;

	private boolean debug;

	private byte[] request;
	private byte[] response;

	private int reqSize = 0;
	private int respSize = 0;

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

	private boolean checkContent = true;

//Constructor: Get Client input and output streams and initialize buffers
	public NetThread(Socket sClient, boolean debug){
		//request = new byte[RQ_BUFFER];
		//response = new byte[RP_BUFFER];

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

// Main function of the thread, get request and if correct send it to the server
	@Override
	public void run(){
		System.out.println("[+] ("+this.getName()+") New connection from "+sClient.getRemoteSocketAddress());
		if (getRequest()){
			sendToServer();
		}
		closeSockets();
		System.out.println("[+] ("+this.getName()+") Exiting...");
		return;
	}

// Send response to the client
	private void sendToClient(){
		try {
			toClient.write(response, 0, respSize);
			System.out.println("[+] ("+this.getName()+") Response sent to client");
		} catch (IOException e) {
			e.printStackTrace(); // debug
			System.out.println("[-] ("+this.getName()+") Couldn't send response");
		}
	}

// Send request to the server, get response and send it to the client
	private void sendToServer(){

		String sResponse = null;

		try { // send to server
			toServer.write(request, 0, reqSize);
			System.out.println("[+] ("+this.getName()+") Request sent to the server "+sServer.getRemoteSocketAddress());
		} catch (IOException e) {
			System.out.println("[-] ("+this.getName()+") Couldn't send request");
		}

		try{ // get from server
			while((respSize = fromServer.read(response)) != -1){
				sResponse = new String(response, StandardCharsets.UTF_8);
				System.out.println("[+] ("+this.getName()+") Got response from server "+sServer.getRemoteSocketAddress()+")");

				if (checkContent){
					checkContent = isResponseCheckeable(sResponse);
					if (!checkResponse(sResponse) && checkContent){
						System.out.println("[+] ("+this.getName()+") Bad Content!");
						response = R_CONTENT_RESPONSE.getBytes(Charset.forName("UTF-8"));
						respSize = R_CONTENT_RESPONSE.getBytes(Charset.forName("UTF-8")).length;
					}
				}
				if (debug && checkContent) { // if it's not text, we don't want to see it
					System.out.println("[+] ("+this.getName()+") RESPONSE:");
					System.out.println(sResponse);
					System.out.println("[+] ("+this.getName()+") END RESPONSE");
				}
				sendToClient();
			}
		}catch (IOException e) {
			System.out.println("[-] ("+this.getName()+") Couldn't get response");
		}
	}

private boolean isResponseCheckeable(String sResponse){

	String[] lines = sResponse.split("\n");

	for (int i = 0; i < lines.length; i++ ){
		if (lines[i].toUpperCase().contains("CONTENT-TYPE")){
			if (!lines[i].toUpperCase().contains("TEXT")){
				System.out.println("[-] ("+this.getName()+") Content not worth checking (n text)");
				return false;
			}
		}
		if(lines[i].toUpperCase().contains("CONTENT-ENCODING")){
			if (lines[i].toUpperCase().contains("GZIP")){
				System.out.println("[-] ("+this.getName()+") Content not worth checking (gzip)");
				return false;
			}
		}
	}
	System.out.println("[-] ("+this.getName()+") Content worth checking ?");
	return true;
}

// Check if the response has bad content
	private boolean checkResponse(String sResponse){

		String[] lines = sResponse.split("\n");
		String[] words;

		for (int i = 0; i < lines.length; i++ ){
				words = lines[i].split(" ");
				for (int k = 0; k < words.length; k++){
					for (int j = 0; j < CONTENT_BLIST.length; j++){
						if (words[k].toUpperCase().equals(CONTENT_BLIST[j].toUpperCase()))
							return false;
					}
				}
			}

		return true;
	}

// Get request from client
	private boolean getRequest(){

		String sRequest = null;

		try{
			reqSize = fromClient.read(request);
			sRequest = new String(request, StandardCharsets.UTF_8);
			System.out.println("[+] ("+this.getName()+") Got request from client");
			if (debug){
				System.out.println("[+] ("+this.getName()+") REQUEST:");
				System.out.println(sRequest);
				System.out.println("[+] ("+this.getName()+") END REQUEST");
			}

			getServerHost(sRequest);

			if (serverHost == null) // if we can't get the host, we can't connect to it
				return false;

			if (!checkURL(sRequest)){ // if the URL is not accepted send response
				System.out.println("[-] ("+this.getName()+") BAD URL!");
				response = R_URL_RESPONSE.getBytes(Charset.forName("UTF-8"));
				respSize = R_URL_RESPONSE.getBytes(Charset.forName("UTF-8")).length;
				sendToClient();
				return false;
			}

			getServerConn();

			return true;

		}catch(IOException e){
			System.out.println("[-] ("+this.getName()+") Error getting the response");
			return false;
		}
	}

// Get the connection with the server and create the streams
	private void getServerConn(){

		try {
			sServer = new Socket(serverHost, SERVER_PORT);
			fromServer = sServer.getInputStream();
			toServer = sServer.getOutputStream();
		} catch (IOException e) {
			System.out.println("[-] ("+this.getName()+") Couldn't connect to "+serverHost);
		}
	}

// Check if the request is directed to a bad URL
	private boolean checkURL(String sRequest){

		String[] lines = sRequest.split("\r\n");
		String hostLine = null;

		for (int i = 0; i < lines.length; i++){
			if(lines[i].toUpperCase().contains("GET")){
				if(lines[i].contains("http://"+serverHost)){
					hostLine = lines[i];
				}
				url = lines[i].split(" ")[1];
				break;
			}
		}

		if (hostLine != null){ // GET http://hostname/dir -> GET /dir
			sRequest = sRequest.replace(hostLine, hostLine.replace("http://"+serverHost, ""));
			request = sRequest.getBytes(Charset.forName("UTF-8"));
		}

		if(url == null)
			return true;

		for (int i = 0; i < URL_BLIST.length; i++ ){
			if (url.equals(URL_BLIST[i])){
				return false;
			}
		}
		return true;
	}

// Get hostname of the server
	private void getServerHost(String sRequest){
		String split[] = sRequest.split("\r\n");

		for(int i = 0; i < split.length; i++){
			if(split[i].toUpperCase().contains("HOST")){
				serverHost = split[i].split(" ")[1];
				break;
			}
		}
	}

// Close the used sockets
	private void closeSockets(){
		try {
			if (sClient != null)
				sClient.close();
			if(sServer != null)
				sServer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
