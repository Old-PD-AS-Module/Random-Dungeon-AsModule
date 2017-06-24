/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
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
package com.lh64.randomdungeon.windows;

import com.lh64.noosa.BitmapText;
import com.lh64.noosa.ui.Component;
import com.lh64.randomdungeon.actors.mobs.Mob;
import com.lh64.randomdungeon.scenes.PixelScene;
import com.lh64.randomdungeon.sprites.CharSprite;
import com.lh64.randomdungeon.ui.BuffIndicator;
import com.lh64.randomdungeon.ui.HealthBar;
import com.lh64.randomdungeon.utils.Utils;

public class WndInfoMob extends WndTitledMessage {

	public WndInfoMob( Mob mob, boolean bestiary ) {
		
		super( new MobTitle( mob ), desc( mob, bestiary ) );
		
	}
	
	private static String desc( Mob mob, boolean bestiary ) {
		StringBuilder builder;
		if(mob.altDescription().equals(".") || bestiary == false){
		builder = new StringBuilder( mob.description() );
		} else {
			builder = new StringBuilder( mob.altDescription());
		}
		
		
		if(mob.state != mob.NOTHING && bestiary == false){
		builder.append( "\n\n" + mob.state.status() + "." +"\n Health: " + mob.HP + "/" + mob.HT);
		}
		
		if(mob.discovered == true || Mob.Mobs.contains(mob) == false){
		return builder.toString();
		} else{
			return "You haven't gotten information on this mob yet.";
		}
	}
	
	private static class MobTitle extends Component {
		
		private static final int GAP	= 2;
		
		private CharSprite image;
		private BitmapText name;
		private HealthBar health;
		private BuffIndicator buffs;
		
		public MobTitle( Mob mob ) {
			String temptext = mob.name;
			if(mob.discovered != true && Mob.Mobs.contains(mob)){
				temptext="???";
			}
			name = PixelScene.createText( Utils.capitalize( temptext ), 9 );
			name.hardlight( TITLE_COLOR );
			name.measure();	
			add( name );
			
			image = mob.sprite();
			if(mob.discovered != true && Mob.Mobs.contains(mob) == true){
				image.hardlight(0x000000);
			}
			add( image );
			
			health = new HealthBar();
			health.level( (float)mob.HP / mob.HT );
			add( health );
			
			buffs = new BuffIndicator( mob );
			add( buffs );
		}
		
		@Override
		protected void layout() {
			
			image.x = 0;
			image.y = Math.max( 0, name.height() + GAP + health.height() - image.height );
			
			name.x = image.width + GAP;
			name.y = image.height - health.height() - GAP - name.baseLine();
			
			float w = width - image.width - GAP;
			
			health.setRect( image.width + GAP, image.height - health.height(), w, health.height() ); 
			
			buffs.setPos( 
				name.x + name.width() + GAP, 
				name.y + name.baseLine() - BuffIndicator.SIZE );

			height = health.bottom();
		}
	}
}
