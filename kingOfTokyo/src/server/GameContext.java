package server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class GameContext {
	private ServerSocket hostSocket;
	private LinkedList<Monster> turnOrder;
	private Set<Monster> allPlayers;
	private Monster tokyoHolder;
	private boolean waiting;
	private int players;
	private DeckStore deckStore;

	public void tellAllPlayers(String message) {
		for (Monster monster : allPlayers) {
			monster.inform(message);
		}
	}

	public enum Target {
		SELF, OTHERS, IN_TOKYO, OUT_TOKYO, ALL
	}
	
	public enum Trigger {
		TURN_START, // any turn starts (mimic, say, modified to only on certain turns)
		RESOLVE_ROLL, // any roll resolves (plot twist)
		CARD_BOUGHT,  // any card is bought (opportunist)
		TURN_END, // end of every turn
		DISCARD_SIGNAL // sent only to discards when bought. #pedantic
	}

	public Set<Monster> getTargetMonsterSet(Target target, Monster self) {
		HashSet<Monster> targetSet = new HashSet<Monster>();
		switch (target) {
		case SELF:
			targetSet.add(self);
			break;
		case OTHERS:
			for (Monster monster : turnOrder) {
				if (!self.equals(monster)) {
					targetSet.add(monster);
				}
			}
			break;
		case IN_TOKYO:
			targetSet.add(tokyoHolder);
			break;
		case OUT_TOKYO:
			for (Monster monster : turnOrder) {
				if (!monster.equals(tokyoHolder)) {
					targetSet.add(monster);
				}
			}
			break;
		case ALL:
			for (Monster monster : turnOrder) {
				targetSet.add(monster);
			}
		}
		return targetSet;
	}

	public void tellEveryoneElse(String message, Monster exception) {
		for (Monster monster : allPlayers) {
			if (!monster.equals(exception)) {
				monster.inform(message);
			}
		}
	}

	private void wakeAllPlayers() {
		for (Monster monster : allPlayers) {
			synchronized (monster) {
				monster.notify();
			}
		}
	}

	public synchronized void host(int port, int players) {
		// Listens for connections, and creates monsters as players connect.

		turnOrder = new LinkedList<Monster>();
		allPlayers = new HashSet<Monster>();
		this.players = players;
		HashSet<Thread> registration = new HashSet<Thread>();
		deckStore = new DeckStore(3, this);
		makeTestCards();

		try {
			int connected = 0;
			hostSocket = new ServerSocket(port);
			while (connected < players) {

				Socket playerSocket = hostSocket.accept();
				Monster newMonster = new Monster(this);
				turnOrder.add(newMonster);
				allPlayers.add(newMonster);
				PlayerThread newPlayer = new PlayerThread(newMonster,
						playerSocket);
				Thread t = new Thread(newPlayer);
				registration.add(t);
				t.start();
				++connected;
			}
			hostSocket.close();
			waiting = true;
			while (waiting) {
				// wait until all monsters are initialized.
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				waiting = false;
				for (Monster monster : allPlayers) {
					if (!monster.isInitialized()) {
						waiting = true;
					}
				}
			}

			tellAllPlayers("All players registered.");
			wakeAllPlayers();

			// Game Loop
			while (true) {
				
				Monster turnHolder = turnOrder.peek();
				doTurn(turnHolder);
				turnOrder.add(turnOrder.pop());
				if (gameIsOver()) {
					break;
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void doTurn(Monster turnHolder) {
		tellAllPlayers(turnHolder.getName() + "'s turn");
		if (turnHolder.equals(tokyoHolder)) {
			turnHolder.score(2);
			tellAllPlayers(turnHolder.getName()
					+ " has held Tokyo and scored 2 points.");
		}
		char[] rollArray = turnHolder.getRoll();
		resolveRoll(turnHolder, rollArray);
		offerShop(turnHolder);
		gameStatusUpdate();
	}

	private void offerShop(Monster monster) {
		int cardNum = 1;
		monster.inform("You have " + monster.getEnergy() + " energy to spend.");
		
		for (Card card : deckStore.getStore()) {
			monster.inform(cardNum + ": " + card.getName() + ", " + card.getCost() + " energy.");
			++cardNum;
		}
		char shopChoice = monster.ask("Type the number of the card to buy, 0 to end shopping, or s to sweep").charAt(0);
		
		int choice = -1;
		switch (shopChoice) {
		case '0': return; 
		case '1': choice = 0; break;
		case '2': choice = 1; break;
		case '3': choice = 2; break;
		case 's': deckStore.sweep();
		}
		if (choice > -1) {
			deckStore.buyCard(monster, choice);
			
		}
		
		offerShop(monster);
	}

	public boolean gameIsOver() {
		int alive = 0;
		for (Monster monster : turnOrder) {
			if (monster.isAlive()) {
				if (monster.getScore() > 19) {
					tellAllPlayers(monster.getName() + " wins by points.");
					return true;
				}
				++alive;
			}
		}
		if (alive + 1 == players) {
			for (Monster monster : turnOrder) {
				if (monster.isAlive()) {
					tellAllPlayers(monster.getName() + " wins by elimination.");
					return true;
				} else {
					turnOrder.remove(monster);

				}
			}
		}
		return false;

	}

	public boolean isKing(Monster monster) {
		if (monster.equals(tokyoHolder)) {
			return true;
		}
		return false;
	}

	public void resolveRoll(Monster roller, char[] roll) {
		int ones = 0;
		int twos = 0;
		int threes = 0;
		int claws = 0;
		int hearts = 0;
		int energy = 0;
		int points = 0;
		for (char c : roll) {
			switch (c) {
			case '1':
				++ones;
				break;
			case '2':
				++twos;
				break;
			case '3':
				++threes;
				break;
			case 'W':
				++claws;
				break;
			case '%':
				++hearts;
				break;
			case 'Z':
				++energy;
				break;
			}
		}
		points += (ones > 2) ? ones - 2 : 0;
		points += (twos > 2) ? twos - 1 : 0;
		points += (threes > 2) ? threes : 0;
		roller.score(points);
		roller.gatherEnergy(energy);
		roller.heal(hearts);
		if (claws > 0) {
			if (roller.equals(tokyoHolder)) {
				attackAll(roller, claws);
			} else if (tokyoHolder == null) {
				tellAllPlayers(roller.getName()
						+ " has claimed the vacant Tokyo, scoring 1 point.");
				tokyoHolder = roller;
				roller.score(1);
			} else {
				tokyoHolder.takeDamage(claws);
				String cede = tokyoHolder.ask(roller.getName()
						+ " has dealt you " + claws
						+ " damage. Will you cede? y/n");
				if (cede.equals("y")) {
					tellAllPlayers(roller.getName() + " has taken Tokyo from "
							+ tokyoHolder.getName() + ", scoring 1 point.");
					tokyoHolder = roller;
					roller.score(1);
				}
			}
		}

		tellAllPlayers(roller.getName() + " results:\npoints: " + points
				+ "\nhearts: " + hearts + "\nenergy: " + energy + "\ndamage: "
				+ claws);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void gameStatusUpdate() {
		for (Monster monster : allPlayers) {
			monsterStatusUpdate(monster);
		}
	}

	public void monsterStatusUpdate(Monster monster) {
		String isHoldingTokyo = "";
		if (isKing(monster)) {
			isHoldingTokyo = ", HOLDING TOKYO";
		}
		tellAllPlayers(monster.getName() + " status: " + monster.getScore()
				+ " points, " + monster.getLife() + " health, "
				+ monster.getEnergy() + " energy" + isHoldingTokyo);
	}

	public void attackAll(Monster attacker, int claws) {
		for (Monster monster : turnOrder) {
			if (!attacker.equals(monster)) {
				monster.takeDamage(claws);
			}
		}
	}

	public void getNames() {
		for (Monster monster : turnOrder) {
			System.out.println(monster.getName());
		}
	}
	
	public void makeTestCards() {
		// fire blast
		PDEHCard fireBlast = new PDEHCard(3, "Fire Blast", this);
		fireBlast.addEffect(Target.OTHERS, 0, 2, 0, 0);
		
		// gas refinery
		PDEHCard gasRefinery = new PDEHCard(6, "Gas Refinery", this);
		gasRefinery.addEffect(Target.OTHERS, 0, 3, 0, 0);
		gasRefinery.addEffect(Target.SELF, 2, 0, 0, 0);
		
		// skyscraper
		PDEHCard skyscraper = new PDEHCard(6, "Skyscraper", this);
		skyscraper.addEffect(Target.SELF, 4, 0, 0, 0);
		
		// militia
		PDEHCard militia = new PDEHCard(3, "Militia", this);
		militia.addEffect(Target.SELF, 2, 2, 0, 0);
		
		deckStore.addCard(fireBlast);
		deckStore.addCard(gasRefinery);
		deckStore.addCard(skyscraper);
		deckStore.addCard(militia);
		deckStore.closeDeck();
	}

	public static void main(String[] args) {
		GameContext test = new GameContext();

		test.host(4343, 3);
		test.getNames();

		try {
			test.hostSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
