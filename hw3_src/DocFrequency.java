package hw3;

public class DocFrequency {
	private String docId;
	private int frequency;
	
	public DocFrequency(String docId, int frequency) {
		this.docId = docId;
		this.frequency = frequency;
	}
	
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public int getFrequency() {
		return frequency;
	}
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
}
