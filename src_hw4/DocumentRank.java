
public class DocumentRank {
	String documentId;
	double rank;
	
	public DocumentRank(String documentId, double rank) {
		this.documentId = documentId;
		this.rank = rank;
	}

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public double getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}
}
