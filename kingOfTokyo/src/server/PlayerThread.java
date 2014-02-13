package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class PlayerThread implements Runnable {
	Monster monster;
	Socket playerSocket;
	
	

	public PlayerThread(Monster newMonster, Socket playerSocket) {
		this.monster = newMonster;
		this.playerSocket = playerSocket;
	}

	

	

	@Override
	public void run() {
		synchronized (monster) {
			try {
				// Setup
				BufferedWriter playerWriter = new BufferedWriter(
						new OutputStreamWriter(playerSocket.getOutputStream()));
				BufferedReader playerReader = new BufferedReader(
						new InputStreamReader(playerSocket.getInputStream()));
				

				// Create your monster by choosing his name.

				playerWriter.write("Please enter your Monster Name: ");
				playerWriter.flush();
				String name = playerReader.readLine();
				monster.setName(name);
				monster.setSocket(playerSocket);
				monster.setIO(playerReader, playerWriter);
				monster.initialize();
				monster.inform("Waiting for other players to register");

				//
				while (true) {
					try {
						monster.wait();
					} catch (InterruptedException e) {
						// I don't know why this is an exception. I just want to wake up.
					}
				}
				

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}
