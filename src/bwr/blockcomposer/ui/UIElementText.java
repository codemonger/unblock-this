package bwr.blockcomposer.ui;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import bwr.blockcomposer.GameResources;
import bwr.blockcomposer.R;

import android.content.Context;

public class UIElementText implements UIElement {
	private static final int UIBORDER = 8;
	private final int id;
	private final int x, y, width, height;
	private final TextRenderer textRenderer;
	
	private final IntBuffer vertexBuffer;
	private static final ByteBuffer indexBuffer;
	private static final IntBuffer textureBuffer;
	
	private String text;
	private String updateText;
	private final GameResources gameResources;
	public boolean hidden = false;
	
	static private final int textureVertices[] = {
		65536, 0,
		57344, 0,
		8192, 0,
		0, 0,
		
		65536, 8192,
		57344, 8192,
		8192, 8192,
		0, 8192,
		
		65536, 57344,
		57344, 57344,
		8192, 57344,
		0, 57344,
		
		65536, 65536,
		57344, 65536,
		8192, 65536,
		0, 65536,
	};
		
    static private final byte indices[] = {
		0,4,5,
		0,5,1,
		1,5,6,
		1,6,2,
		2,6,7,
		2,7,3,
		4,8,9,
		4,9,5,
		5,9,10,
		5,10,6,
		6,10,11,
		6,11,7,
		8,12,13,
		8,13,9,
		9,13,14,
		9,14,10,
		10,14,15,
		10,15,11
    };
	
    static {
		textureBuffer = ByteBuffer.allocateDirect(textureVertices.length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
		textureBuffer.put(textureVertices);
		textureBuffer.position(0);
		
        indexBuffer = ByteBuffer.allocateDirect(indices.length);
        indexBuffer.put(indices);
        indexBuffer.position(0);
    }
	
	public UIElementText(GameResources gameResources, Context context, GL10 gl, int id, String text, int x, int y, int width, int height, int textSize) {
		this.gameResources = gameResources;
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	    this.text = text;
	    
		final int vertices[] = {
			        (x)<<16, (y)<<16, 0,
			      (x+UIBORDER)<<16, (y)<<16, 0,
			(x+width-UIBORDER)<<16, (y)<<16, 0,
			  (x+width)<<16, (y)<<16, 0,
			  
			        (x)<<16, (y-UIBORDER)<<16, 0,
			      (x+UIBORDER)<<16, (y-UIBORDER)<<16, 0,
			(x+width-UIBORDER)<<16, (y-UIBORDER)<<16, 0,
			  (x+width)<<16, (y-UIBORDER)<<16, 0,
			  
			        (x)<<16, (y-height+UIBORDER)<<16, 0,
			      (x+UIBORDER)<<16, (y-height+UIBORDER)<<16, 0,
			(x+width-UIBORDER)<<16, (y-height+UIBORDER)<<16, 0,
			  (x+width)<<16, (y-height+UIBORDER)<<16, 0,
			  
			        (x)<<16, (y-height)<<16, 0,
			      (x+UIBORDER)<<16, (y-height)<<16, 0,
			(x+width-UIBORDER)<<16, (y-height)<<16, 0,
			  (x+width)<<16, (y-height)<<16, 0,
		};
		
		vertexBuffer = ByteBuffer.allocateDirect(vertices.length*4).order(ByteOrder.nativeOrder()).asIntBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
		
		textRenderer = new TextRenderer(context, gl, text, x+UIBORDER, y-UIBORDER, width-(2*UIBORDER), height-(2*UIBORDER), textSize);
	}
	
	public void setText(String text) {
		if(!this.text.equals(text)) {
			this.updateText = text;
		}
	}
	
	public void update(Context context, GL10 gl, long dt) {
		if(updateText != null) {
			textRenderer.setText(context, gl, updateText);
			text = updateText;
			updateText = null;
		}
	}

	public int getId() {
		return id;
	}
	
	public boolean isHit(int x, int y) {
		return x >= this.x && x <= (this.x+width) && y<=this.y && y >= (this.y-height);
	}
	
	public void render(GL10 gl) {
		gameResources.bindTexture(gl, R.drawable.ui);

        gl.glVertexPointer(3, GL10.GL_FIXED, 0, vertexBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FIXED, 0, textureBuffer);
        gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_BYTE, indexBuffer);
        
        textRenderer.draw(gl);
	}
	
	public static int calculateRequiredHeight(String text, int desiredWidth, int textSize) {
		return TextRenderer.calculateRequiredHeight(text, desiredWidth, textSize) + 2*UIBORDER;
	}

	public void refresh(Context context, GL10 gl) {
		textRenderer.refreshTexture(context, gl);
	}
}
