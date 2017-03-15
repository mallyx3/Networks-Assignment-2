package ass2;

import java.nio.ByteBuffer;

public class Packet {
	private String ID;
	private int length; //number of entries
	private String nodes;
	private long millis;
	
	
	/**
	 * Return's an stpPacket's byte array
	 * @return
	 */
	public byte[] getBytes () {
		byte[] bytes = new byte[13+this.nodes.length()];
		bytes[0] = (byte) (this.length >> 24);
		bytes[1] = (byte) (this.length >> 16);
		bytes[2] = (byte) (this.length >> 8);
		bytes[3] = (byte) (this.length);
		byte[] newLong = longToBytes(this.millis);
		System.arraycopy(newLong, 0, bytes, 4, 8);
		bytes[12] = (byte) (this.ID.getBytes()[0]);
		byte[] node = this.nodes.getBytes();
		System.arraycopy(node, 0, bytes, 13, this.nodes.length());
		return bytes;
	}
	
	public byte[] longToBytes(long x) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.putLong(x);
	    return buffer.array();
	}
	
	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public Packet(int size, String d, String id, long millis) {
		this.length = size;
		this.nodes = d;
		this.ID = id;
		this.millis = millis;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getNodes() {
		return nodes;
	}

	public void setNodes(String data) {
		this.nodes = data;
	}

	public void setMillis(long millis) {
		this.millis = millis;
	}

	public long getMillis() { 
		return millis;
	}
}
