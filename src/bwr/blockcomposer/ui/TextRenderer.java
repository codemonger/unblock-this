package bwr.blockcomposer.ui;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.LinkedList;

import javax.microedition.khronos.opengles.GL10;

import bwr.blockcomposer.misc.GLUtility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class TextRenderer {
	private static final int TEXT_PADDING_Y = 5;
	private int[] textTexture;
	
	public final int x, y, width, height;
	private int textureDimension;
	private Paint textPaint;
	
	private IntBuffer mVertexBuffer;

	private int textSize;
	private boolean blankText;
	private String text = "";
	private static final ByteBuffer mIndexBuffer;
	private static final IntBuffer textureBuffer;
	
	private static final int textureVertices[] = {
		 	 0,     0,
			 0, 1<<16,
		 1<<16, 1<<16,
		 1<<16,     0,
	};
	
    private static final byte indices[] = {
	        0, 1, 2,
	        0, 2, 3
	};
    
	static {
		textureBuffer = ByteBuffer.allocateDirect(textureVertices.length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
		textureBuffer.put(textureVertices);
		textureBuffer.position(0);
		
        mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
        mIndexBuffer.put(indices);
        mIndexBuffer.position(0);
	}
	
	public TextRenderer(Context context, GL10 gl, String text, int x, int y, int width, int height, int textSize) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.textSize = textSize;
		
		textPaint = createTextPaint(this.textSize);
		
		setText(context, gl, text);
	}
	
	public void draw(GL10 gl) {
		if(blankText) return;
		
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textTexture[0]);
        gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FIXED, 0, textureBuffer);
        gl.glDrawElements(GL10.GL_TRIANGLES, 6, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
	}
	
	public void setText(Context context, GL10 gl, String text) {
		if(!blankText && textTexture != null) gl.glDeleteTextures(1, textTexture, 0);
		
		this.text = text;
		
		if("".equals(text)) {
			blankText = true;
		} else {
			blankText = false;
			loadTextTexture(context, gl, text);
		}
	}
	
	public void refreshTexture(Context context, GL10 gl) {
		if(!blankText && textTexture != null) gl.glDeleteTextures(1, textTexture, 0);
		
		if("".equals(text)) {
			blankText = true;
		} else {
			blankText = false;
			loadTextTexture(context, gl, text);
		}
	}
	
	private static LinkedList<String> wordWrapText(String text, Paint paint, int desiredWidth) {
		LinkedList<String> lines = new LinkedList<String>();
		
		String remainingText = text;
		while(remainingText.length() > 0) {
			int c = paint.breakText(remainingText, true, desiredWidth, null);

			int c2;
			if(c != remainingText.length()) {
				c2 = remainingText.lastIndexOf(' ', c);
			} else c2 = c;

			if(c2 == -1) c2 = c;
			
			lines.add(remainingText.substring(0, c2));
			if(c2>=remainingText.length()) {
				remainingText = "";
			} else {
				remainingText = remainingText.substring(c2+1, remainingText.length());
			}
		}
		return lines;
	}
	
	private static Paint createTextPaint(int textSize) {
		Paint textPaint = new Paint();
		textPaint.setTextSize(textSize);
		textPaint.setAntiAlias(true);
		textPaint.setColor(Color.WHITE);
		return textPaint;
	}
	
	public static int calculateRequiredHeight(String text, int desiredWidth, int textSize) {
		Paint p = createTextPaint(textSize);
		
		LinkedList<String> lines = wordWrapText(text, p, desiredWidth);
		int lineSize = getLineHeight(lines.get(0), p);
		
		return (lineSize+TEXT_PADDING_Y)*(lines.size()+1);
	}
	
	public static Rect calculateSize(LinkedList<String> text, Paint paint, int yPadding) {
		
		int textWidth = 0;
		int textHeight = 0;
		
		Rect tmp = new Rect();
		for(String l : text) {
			paint.getTextBounds(l, 0, l.length(), tmp);
			textWidth = Math.max(textWidth, tmp.width());
			textHeight += tmp.height() + yPadding;
		}
		
		Rect result = new Rect(0, 0, textWidth, textHeight);
		
		return result;
	}
	
	public static int getLineHeight(String text, Paint paint) {
		Rect rect = new Rect();
		
		paint.getTextBounds(text, 0, text.length(), rect);
		
		return rect.height();
	}
	
	public static int determineTextureSize(int width, int height) {
		int maxDim = Math.max(width, height);
		int n = 5;
		while(Math.pow(2, n) < maxDim) n++;
		return (int) Math.pow(2, n);
	}
	
	public void loadTextTexture(Context context, GL10 gl, String text) {
		
		final LinkedList<String> lines = wordWrapText(text, textPaint, width);
		
		final Rect textSize = calculateSize(lines, textPaint, TEXT_PADDING_Y);
		
		final int lineHeight = getLineHeight(lines.get(0), textPaint);
		
		textureDimension = determineTextureSize(textSize.width(), textSize.height());
		
		final Bitmap bitmap = Bitmap.createBitmap(textureDimension, textureDimension, Bitmap.Config.ARGB_4444);
		final Canvas canvas = new Canvas(bitmap);
		
		for(int i = 0 ; i < lines.size(); i++) {
			canvas.drawText(lines.get(i), 0, lineHeight + (lineHeight+TEXT_PADDING_Y)*i, textPaint);
		}
		
		textTexture = new int[1];
		
		//Generate one texture pointer...
		gl.glGenTextures(1, textTexture, 0);
		
		GLUtility.loadBitmapIntoTexture(gl, textTexture[0], bitmap);
		
		//Clean up
		bitmap.recycle();
		
		// Want to offset to center text
		final int offsetx = (width - textSize.width())/2;
		final int offsety = (height - textSize.height())/2;
		
		final int vertices[] = {
				(x+offsetx)<<16, (y-offsety)<<16, 0,
				(x+offsetx)<<16, (y-offsety-textureDimension)<<16, 0,
				(x+offsetx+textureDimension)<<16, (y-offsety-textureDimension)<<16, 0,
				(x+offsetx+textureDimension)<<16, (y-offsety)<<16, 0
		};

		mVertexBuffer = ByteBuffer.allocateDirect(vertices.length*4).order(ByteOrder.nativeOrder()).asIntBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);
	}
	
}
