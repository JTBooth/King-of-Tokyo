package server;

abstract class KeepCard implements Card {
	private String rulesText;
	private String name;
	private int cost;
	
	public abstract void use();
	
	public String getName() {
		return name;
	}
	
	public String getRulesText() {
		return rulesText;
	}
	
	public int getCost() {
		return cost;
	}
}
