package taskone;

/**
 * @author Renuka
 * Helper class for calculation of TFIDF
 */
public class TermDetails {
	int termFreq;
	
	int docId;
	
	float lenOfDoc;
	
	

	/**
	 * @param termFreq
	 * @param docId
	 * @param lenOfDoc
	 */
	public TermDetails(int termFreq, int docId, float lenOfDoc) {
		super();
		this.termFreq = termFreq;
		this.docId = docId;
		this.lenOfDoc = lenOfDoc;
	}

	public int getTermFreq() {
		return termFreq;
	}

	public void setTermFreq(int termFreq) {
		this.termFreq = termFreq;
	}

	public int getDocId() {
		return docId;
	}

	public void setDocId(int docId) {
		this.docId = docId;
	}

	public float getLenOfDoc() {
		return lenOfDoc;
	}

	public void setLenOfDoc(float lenOfDoc) {
		this.lenOfDoc = lenOfDoc;
	}
	
	
}
