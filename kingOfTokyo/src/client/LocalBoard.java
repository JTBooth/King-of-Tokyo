package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Set;

public class LocalBoard {
	LinkedList<Monster> turnOrder;
	Monster tokyoHolder;
	Set<Card> store;
	String hostname;
	int portNumber;
	Socket boardWatcher;
	PrintWriter moveReporter;

	public void connect(String hostname, int portNumber) {
		this.hostname = hostname;
		this.portNumber = portNumber;
		try {
			boardWatcher = new Socket(hostname, portNumber);
			PrintWriter out = new PrintWriter(boardWatcher.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(boardWatcher.getInputStream()));
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
