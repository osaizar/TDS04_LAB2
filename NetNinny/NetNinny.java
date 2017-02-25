package NetNinny;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class NetNinny{

	final private static int PORT = 8080; //default port, you can change it with args

	public NetNinny(int port, String ip, boolean debug){

		ServerSocket server;
		InetAddress addr;

		System.out.println("[+] Listening to "+ip+":"+port);
		if (debug)System.out.println("[+] Debug mode active, showing all");

		try {
			addr = InetAddress.getByName(ip);
			server = new ServerSocket(port, 50, addr);
			while (true){ //for each new connection, we create a new socket
				new NetThread(server.accept(), debug);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args){
		int port = PORT;
		boolean debug = false;
		String ip = "127.0.0.1";

		for (int i = 0; i < args.length; i++){
			if(args[i].equals("-p")){ // listen to custom port
				port = Integer.parseInt(args[i+1]);
				i++;
			}else if(args[i].equals("-d")){ // debug mode on/off
				debug = true;
			}else if(args[i].equals("-i")){ // listen to custom IP address
				ip = args[i+1];
				i++;
			}
		}

		new NetNinny(port, ip, debug);
	}

}
