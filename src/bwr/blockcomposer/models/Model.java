package bwr.blockcomposer.models;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;

public class Model {
	private final static int FLOAT_SIZE = 4;
	private final static int CHAR_SIZE = 2;
	
	private int numVertices;
	private int numTriangles;
	
	private FloatBuffer vertexBuffer;
	private FloatBuffer normalBuffer;
	private FloatBuffer textureBuffer;
	private CharBuffer indexBuffer;
	
	private static class TextureCoords {
		public float u,v;
	}
	
	private static class Vertex {
		public float x,y,z;
	}
	
	private static class Triangle {
		public final Vertex[] vertices = new Vertex[3];
		public final Vertex[] normals = new Vertex[3];
		public final TextureCoords[] tcoords = new TextureCoords[3];
	}
	
	public void prerender(GL10 gl) {
		gl.glFrontFace(GL10.GL_CCW);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glNormalPointer(GL10.GL_FLOAT, 0, normalBuffer);
		
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
	}
	
	public void render(GL10 gl) {
		gl.glDrawElements(GL10.GL_TRIANGLES, numVertices, GL10.GL_UNSIGNED_SHORT, indexBuffer);
	}
	
	public void postrender(GL10 gl) {
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);	
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY); 
	}
	
	private static Triangle readTriangle(DataInputStream dataInput) throws IOException {
		Triangle triangle = new Triangle();
		
		for(int i = 0; i < 3; i++) {
			triangle.vertices[i] = new Vertex();
			triangle.vertices[i].x = dataInput.readFloat();
			triangle.vertices[i].y = dataInput.readFloat();
			triangle.vertices[i].z = dataInput.readFloat();
		}
		
		for(int i = 0; i < 3; i++) {
			triangle.normals[i] = new Vertex();
			triangle.normals[i].x = dataInput.readFloat();
			triangle.normals[i].y = dataInput.readFloat();
			triangle.normals[i].z = dataInput.readFloat();
		}
		
		for(int i = 0; i < 3; i++) {
			triangle.tcoords[i] = new TextureCoords();
			triangle.tcoords[i].u = dataInput.readFloat();
			triangle.tcoords[i].v = 1.0f - dataInput.readFloat();
		}
		
		return triangle;
	}
	
	public static Model loadFromResource(Context context, int resourceId) throws IOException {
		Model model = new Model();
		
		DataInputStream dataInput = new DataInputStream(new BufferedInputStream(context.getResources().openRawResource(resourceId)));
		
		model.numTriangles = dataInput.readInt();
		model.numVertices = model.numTriangles * 3;
						
		model.vertexBuffer = ByteBuffer.allocateDirect(model.numVertices*3*FLOAT_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
		model.indexBuffer = ByteBuffer.allocateDirect(model.numVertices*CHAR_SIZE).order(ByteOrder.nativeOrder()).asCharBuffer();
		model.normalBuffer = ByteBuffer.allocateDirect(model.numVertices*3*FLOAT_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
		model.textureBuffer = ByteBuffer.allocateDirect(model.numVertices*2*FLOAT_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
		
        char index = 0; // vertex index
        
		for(int t = 0; t < model.numTriangles; t++) {
			Triangle triangle = readTriangle(dataInput);
			
			for(int v = 0; v < 3; v++) {
				model.vertexBuffer.put(triangle.vertices[v].x);
				model.vertexBuffer.put(triangle.vertices[v].y);
				model.vertexBuffer.put(triangle.vertices[v].z);
				
				model.normalBuffer.put(triangle.normals[v].x);
				model.normalBuffer.put(triangle.normals[v].y);
				model.normalBuffer.put(triangle.normals[v].z);
				
				model.textureBuffer.put(triangle.tcoords[v].u);
				model.textureBuffer.put(triangle.tcoords[v].v);
				
				model.indexBuffer.put(index);
				index++;
			}
		}
		
		model.vertexBuffer.position(0);
		model.indexBuffer.position(0);
        model.normalBuffer.position(0);
		model.textureBuffer.position(0);

		return model;
	}
}
