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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.Timer;
public class Test {
		private Vertex thisVertex;
		private String node_id;
		private String w;
		
		private Hashtable<String, Integer> neighbours; 
		private Map<String, Vertex[]> graph;
		
		public Test(String ID) throws SocketException {
			this.neighbours = new Hashtable(10);
			this.node_id = ID;

		    this.thisVertex = new Vertex(ID, 0);
		    this.graph = new HashMap<String, Vertex[]>();
		    

		}
		
		public static void main(String[] args) throws Exception {

			
			Test t = new Test("A");
			
			//put a
			Vertex b = new Vertex("B", 2);
			Vertex d = new Vertex("D", 1);
			Vertex c = new Vertex("C", 5);
			Vertex[] z = {b, d, c};
			t.graph.put("A", z);
		
			//put b
			c = new Vertex("C", 3);
			b = new Vertex("B", 0);
			d = new Vertex("D", 2);
			Vertex a = new Vertex("A", 2);
			Vertex[] z1 = {c, d, a};
			t.graph.put("B", z1);
			
			//put c
			Vertex e = new Vertex("E", 1);
			d = new Vertex("D", 3);
			b = new Vertex("B", 3);
			a = new Vertex("A", 5);
			c = new Vertex("C", 0);
			Vertex f = new Vertex("F", 5);
			Vertex[] z2 = {f, e, d, b, a};
			t.graph.put("C", z2);
			
			//put d
			b = new Vertex("B", 2);
			a = new Vertex("A", 1);
			e = new Vertex("E", 1);
			c = new Vertex("C", 3);
			Vertex[] z3 = {b, a, e, c};
			t.graph.put("D", z3);
			
			//put e
			c = new Vertex("C", 1);
			d = new Vertex("D", 1);
			f = new Vertex("F", 2);
			Vertex[] z4 = {c, d, f};
			e = new Vertex("E", 0);
			t.graph.put("E",  z4);
			
			//put f
			c = new Vertex("C", 5);
			e = new Vertex("E", 2);
			f = new Vertex("F", 0);
			Vertex[] z5 = {c, e};
			t.graph.put("F", z5);
			
			t.calculatePath();
			
	
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
				for(Vertex u : graph.get(N.get(i))) {
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
			
		}
		
		public Hashtable<String, Integer> getNeighbours() {
			return neighbours;
		}

		
		public Packet getPacket (byte[] bytes) {
			int length = ((bytes[0] << 24) & 0xFF000000) + ((bytes[1] << 16) & 0x00FF00000) + ((bytes[2] << 8) & 0x0000FF00) + ((bytes[3]) & 0x000000FF);
			boolean failed = bytes[4] == 1 ? true : false;
			String id = new String(bytes, 5, 1);
			String str = new String(bytes, 6, bytes.length-6);
			Packet packet = new Packet(length, str, id, failed);
			return packet;
		}
		


	
	
}
