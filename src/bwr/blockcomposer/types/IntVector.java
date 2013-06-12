package bwr.blockcomposer.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class IntVector implements Comparable<IntVector> {
	public int x;
	public int y;
	public int z;
	
	public IntVector() {}
	
	public IntVector(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void copy(IntVector other) {
		x = other.x;
		y = other.y;
		z = other.z;
	}
	
	public void set(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void add(IntVector v) {
		x += v.x;
		y += v.y;
		z += v.z;
	}
	
	public void sub(IntVector v) {
		x -= v.x;
		y -= v.y;
		z -= v.z;
	}
	
	public IntVector duplicate() {
		return new IntVector(x,y,z);
	}
	
	public void writeTo(DataOutputStream out) throws IOException {
		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(z);
	}

	public void readFrom(DataInputStream in) throws IOException {
		x = in.readInt();
		y = in.readInt();
		z = in.readInt();
	}

	public int compareTo(IntVector other) {
		if(x != other.x) {
			return x - other.x;
		} else if (y != other.y) {
			return y - other.y;			
		} else if (z != other.z) {
			return z - other.z;	
		}
		return 0;
	}

}