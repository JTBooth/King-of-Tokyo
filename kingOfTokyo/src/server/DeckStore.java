package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import server.GameContext.Trigger;

public class DeckStore {
	private LinkedList<Card> deck;
	private ArrayList<Card> store;
	int storesize;
	boolean closed;
	GameContext host;
	
	public DeckStore(int storeSize, GameContext host) {
		this.deck = new LinkedList<Card>();
		store = new ArrayList<Card>();
		this.storesize = storeSize;
		closed = false;
		this.host = host;
	}
	
	public void addCard(Card card) {
		if (!closed) {
			deck.add(card);
		} else {
			System.out.println("Deck closed. Did not add card.");
		}
	}
	
	public void sweep() {
		store = new ArrayList<Card>();
		for (int i = 0; i < store.size(); ++i) {
			if (!deck.isEmpty()) {
				store.add(deck.pop());
			}
		}
	}
	
	public boolean buyCard(Monster buyer, int index) {
		
		Card card = store.get(index);
		System.out.println("Buyer has " + buyer.getEnergy());
		System.out.println("Card costs " + card.getCost());
		if (buyer.getEnergy() >= card.getCost()) {
			
			buyer.gatherEnergy(-card.getCost());
			card.setOwner(buyer);
			if (card.getClass().isAssignableFrom(KeepCard.class)) {
				buyer.gainCard((KeepCard) card);
			} else {
				card.checkTrigger(Trigger.DISCARD_SIGNAL);
			}
			host.tellAllPlayers(buyer.getName() + " has purchased " + card.getName());
			
			store.remove(index);
		} else {
			buyer.inform("You cannot afford that card.");
			return false;
		}
		// only if a card is bought
		if (!deck.isEmpty()) {
			store.add(deck.pop());
		}
		return true;
	}
	
	public ArrayList<Card> getStore() {
		return store;
	}
	
	public void closeDeck() {
		closed = true;
		Collections.shuffle(deck);
		fillStore();
	}
	
	public void fillStore() {
		for (int i = 0; i < storesize; ++i) {
			if (!deck.isEmpty()) {
				store.add(deck.pop());
			} else {
				return;
			}
		}
	}
}
