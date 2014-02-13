package server;

import server.GameContext.Trigger;

abstract interface Card {
	public String getName();
	public String getRulesText();
	public int getCost();
	public void setOwner(Monster owner);
	public void checkTrigger(Trigger trigger);
}
