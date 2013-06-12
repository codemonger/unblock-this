package bwr.blockcomposer.models;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

public class SelectBoxRenderer {
	private final static int FLOAT_SIZE = 4;
	private final static int CHAR_SIZE = 2;
		
	private int numVertices;
	
	private IntBuffer vertexBuffer;
	private CharBuffer indexBuffer;
	
	public void render(GL10 gl) {
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glDisable(GL10.GL_TEXTURE_2D);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FIXED, 0, vertexBuffer);
		
		gl.glLineWidthx(1<<16);
		gl.glColor4f(0.99609f,0.25390f,0.88281f, 1.0f);

		gl.glDrawElements(GL10.GL_LINES, numVertices, GL10.GL_UNSIGNED_SHORT, indexBuffer);
		
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_TEXTURE_2D);
	}
	
	private void putVertex(int x, int y, int z) {
		vertexBuffer.put(x);
		vertexBuffer.put(y);
		vertexBuffer.put(z);
	}
	
	public SelectBoxRenderer() {
		
		numVertices = 24;
						
		vertexBuffer = ByteBuffer.allocateDirect(numVertices*3*FLOAT_SIZE).order(ByteOrder.nativeOrder()).asIntBuffer();
		indexBuffer = ByteBuffer.allocateDirect(numVertices*CHAR_SIZE).order(ByteOrder.nativeOrder()).asCharBuffer();
		
        char index = 0; // vertex index
        
        putVertex(0,0,0);  indexBuffer.put(index++);
    	putVertex(1<<16,0,0); indexBuffer.put(index++);

    	putVertex(0,0,1<<16); indexBuffer.put(index++);
    	putVertex(1<<16,0,1<<16); indexBuffer.put(index++);

    	putVertex(0,1<<16,0); indexBuffer.put(index++);
    	putVertex(1<<16,1<<16,0); indexBuffer.put(index++);

    	putVertex(0,1<<16,1<<16); indexBuffer.put(index++);
    	putVertex(1<<16,1<<16,1<<16); indexBuffer.put(index++);

    	putVertex(0,0,0); indexBuffer.put(index++);
    	putVertex(0,0,1<<16); indexBuffer.put(index++);

    	putVertex(0,1<<16,0); indexBuffer.put(index++);
    	putVertex(0,1<<16,1<<16); indexBuffer.put(index++);

    	putVertex(1<<16,0,0); indexBuffer.put(index++);
    	putVertex(1<<16,0,1<<16); indexBuffer.put(index++);

    	putVertex(1<<16,1<<16,0); indexBuffer.put(index++);
    	putVertex(1<<16,1<<16,1<<16); indexBuffer.put(index++);

    	putVertex(0,0,0); indexBuffer.put(index++);
    	putVertex(0,1<<16,0); indexBuffer.put(index++);

    	putVertex(1<<16,0,0); indexBuffer.put(index++);
    	putVertex(1<<16,1<<16,0); indexBuffer.put(index++);

    	putVertex(0,0,1<<16); indexBuffer.put(index++);
    	putVertex(0,1<<16,1<<16); indexBuffer.put(index++);

    	putVertex(1<<16,0,1<<16); indexBuffer.put(index++);
    	putVertex(1<<16,1<<16,1<<16); indexBuffer.put(index++);
  		
		
		vertexBuffer.position(0);
		indexBuffer.position(0);
	}
}
