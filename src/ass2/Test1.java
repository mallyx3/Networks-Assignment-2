package ass2;

import java.nio.ByteBuffer;

public class Test1 {
	private String ID;
	private int length; //number of entries
	private String nodes;
	private long millis;
	
	public static void main(String[] args) throws Exception
	   {
			Test1 test = new Test1(2, "B 4 2000 C 5 2001", "A", System.currentTimeMillis());
			System.out.println("current time: " + test.getMillis());
			
			Test1 test1 = test.getTest(test.getBytes());
			System.out.println("data: " + test1.getNodes() + " id: " + test1.getID() + " time: " + test1.getMillis());
			
	   
	   }
	
	
	public Test1 getTest (byte[] bytes) {
		int length = ((bytes[0] << 24) & 0xFF000000) + ((bytes[1] << 16) & 0x00FF00000) + ((bytes[2] << 8) & 0x0000FF00) + ((bytes[3]) & 0x000000FF);
		byte[] newLong = new byte[8];
		System.arraycopy(bytes, 4, newLong, 0, 8);
		long millis = bytesToLong(newLong);

		String id = new String(bytes, 12, 1);
		String str = new String(bytes, 13, bytes.length-13);
		Test1 packet = new Test1(length, str, id, millis);
		return packet;
	}
	
	public long bytesToLong(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.put(bytes);
	    buffer.flip();//need flip 
	    return buffer.getLong();
	}
	
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

	public Test1(int size, String d, String id, long millis) {
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
