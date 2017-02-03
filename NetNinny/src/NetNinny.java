import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class NetNinny {
	
	final private static int PORT = 8080; // default port, we have to ask it in the arguments
	
	private ServerSocket listener;
	private Socket clientConn;
	private Socket serverConn;
	
	private String request;
	private String fromAddress;
	private int fromPort;
	
	private String response; 
	private String toAddress;
	private int toPort = 80; //should be 80
	
	public NetNinny(int port){
		startServer(port);
		for(;;){
			getConn();
			sendRequest();
			returnResponse();
			clearSockets();
		}
	}
	
	public void clearSockets(){
		try {
			clientConn.close();
			serverConn.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void returnResponse(){
		try {
			sendString(clientConn, response);
		} catch (IOException e) {
			System.out.println("[*] Can't connect to "+fromAddress);
			e.printStackTrace();
		}	
	}
	
	
	public void sendRequest(){		
		try {
			toAddress = getServerHost();
			serverConn = new Socket(toAddress, toPort);
			sendString(serverConn, request);
			
			System.out.println("Getting response...");
			
			response = getString(serverConn, true);
			
			System.out.println("Response: \n"+response);
			
		} catch (IOException e) {
			System.out.println("[*] Can't connect to "+toAddress);
			e.printStackTrace();
		} finally{
			try {
				serverConn.close();
			} catch (IOException e) {
				System.out.println("[-] Can't close socket.");
			}
		}
	}
	
	public String getServerHost(){
		
		String host = null;
		String split[] = request.split("\n");
		
		for(int i = 0; i < split.length; i++){
			if(split[i].toUpperCase().contains("HOST:")){
				host = split[i].split(" ")[1];
			}
		}
		
		return host;
	}	

	public void getConnData() throws IOException{
		
		request = getString(clientConn, false);
		fromAddress = clientConn.getRemoteSocketAddress().toString().split(":")[0].replaceAll("/", "");
		fromPort = clientConn.getPort();
	}
	
	public void getConn(){
		try {
			clientConn = listener.accept();		
			getConnData();
			
		} catch (IOException e) {
			System.out.println("[-] Connection error.");
		}
	}
	
	public void startServer(int port){
		try {
			listener = new ServerSocket(port);
			System.out.println("[*] Sockets initialized.");
		} catch (IOException e) {
			System.out.println("[-] Error at socket initialization.");
			System.out.println("[-] Aborting..");
			System.exit(-1);
		}
	}
	
	public void sendString(Socket socket, String req) throws IOException{
		
		PrintWriter out;
		
		out = new PrintWriter(socket.getOutputStream(), true);
		System.out.println("Sending:\n"+req);
		out.println(req);
	}
	
	public String getString(Socket socket, boolean isServer) throws IOException{
		
		BufferedReader in;
		String resp = ""; 
		String line = ".";
		
		in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
		
		while(true){
			if(line == null)break;
			if (!isServer)if (line.isEmpty())break;
			
			line = in.readLine();
			resp += line + "\n";
		}
		
		return resp;
	}
	
	
	public static void main(String[] args) {
		new NetNinny(PORT);
	}

}
