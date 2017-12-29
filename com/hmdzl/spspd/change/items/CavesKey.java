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
package com.hmdzl.spspd.change.items;

import java.util.ArrayList;

import com.hmdzl.spspd.change.Dungeon;
import com.hmdzl.spspd.change.Statistics;
import com.hmdzl.spspd.change.actors.Actor;
import com.hmdzl.spspd.change.actors.buffs.Buff;
import com.hmdzl.spspd.change.actors.hero.Hero;
import com.hmdzl.spspd.change.actors.mobs.Mob;
import com.hmdzl.spspd.change.actors.mobs.pets.PET;
import com.hmdzl.spspd.change.items.artifacts.TimekeepersHourglass;
import com.hmdzl.spspd.change.items.bombs.FishingBomb;
import com.hmdzl.spspd.change.items.food.Blackberry;
import com.hmdzl.spspd.change.items.food.Blueberry;
import com.hmdzl.spspd.change.items.food.Cloudberry;
import com.hmdzl.spspd.change.items.food.FullMoonberry;
import com.hmdzl.spspd.change.items.food.GoldenNut;
import com.hmdzl.spspd.change.items.food.Moonberry;
import com.hmdzl.spspd.change.items.scrolls.ScrollOfSacrifice;
import com.hmdzl.spspd.change.levels.Level;
import com.hmdzl.spspd.change.messages.Messages;
import com.hmdzl.spspd.change.scenes.InterlevelScene;
import com.hmdzl.spspd.change.sprites.ItemSprite.Glowing;
import com.hmdzl.spspd.change.sprites.ItemSpriteSheet;
import com.hmdzl.spspd.change.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;

public class CavesKey extends Item {
	
	public static final float TIME_TO_USE = 1;

	public static final String AC_PORT = "PORT";

	private int specialLevel = 29;
	private int returnDepth = -1;
	private int returnPos;

	{
		//name = "ancient caves key";
		image = ItemSpriteSheet.CAVEKEY;

		stackable = false;
		unique = true;
	}
	
	private static final String DEPTH = "depth";
	private static final String POS = "pos";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(DEPTH, returnDepth);
		if (returnDepth != -1) {
			bundle.put(POS, returnPos);
		}
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		returnDepth = bundle.getInt(DEPTH);
		returnPos = bundle.getInt(POS);
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_PORT);
		
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {

		if (action == AC_PORT) {

			if (Dungeon.bossLevel() || hero.petfollow) {
				hero.spend(TIME_TO_USE);
				GLog.w(Messages.get(Item.class, "not_here"));
				return;
			}
			
			if (Dungeon.depth>25 && Dungeon.depth!=specialLevel) {
				hero.spend(TIME_TO_USE);
				GLog.w(Messages.get(Item.class, "not_here"));
				return;
			}
			if (Dungeon.depth==1) {
				hero.spend(TIME_TO_USE);
				GLog.w(Messages.get(Item.class, "not_here"));
				return;
			}


		}

		if (action == AC_PORT) {
			
			    hero.spend(TIME_TO_USE);

				Buff buff = Dungeon.hero
						.buff(TimekeepersHourglass.timeFreeze.class);
				if (buff != null)
					buff.detach();

              if (Dungeon.depth<25 && !Dungeon.bossLevel()){
            	
            	returnDepth = Dungeon.depth;
       			returnPos = hero.pos;
				InterlevelScene.mode = InterlevelScene.Mode.PORTCAVES;
			} else {
				FishingBomb bomb = Dungeon.hero.belongings.getItem(FishingBomb.class);
				updateQuickslot();
				 
				hero.invisible=0;
								
				this.doDrop(hero);
				 checkPetPort();
				 removePet();
				
				if (Statistics.albinoPiranhasKilled>99){
					Moonberry berry1 = new Moonberry(10);
					berry1.doPickUp(Dungeon.hero);	
					Cloudberry berry2 = new Cloudberry(10);
					berry2.doPickUp(Dungeon.hero);	
					Blueberry berry3 = new Blueberry(10);
					berry3.doPickUp(Dungeon.hero);	
					Blackberry berry4 = new Blackberry(10);
					berry4.doPickUp(Dungeon.hero);
					ScrollOfSacrifice sacrificeprice = new ScrollOfSacrifice();
					sacrificeprice.doPickUp(Dungeon.hero);
					sacrificeprice.identify();
					if (Dungeon.checkNight()){
						FullMoonberry berry = new FullMoonberry();
						berry.doPickUp(Dungeon.hero);
					}
				}

				  if (Statistics.goldThievesKilled>99 && Statistics.skeletonsKilled>99
						  && Statistics.albinoPiranhasKilled>99 && Statistics.archersKilled>99){
					  GoldenNut nut = new GoldenNut();
					  nut.doPickUp(Dungeon.hero);
				  }
								
				InterlevelScene.mode = InterlevelScene.Mode.RETURN;	
			}
                hero.invisible=0;
				InterlevelScene.returnDepth = returnDepth;
				InterlevelScene.returnPos = returnPos;
				Game.switchScene(InterlevelScene.class);
					
		} else {

			super.execute(hero, action);

		}
	}
	
	

	private PET checkpet(){
		for (Mob mob : Dungeon.level.mobs) {
			if(mob instanceof PET) {
				return (PET) mob;
			}
		}	
		return null;
	}
	
	private boolean checkpetNear(){
		for (int n : Level.NEIGHBOURS8) {
			int c =  Dungeon.hero.pos + n;
			if (Actor.findChar(c) instanceof PET) {
				return true;
			}
		}
		return false;
	}
	
	private void checkPetPort(){
		PET pet = checkpet();
		if(pet!=null && checkpetNear()){
		  //GLog.i("I see pet");
		  Dungeon.hero.petType=pet.type;
		  Dungeon.hero.petLevel=pet.level;
		  Dungeon.hero.petKills=pet.kills;	
		  Dungeon.hero.petHP=pet.HP;
		  Dungeon.hero.petExperience=pet.experience;
		  Dungeon.hero.petCooldown=pet.cooldown;
		  pet.destroy();
		  Dungeon.hero.petfollow=true;
		} else if (Dungeon.hero.haspet && Dungeon.hero.petfollow) {
			Dungeon.hero.petfollow=true;
		} else {
			Dungeon.hero.petfollow=false;
		}
		
	}
	private void removePet(){
		if (Dungeon.hero.haspet && !Dungeon.hero.petfollow){
		 for (Mob mob : Dungeon.level.mobs) {
				if(mob instanceof PET) {				 
					Dungeon.hero.haspet=false;
					Dungeon.hero.petCount++;
					mob.destroy();				
				}
			  }
		}
	}
	
	
	public void reset() {
		returnDepth = -1;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}


	private static final Glowing BLACK = new Glowing(0x00000);

	@Override
	public Glowing glowing() {
		return BLACK;
	}
}
