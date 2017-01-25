import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class NetNinny {
	
	private ServerSocket listener;
	private Socket socket;
	private BufferedReader in;
	
	private String request;
	private String fromAddress;
	private int fromPort;
	
	public NetNinny(){
		startServer();
		for(;;){
			getConn();
			sendRequest();
		}
	}
	
	public void sendRequest(){
		
	}
	
	
	public void getConnData() throws IOException{
		String line;
		
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		//while ((line = in.readLine()) != null){
		//	request = line+"\n";
		//}
		
		request = in.readLine();
		fromAddress = socket.getRemoteSocketAddress().toString().split(":")[0].replaceAll("/", "");
		fromPort = socket.getPort();
		
		System.out.println("[+] Got "+request+" from "+fromAddress+":"+fromPort); // debug
	}
	
	public void getConn(){ // on this function we have to get the HTTP request and the data of the client
		try {
			socket = listener.accept();			
			getConnData();
			
		} catch (IOException e) {
			System.out.println("[-] Connection error.");
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				System.out.println("[-] Can't close socket.");
			}
		}
	}
	
	public void startServer(){
		try {
			listener = new ServerSocket(8080);
			System.out.println("[*] Sockets initialized.");
		} catch (IOException e) {
			System.out.println("[-] Error at socket initialization.");
			System.out.println("[-] Aborting..");
			System.exit(-1);
		}
	}

	public static void main(String[] args) {
		new NetNinny();
	}

}
