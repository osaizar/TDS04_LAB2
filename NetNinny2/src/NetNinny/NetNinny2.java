package NetNinny;

import java.io.IOException;
import java.net.ServerSocket;

public class NetNinny2{

	final private static int PORT = 8080; //default port, you can change it with args

	public NetNinny2(int port, boolean debug){

		ServerSocket server;
		
		System.out.println("[+] Listening to "+port);
		if (debug)System.out.println("[+] Debug mode active, showing all");
		
		try {
			server = new ServerSocket(port);
			while (true){
				new NetThread(server.accept(), debug);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args){
		int port = PORT;
		boolean debug = false;
		
		for (int i = 0; i < args.length; i++){
			if(args[i].equals("-p")){ //custom port
				port = Integer.parseInt(args[i+1]);
				i++;
			}else if(args[i].equals("-d")){ //debug
				debug = true;
			}
		}

		new NetNinny2(port, debug);
	}

}