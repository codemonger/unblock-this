package bwr.blockcomposer.ui;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import bwr.blockcomposer.GameResources;
import bwr.blockcomposer.R;

import android.content.Context;

public class UIElementPicture implements UIElement {
	public final int id;
	public final int x, y, width, height;
	public boolean hidden = false;
	
	public final IntBuffer mVertexBuffer;
	public final ByteBuffer mIndexBuffer;
	private IntBuffer textureBuffer;
		
	public static final int BUTTON_UP = 0;
	public static final int BUTTON_DOWN = 1;
	public static final int BUTTON_LEFT = 2;
	public static final int BUTTON_RIGHT = 3;
	
	final int textureVertices[][] = {
		{ 	 0,     0,  // UP
			 0, 1<<16,
		 1<<16, 1<<16,
		 1<<16,     0, },
		{1<<16, 1<<16,	// DOWN
		 1<<16,     0,
		     0,     0,
		     0, 1<<16, },
        { 	 0,     0,  // LEFT
		 1<<16,     0,
	     1<<16, 1<<16,
		     0, 1<<16, },
		{1<<16, 1<<16,  // RIGHT
		     0, 1<<16,
			 0,     0,
		 1<<16,     0 },
	};
	
	public UIElementPicture(int id, int x, int y, int width, int height, int direction) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
    
		final int vertices[] = {
				x<<16, y<<16, 0,
				x<<16, (y-height)<<16, 0,
				(x+width)<<16, (y-height)<<16, 0,
				(x+width)<<16, y<<16, 0
		};
		
	    final byte indices[] = {
	        0, 1, 2,
	        0, 2, 3
	    };
		
		ByteBuffer tmp = ByteBuffer.allocateDirect(vertices.length*4);
		tmp.order(ByteOrder.nativeOrder());
		mVertexBuffer = tmp.asIntBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);
		
		tmp = ByteBuffer.allocateDirect(textureVertices[direction].length * 4);
		tmp.order(ByteOrder.nativeOrder());
		textureBuffer = tmp.asIntBuffer();
		textureBuffer.put(textureVertices[direction]);
		textureBuffer.position(0);
		
        mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
        mIndexBuffer.put(indices);
        mIndexBuffer.position(0);
	}

	
	public boolean isHit(int x, int y) {
		return x >= this.x && x <= (this.x+width) && y<=this.y && y >= (this.y-height);
	}
	
	public void draw(GL10 gl, GameResources gameResources) {
		gameResources.bindTexture(gl, R.drawable.up);

        gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FIXED, 0, textureBuffer);
        gl.glDrawElements(GL10.GL_TRIANGLES, 6, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
	}


	public void update(Context context, GL10 gl, long dt) {
		// nothing
	}


	public int getId() {
		return id;
	}
	
}
