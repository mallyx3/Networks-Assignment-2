package ass2;

import java.awt.event.ActionEvent;
import java.lang.StringBuilder;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.Timer;



public class Node {
	private DatagramSocket socket;
	private Vertex thisVertex;
	private int port;
	private String node_id;
	
	private String w;
	
	private Hashtable<String, Integer> neighbours; 
	private Hashtable<String, Integer> heartbeat;
	private Map<String, Vertex[]> graph;
	private Timer timer0;
	private Timer timer1;
	private Hashtable<String, Packet> received;
	
	//private int[][] graph;
	//private char[] IDmapping;
	private Packet payload;
	
	public Node(String port, String ID) throws SocketException {
		this.heartbeat = new Hashtable<String, Integer>();
		this.port = Integer.parseInt(port);
		this.neighbours = new Hashtable<String, Integer>(10);
		this.node_id = ID;
		this.socket = new DatagramSocket(this.port);
		this.received = new Hashtable<String, Packet>();
		
		ActionListener taskPerformer0 = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					sendDataPacket();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
		};
	    this.timer0 = new Timer(1000, taskPerformer0);
	    this.timer0.setRepeats(true);
		ActionListener taskPerformer1 = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				calculatePath();
	        }
		};
	    this.timer1 = new Timer(30000, taskPerformer1);
	    this.timer1.setRepeats(true);

	    this.thisVertex = new Vertex(ID, 0);
	    this.graph = new HashMap<String, Vertex[]>();
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.out.println("Required arguments: ID port config.txt");
			return;
		}
		
		Node n = new Node(args[1], args[0]);
		
		try (BufferedReader br = new BufferedReader(new FileReader(args[2]))) {
			String line;
			String data = "";
			line = br.readLine();
			int size = Integer.parseInt(line);
		    while ((line = br.readLine()) != null) {
		    	data = data + line + " ";
		    }
		    
		    n.payload = new Packet(size, data, args[0], System.currentTimeMillis());
		    
		    n.setNeighbours();
		    n.timer0.start();
		    n.timer1.start();
    
		    while (true) {
				DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
				n.socket.receive(request);
				Packet packetData = n.getPacket(request.getData());
			//	System.out.println("received packet id: " + packetData.getID() + " size: " + packetData.getLength() + " data: " +  packetData.getNodes());
				
				if (n.received.containsKey(packetData.getID())) {
					if (packetData.getMillis() > n.received.get(packetData.getID()).getMillis()) {
						n.transmitPacket(packetData);
					} else {
						continue; //repeat packet
					}
				} else {
					n.transmitNewPacket(packetData);	//new packet
				}
		    }
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
		return;
	}
	
	public void transmitNewPacket(Packet packetData) throws IOException {
		Vertex[] vertexes = new Vertex[packetData.getLength()];
		String[] nodes = packetData.getNodes().split(" ");
		
		for (int i = 0; i<packetData.getLength(); i++) {
			String ID = nodes[i*3];
			int weight = Integer.parseInt(nodes[(i*3) + 1]);
			Vertex v = new Vertex(ID, weight);
			vertexes[i] = v;
		}
		this.graph.put(packetData.getID(), vertexes);
		this.received.put(packetData.getID(), packetData);
		
		for (String k : this.neighbours.keySet()) {
			if (k.equals(packetData.getID())) {
				continue;
			} else {
				byte[] bytes = packetData.getBytes();
		    	DatagramPacket packet = new DatagramPacket(bytes, bytes.length, InetAddress.getByName("127.0.0.1"), this.neighbours.get(k));
		    	this.socket.send(packet);
			}
		}
	}
	
	public void transmitPacket(Packet packetData) throws IOException {
		if (packetData.getLength() < this.graph.get(packetData.getID()).length) {
			System.out.println("new graph layout detected");
			Vertex[] vertexes = new Vertex[packetData.getLength()];
			String[] nodes = packetData.getNodes().split(" ");

			ArrayList<String> neighbours = new ArrayList<String>();
			for (int i = 0; i<nodes.length/3; i++) {
				String ID = nodes[i*3];
				neighbours.add(ID);
				int weight = Integer.parseInt(nodes[(i*3) + 1]);
				Vertex v = new Vertex(ID, weight);				
				vertexes[i] = v;
			}
			
			Vertex[] oldNeighbours = graph.get(packetData.getID());
			for (int i = 0; i<oldNeighbours.length ; i++) {
				if(!neighbours.contains(oldNeighbours[i].getID())) {
					if (this.neighbours.containsKey(oldNeighbours[i].getID())) {
						removeNeighbour(oldNeighbours[i].getID());
					} else {
						this.graph.remove(oldNeighbours[i].getID());
					}
				}
			}
			
			this.graph.put(packetData.getID(), vertexes);
			this.received.put(packetData.getID(), packetData);
			this.heartbeat.put(packetData.getID(), 0);
			
			for (String k : this.neighbours.keySet()) {
				if (k.equals(packetData.getID())) {
					continue;
				} else {
					byte[] bytes = packetData.getBytes();
			    	DatagramPacket packet = new DatagramPacket(bytes, bytes.length, InetAddress.getByName("127.0.0.1"), this.neighbours.get(k));
			    	this.socket.send(packet);				
			   }	
			}
		} else {
			this.received.put(packetData.getID(), packetData);
			this.heartbeat.put(packetData.getID(), 0);
			for (String k : this.neighbours.keySet()) {
				if (k.equals(packetData.getID())) {
					continue;
				} else {
					byte[] bytes = packetData.getBytes();
			    	DatagramPacket packet = new DatagramPacket(bytes, bytes.length, InetAddress.getByName("127.0.0.1"), this.neighbours.get(k));
			    	this.socket.send(packet);				
			   }	
			}
		}
	}
	
	public void sendDataPacket() throws IOException {
		for(String s: neighbours.keySet()) {
			int old = heartbeat.get(s)+1;
			heartbeat.put(s, old);
		}
		for (String s : heartbeat.keySet()) {
			if (heartbeat.get(s) >= 3) {
				removeNeighbour(s);
			}
		}
		
		for(int j : getNeighbours().values()) {
			payload.setMillis(System.currentTimeMillis());
			byte[] bytes = payload.getBytes();
	    	DatagramPacket packet = new DatagramPacket(bytes, bytes.length, InetAddress.getByName("127.0.0.1"), j);
	    	socket.send(packet);
	    }
	}
	
	public void removeNeighbour(String s) {
		//update payload packet
		String[] nodes = payload.getNodes().split(" ");
		ArrayList<String> newArray = new ArrayList<String>();
		for (int i = 0; i < payload.getLength()*3; i = i+3){
			if (nodes[i].equals(s)) {
				continue;
			} else {
				newArray.add(nodes[i]);
				newArray.add(nodes[i+1]);
				newArray.add(nodes[i+2]);
			}
		}
		String newNodes = String.join(" ", newArray);
		payload.setNodes(newNodes);
		int length = payload.getLength();
		payload.setLength(length-1);
		
		heartbeat.put(s, 0);
		
		ArrayList<Vertex> newList = new ArrayList<Vertex>();
		for (Vertex v : this.graph.get(this.thisVertex.getID())) {
			if (v.getID().equals(s)) {
				continue;
			} 
			newList.add(v);
		}
		Vertex[] newVertexes = newList.toArray(new Vertex[newList.size()]);
		graph.put(this.thisVertex.getID(), newVertexes);
		graph.remove(s);
		neighbours.remove(s);
		
	}
	
	
	public void calculatePath() {
		//initialization
		ArrayList<String> N = new ArrayList<String>();
		Hashtable<String, Vertex> data = new Hashtable<String, Vertex>();
		N.add(this.thisVertex.getID());
		
		for (Vertex v : graph.get(this.thisVertex.getID())) {
			Vertex newV = new Vertex(this.thisVertex.getID(), v.getWeight());
			data.put(v.getID(), newV);
		}
		for (String v : graph.keySet()) {
			if (!data.containsKey(v)) {
				Vertex newV = new Vertex(v, -1);
				data.put(v, newV);
			}
		}
		
		for (int i = 0; i<this.graph.size()-1; i++) {
			float min = -1;
			
			//find w not in N such that D(w) is min
			for(Vertex u : this.graph.get(N.get(i))) {
				if (N.contains(u.getID())) {
					continue;
				} else {
					if (data.get(u.getID()).getWeight() < min || min == -1) {
						min = data.get(u.getID()).getWeight();
						this.w = u.getID();
					} else {
						continue;
					}
				}
			}
			
			N.add(this.w);
			for (Vertex x : this.graph.get(this.w)) {
				if (!N.contains(x)) {
					if ((data.get(this.w).getWeight() + x.getWeight()) < data.get(x.getID()).getWeight() || data.get(x.getID()).getWeight() == -1) {
						Vertex newV = new Vertex(this.w, data.get(this.w).getWeight() + x.getWeight());
						data.put(x.getID(), newV);
					}
				}
			}
		}	
			
		for (String id : data.keySet()) {
			if (id == this.thisVertex.getID()) {
				continue;
			}
			float cost = data.get(id).getWeight();
			String path = "";
			String currID = id;
			while (!currID.equals(this.thisVertex.getID())) {
				path += currID;
				currID = data.get(currID).getID();
			}
			path += this.thisVertex.getID();
			String newPath = new StringBuilder(path).reverse().toString(); 
			System.out.println("least-cost path to node " + id + ": " + newPath + " and the cost is " + cost);
		}
		data.clear();
		
	}

	
	public Hashtable<String, Integer> getNeighbours() {
		return neighbours;
	}

	public void setNeighbours() {
		Vertex[] vertexes = new Vertex[this.payload.getLength()];
		String[] data = this.payload.getNodes().split(" ");
		for (int i = 0; i < this.payload.getLength(); i = i+1){
			this.neighbours.put(data[i*3], Integer.parseInt(data[(i*3)+2]));
			this.heartbeat.put(data[i*3], 0);
			Vertex v = new Vertex(data[i*3], Float.valueOf(data[(i*3)+1]));
			vertexes[i] = v;
			
		}
		this.graph.put(this.thisVertex.getID(), vertexes);
	}
	
	public Packet getPacket (byte[] bytes) {
		int length = ((bytes[0] << 24) & 0xFF000000) + ((bytes[1] << 16) & 0x00FF00000) + ((bytes[2] << 8) & 0x0000FF00) + ((bytes[3]) & 0x000000FF);
		byte[] newLong = new byte[8];
		System.arraycopy(bytes, 4, newLong, 0, 8);
		long millis = bytesToLong(newLong);

		String id = new String(bytes, 12, 1);
		String str = new String(bytes, 13, bytes.length-13);
		Packet packet = new Packet(length, str, id, millis);
		return packet;
	}
	
	public long bytesToLong(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.put(bytes);
	    buffer.flip();//need flip 
	    return buffer.getLong();
	}
	
}
