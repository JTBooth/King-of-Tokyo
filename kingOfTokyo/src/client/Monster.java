package client;

import java.util.HashSet;
import java.util.Set;

public class Monster {
	private String name;
	private boolean locallyOwned;
	private int score;
	private int health;
	private int energy;
	private int dice;
	private Set<KeepCard> keepCards;
	
	public Monster(boolean locallyOwned, String name) {
		this.locallyOwned = locallyOwned;
		this.name = name;
		score = 0;
		health = 10;
		energy = 0;
		dice = 6;
		keepCards = new HashSet<KeepCard>();
	}
}
