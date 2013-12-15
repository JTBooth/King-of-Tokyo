package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;

public class GameContext {
	private ServerSocket hostSocket;
	// private Set<Socket> playerSockets;
	Socket playerSocket;
	
	public void host(int port) {
		try {
			hostSocket = new ServerSocket(port);
			Socket playerSocket = hostSocket.accept();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
