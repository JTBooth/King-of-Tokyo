package server;

import java.util.Set;

import server.GameContext.Target;

public class TargetedPDEHEffect {
	private Target target;
	private int damage;
	private int energy;
	private int points;
	private int health;
	
	public TargetedPDEHEffect(Target target, int points, int damage, int energy, int health) {
		this.target = target;
		this.damage = damage;
		this.energy = energy;
		this.points = points;
	}
	
	public void execute(Monster self, GameContext game) {
		
		Set<Monster> targets = game.getTargetMonsterSet(target, self);
		for (Monster monster : targets) {
			monster.heal(health);
			monster.takeDamage(damage);
			monster.score(points);
			monster.gatherEnergy(energy);
		}
	}
}
