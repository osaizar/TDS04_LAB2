import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
	private int toPort; //should be 80
	
	public NetNinny(int port){
		startServer(port);
		for(;;){
			getConn();
			//sendRequest();
			//returnResponse();
			// clearSockets();
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
			serverConn = new Socket(toAddress, toPort);
			
			sendString(serverConn, request);
			response = getString(serverConn);
			
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
	

	public void getConnData() throws IOException{
		
		request = getString(clientConn);
		fromAddress = clientConn.getRemoteSocketAddress().toString().split(":")[0].replaceAll("/", "");
		fromPort = clientConn.getPort();
		
		System.out.println("[+] Got "+request+" from "+fromAddress+":"+fromPort); // debug
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
		BufferedWriter out;
		
		out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		out.write(req);
		out.close();
	}
	
	public String getString(Socket socket) throws IOException{
		
		BufferedReader in;
		String resp = null; 
		String line;
		
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		do{
			line = in.readLine();
			resp = line+"\n";
			System.out.println(line);
		}while(line != "");
		
		System.out.println("Got out");
		
		return resp;
	}

	public static void main(String[] args) {
		new NetNinny(PORT);
	}

}
