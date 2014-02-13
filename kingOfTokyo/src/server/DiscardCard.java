package server;

public abstract class DiscardCard implements Card {
	String name;
	String rulesText;
	int cost;
	Monster owner;
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getRulesText() {
		return rulesText;
	}

	@Override
	public int getCost() {
		return cost;
	}

}
