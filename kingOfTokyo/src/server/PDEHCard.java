package server;

import java.util.HashSet;
import java.util.Set;

import server.GameContext.Target;
import server.GameContext.Trigger;

public class PDEHCard extends DiscardCard {
	Set<TargetedPDEHEffect> effects;
	int cost;
	String name;
	GameContext game;
	
	public String getName() {
		return name;
	}
	
	public int getCost() {
		return cost;
	}
	
	public void checkTrigger(Trigger trigger) {
		if (trigger == Trigger.DISCARD_SIGNAL) {
			execute(owner);
		}
	}
	
	public PDEHCard(int cost, String name, GameContext game) {
		this.cost = cost;
		this.name = name;
		this.game = game;
		effects = new HashSet<TargetedPDEHEffect>();
	}
	
	public void addEffect(Target target, int points, int damage, int energy, int health) {
		TargetedPDEHEffect newEffect = new TargetedPDEHEffect(target, points, damage, energy, health);
		effects.add(newEffect);
	}
	
	public void execute(Monster activator) {
		game.tellAllPlayers(activator.getName() + " has activated " + name);
		for (TargetedPDEHEffect effect : effects) {
			effect.execute(activator, game);
		}
	}

	@Override
	public void setOwner(Monster owner) {
		this.owner = owner;
	}
}
