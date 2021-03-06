/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.hmdzl.spspd.change.actors.mobs;

import java.util.HashSet;

import com.hmdzl.spspd.change.messages.Messages;
import com.hmdzl.spspd.change.Dungeon;
import com.hmdzl.spspd.change.ResultDescriptions;
import com.hmdzl.spspd.change.actors.Actor;
import com.hmdzl.spspd.change.actors.Char;
import com.hmdzl.spspd.change.actors.blobs.ToxicGas;
import com.hmdzl.spspd.change.actors.buffs.Terror;
import com.hmdzl.spspd.change.effects.CellEmitter;
import com.hmdzl.spspd.change.effects.particles.PurpleParticle;
import com.hmdzl.spspd.change.items.OrbOfZot;
import com.hmdzl.spspd.change.items.weapon.enchantments.Death;
import com.hmdzl.spspd.change.items.weapon.enchantments.Leech;
import com.hmdzl.spspd.change.levels.Level;
import com.hmdzl.spspd.change.mechanics.Ballistica;
import com.hmdzl.spspd.change.scenes.GameScene;
import com.hmdzl.spspd.change.sprites.CharSprite;
import com.hmdzl.spspd.change.sprites.OrbOfZotSprite;
import com.hmdzl.spspd.change.utils.GLog;
 
import com.watabou.utils.Random;

public class OrbOfZotMob extends Mob {

	private static final String TXT_DEATHGAZE_KILLED = "%s's deathray killed you...";

	{
		spriteClass = OrbOfZotSprite.class;
		hostile = false;
		state = HUNTING;
		HP = HT= 500;
		defenseSkill = 35;	
		
		properties.add(Property.MECH);
	}

	private Ballistica beam;
	
	private static final float SPAWN_DELAY = 1f;
	
    
	@Override
	public int dr() {
		return 55;
	}

	
	@Override
	protected boolean act() {
		
		for (int n : Level.NEIGHBOURS8DIST2) {
			int c = pos + n;
			if (c<Level.getLength() && c>0){
			    Char ch = Actor.findChar(c);
			}
		}
		//Level.fieldOfView[Dungeon.hero.pos] &&
		
		boolean result = super.act();
		return result;
	}
	
	@Override
	public void move(int step) {		
	}
		
	@Override
	protected Char chooseEnemy() {

		if (enemy == null || !enemy.isAlive()) {
			HashSet<Mob> enemies = new HashSet<Mob>();
			for (Mob mob : Dungeon.level.mobs) {
				if (mob.hostile && Level.fieldOfView[mob.pos]) {
					enemies.add(mob);
				}
			}

			enemy = enemies.size() > 0 ? Random.element(enemies) : null;
		}

		return enemy;
	}

	
	
    public static OrbOfZotMob spawnAt(int pos) {
		
    	OrbOfZotMob b = new OrbOfZotMob();  
    	
			b.pos = pos;
			b.state = b.HUNTING;
			GameScene.add(b, SPAWN_DELAY);

			return b;
     
     }
	
	private int hitCell;

	@Override
	protected boolean canAttack(Char enemy) {

		beam = new Ballistica( pos, enemy.pos, Ballistica.STOP_TERRAIN);

		return beam.subPath(1, beam.dist).contains(enemy.pos);
	}

	@Override
	public int attackSkill(Char target) {
		return 70+(Dungeon.depth);
	}
	
	@Override
	protected boolean doAttack(Char enemy) {

		spend(attackDelay());

		boolean rayVisible = false;

		for (int i : beam.subPath(0, beam.dist)) {
			if (Dungeon.visible[i]) {
				rayVisible = true;
			}
		}
		
		if (rayVisible) {
			sprite.attack( beam.collisionPos );
			return false;
		} else {
			attack(enemy);
			return true;
		}
	}

	@Override
	public boolean attack(Char enemy) {

		for (int pos : beam.subPath(1, beam.dist)) {

			Char ch = Actor.findChar( pos );
			if (ch == null) {
				continue;
			}

			if (hit(this, ch, true)) {
				ch.damage(Random.NormalIntRange(100, 200), this);
				damage(Random.NormalIntRange(10, 20), this);
				//Dungeon.hero.earnExp(5);
				//Dungeon.zotDrains++;
				//if(Dungeon.zotDrains>=100){
				//	Dungeon.hero.STR++;
				//	Dungeon.hero.HT += 5;
				//	Dungeon.hero.HP += 5;
				//	Dungeon.hero.sprite.showStatus(CharSprite.POSITIVE, "+1 str, +5 ht");
				//	Dungeon.zotDrains=0;
				//}

				if (Dungeon.visible[pos]) {
					ch.sprite.flash();
					CellEmitter.center(pos).burst(PurpleParticle.BURST,
							Random.IntRange(1, 2));
				}

				if (!ch.isAlive() && ch == Dungeon.hero) {
					Dungeon.fail(Messages.format(ResultDescriptions.MOB));
					//GLog.n(Messages.get(this, "kill"));
				}
			} else {
				ch.sprite.showStatus(CharSprite.NEUTRAL, ch.defenseVerb());
			}
		}

		return true;
	}

	@Override
	public void beckon(int cell) {
	}
	
	@Override
	public void die(Object cause) {

		yell(Messages.get(this, "die"));
		Dungeon.level.drop(new OrbOfZot(), pos);
		super.die(cause);
	}	
	
	private static final HashSet<Class<?>> RESISTANCES = new HashSet<Class<?>>();
	static {
		RESISTANCES.add(Death.class);
		RESISTANCES.add(Leech.class);
	}

	@Override
	public HashSet<Class<?>> resistances() {
		return RESISTANCES;
	}

	private static final HashSet<Class<?>> IMMUNITIES = new HashSet<Class<?>>();
	static {
		IMMUNITIES.add(Terror.class);
		IMMUNITIES.add(ToxicGas.class);
	}

	@Override
	public HashSet<Class<?>> immunities() {
		return IMMUNITIES;
	}
}
