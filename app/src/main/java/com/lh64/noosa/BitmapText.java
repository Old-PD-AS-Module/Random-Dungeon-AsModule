/*
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

package com.lh64.noosa;

import java.nio.FloatBuffer;
import com.lh64.gltextures.SmartTexture;
import com.lh64.glwrap.Matrix;
import com.lh64.glwrap.Quad;
import android.graphics.RectF;
import java.util.HashMap;
import android.graphics.Typeface;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class BitmapText extends Visual {

	protected String text;
	protected Font font;

	protected float[] vertices = new float[16];
	protected FloatBuffer quads;
	
	public int realLength;
	
	protected boolean dirty = true;
	
	public BitmapText() {
		this( "", null );
	}
	
	public BitmapText( Font font ) {
		this( "", font );
	}
	
	public BitmapText( String text, Font font ) {
		super( 0, 0, 0, 0 );
		
		this.text = text;
		this.font = font;
	}

	@Override
	public void destroy() {
		text = null;
		font = null;
		vertices = null;
		quads = null;
		super.destroy();
	}

	@Override
	protected void updateMatrix() {
		Matrix.setIdentity( matrix );
		Matrix.translate( matrix, x, y );
		Matrix.scale( matrix, scale.x, scale.y );
		Matrix.rotate( matrix, angle );
	}

	@Override
	public void draw() {
		super.draw();
		NoosaScript script = NoosaScript.get();
		if (font == null || font.texture == null) { return; }
		font.texture.bind();
		if (dirty) {
			updateVertices();
		}
		script.camera( camera() );
		script.uModel.valueM4( matrix );
		script.lighting( rm, gm, bm, am, ra, ga, ba, aa );
		script.drawQuadSet( quads, realLength );
	}

	protected void updateVertices() {
		width = 0;
		height = 0;
		if (text == null) {
			text = "";
		}
		quads = Quad.createSet( text.length() );
		realLength = 0;
		int length = text.length();
		for (int i=0; i < length; i++) {
			RectF rect = font.get( text.charAt( i ) );
			if (rect == null) {
				continue;
			}
			float w = font.width( rect );
			float h = font.height( rect );
			vertices[0] = width;
			vertices[1] = 0;
			vertices[2] = rect.left / font.texture.width();
			vertices[3] = rect.top / font.texture.height();
			vertices[4] = width + w;
			vertices[5] = 0;
			vertices[6] = rect.right / font.texture.width();
			vertices[7] = rect.top / font.texture.height();
			vertices[8] = width + w;
			vertices[9] = h;
			vertices[10] = rect.right / font.texture.width();
			vertices[11] = rect.bottom / font.texture.height();
			vertices[12] = width;
			vertices[13] = h;
			vertices[14] = rect.left / font.texture.width();
			vertices[15] = rect.bottom / font.texture.height();
			quads.put( vertices );
			realLength++;
			width += w + font.tracking;
			if (h > height) {
				height = h;
			}
		}
		if (length > 0) {
			width -= font.tracking;
		}
		dirty = false;
	}

	public void measure() {
		width = 0;
		height = 0;
		if (text == null) {
			text = "";
		}
		int length = text.length();
		for (int i=0; i < length; i++) {
			RectF rect = font.get( text.charAt( i ) );
			float w = font.width( rect );
			float h = font.height( rect );
			width += w + font.tracking;
			if (h > height) {
				height = h;
			}
		}
		if (length > 0) {
			width -= font.tracking;
		}
	}

	public float baseLine() {
		return font.baseLine * scale.y;
	}

	public Font font() {
		return font;
	}

	public void font( Font value ) {
		font = value;
	}

	public String text() {
		return text;
	}

	public void text( String str ) {
		text = LocalizationManager.getInstance().getString(str);
		dirty = true;
	}

	public static class Font extends TextureFilm {

		public static final String LATIN_FULL =
			" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";

		public float tracking = 0;
		public float baseLine;
		public boolean autoUppercase = false;
		public float lineHeight;

		private Typeface typeface;
		private Paint paint;
		private Bitmap.Config config = Bitmap.Config.ARGB_8888;
		private int textureSize = 512;
		private int nextX = 0;
		private int nextY = 0;
		private int lineHeightTex;

		private Canvas canvas;
		private Bitmap bitmap;

		public Font(SmartTexture tx, HashMap<Character, RectF> charMap, float lineHeight, float baseLine, float tracking) {
			super(tx);
			if (charMap != null) {
			    this.frames.putAll(charMap);
			}
            this.lineHeight = lineHeight;
            this.baseLine = baseLine;
            this.tracking = tracking;
		}

		public Font(Typeface typeface, int size, int color) {
			super((SmartTexture)null);
			this.typeface = typeface;
			this.paint = new Paint();
			this.paint.setTypeface(typeface);
			this.paint.setTextSize(size);
			this.paint.setColor(color);
			this.paint.setAntiAlias(true);
			this.lineHeight = size;
			this.baseLine = size;

			this.bitmap = Bitmap.createBitmap(textureSize, textureSize, config);
			this.canvas = new Canvas(this.bitmap);
			this.texture = new SmartTexture(this.bitmap);
		}

		@Override
		public RectF get( char ch ) {
			RectF rect = super.get( autoUppercase ? Character.toUpperCase( ch ) : ch );
			if (rect == null && typeface != null) {
				rect = renderGlyph(ch);
			}
			return rect;
		}

		private RectF renderGlyph(char ch) {
			Paint.FontMetrics fm = paint.getFontMetrics();
			float charWidth = paint.measureText(String.valueOf(ch));
			float charHeight = fm.bottom - fm.top;

			if (nextX + charWidth > textureSize) {
				nextX = 0;
				nextY += lineHeightTex;
				lineHeightTex = 0;
			}
			if (nextY + charHeight > textureSize) {
				return null;
			}
			if (charHeight > lineHeightTex) {
				lineHeightTex = (int)Math.ceil(charHeight);
			}

			Bitmap glyphBitmap = Bitmap.createBitmap((int)Math.ceil(charWidth), (int)Math.ceil(charHeight), config);
			Canvas glyphCanvas = new Canvas(glyphBitmap);
			glyphCanvas.drawText(String.valueOf(ch), 0, -fm.top, paint);

			texture.bind();
			GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, nextX, nextY, glyphBitmap);

			RectF rect = new RectF(nextX, nextY, nextX + charWidth, nextY + charHeight);
			frames.put(ch, rect);

			nextX += Math.ceil(charWidth);
			glyphBitmap.recycle();

			return rect;
		}

		public static Font colorMarked( Bitmap bmp, int height, int color, String chars ) {
			SmartTexture tx = new SmartTexture( bmp );
			HashMap<Character, RectF> map = new HashMap<Character, RectF>();
			int[] pixels = new int[bmp.getWidth()];
			
			int pos = 0;
			for (int i=0; i < chars.length(); i++) {
				bmp.getPixels( pixels, 0, bmp.getWidth(), pos, height, 1, 1 );
				int j = 0;
				while (j < pixels.length && pixels[j] != color) {
					j++;
				}
				map.put( chars.charAt(i), new RectF( pos, 0, j, height ) );
				pos = j + 1;
			}
			
			return new Font( tx, map, height, height, 1 );
		}
	}
}
