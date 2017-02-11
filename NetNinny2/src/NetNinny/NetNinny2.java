package NetNinny;

import java.io.IOException;
import java.net.ServerSocket;

public class NetNinny2{

	final private static int PORT = 8080; //default port, you can change it with args

	public NetNinny2(int port){

		ServerSocket server;
		
		System.out.println("[+] Listening to "+port);
		
		try {
			server = new ServerSocket(port);
			while (true){
				new NetThread(server.accept());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args){
		int port = PORT;

		if(args.length > 1){
			if(args[0].equals("-p")){
				port = Integer.parseInt(args[1]);
			}
		}

		new NetNinny2(port);
	}

}