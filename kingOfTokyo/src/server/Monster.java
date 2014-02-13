package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Monster {
	private String name;
	GameContext host;
	private boolean initialized;
	private boolean alive;
	private int score;
	private int health;
	private int energy;
	private Set<KeepCard> keepCards;
	private Socket playerSocket;
	private BufferedReader br;
	private BufferedWriter bw;
	Random random;
	char[] die = { '1', '2', '3', 'W', '%', 'Z' };

	public void setName(String newName) {
		this.name = newName;
	}

	public void setSocket(Socket newSocket) {
		this.playerSocket = newSocket;
	}

	public char[] getRoll() {
		int rerolls = 0;
		String response;
		char[] diceChars = { ' ', ' ', ' ', ' ', ' ', ' ' };
		char[] rerollChars = { ' ', ' ', ' ', ' ', ' ', ' ' };
		diceChars = reroll(diceChars, rerollChars);
		host.tellEveryoneElse(name + " rolled: " + new String(diceChars), this);
		++rerolls;
		while (rerolls < 3) {
			String diceString = new String(diceChars);
			response = ask("Type \'#\' to keep and \' \' to reroll. Your current roll is:\n"
					+ diceString);
			while (!response.matches("[# ]{6}")) {
				response = ask("Try again. Your current roll is: \n"
						+ diceString);
			}
			rerollChars = response.toCharArray();
			host.tellEveryoneElse(name + " kept  : " + new String(rerollChars),
					this);
			diceChars = reroll(diceChars, rerollChars);
			host.tellEveryoneElse(name + " rolled: " + new String(diceChars),
					this);
			inform(new String(diceChars));
			++rerolls;
		}
		return diceChars;
	}

	private char[] reroll(char[] diceChars, char[] rerollChars) {
		int dice = diceChars.length;
		for (int i = 0; i < dice; ++i) {
			if (rerollChars[i] == ' ') {
				diceChars[i] = die[random.nextInt(6)];
			}
		}
		return diceChars;
	}

	public void score(int points) {
		this.score += points;
	}

	public void heal(int health) {
		if (health == 0) {
			return;
		}
		if (!host.isKing(this)) {
			this.health += health;
			if (this.health > 10) {
				this.health = 10;
			}
		} else {
			host.tellAllPlayers(name + " holds Tokyo, and so did not heal " + health + " health");
		}

	}

	public void takeDamage(int claws) {
		this.health -= claws;
		if (this.health < 1) {
			alive = false;
			host.tellAllPlayers(name + " has died.");
		}
	}

	public void gatherEnergy(int energy) {
		this.energy += energy;

	}

	public boolean isAlive() {
		return alive;

	}

	public int getScore() {
		return score;
	}

	public int getLife() {
		return this.health;
	}

	public int getEnergy() {
		return this.energy;
	}
	
	public void gainCard(KeepCard newCard) {
		keepCards.add(newCard);
	}

	public void inform(String message) {
		try {
			bw.write(message + "\n");
			bw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String ask(String question) {
		String response = "";
		try {
			inform(question);
			response = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	public void setIO(BufferedReader br, BufferedWriter bw) {
		this.bw = bw;
		this.br = br;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void initialize() {
		initialized = true;
	}

	public Monster(GameContext host) {
		score = 0;
		health = 10;
		energy = 0;
		keepCards = new HashSet<KeepCard>();
		initialized = false;
		alive = true;
		this.random = new Random();
		this.host = host;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}
}
