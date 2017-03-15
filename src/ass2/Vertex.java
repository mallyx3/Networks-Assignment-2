package ass2;

public class Vertex {
	private String ID;
	private float weight;
	
	public Vertex(String id, float weight) {
		this.ID = id;
		this.weight = weight;
	}
	
	@Override
	public boolean equals (Object v) {
		boolean retVal = false;
        if (v instanceof Vertex){
            Vertex ptr = (Vertex) v;
            retVal = ptr.ID == this.ID;
        }
        return retVal;
	}
	
	public String getID() {
		return ID;
	}
	public void setID(String iD) {
		ID = iD;
	}
	public float getWeight() {
		return weight;
	}
	public void setWeight(float weight) {
		this.weight = weight;
	}
	
}
