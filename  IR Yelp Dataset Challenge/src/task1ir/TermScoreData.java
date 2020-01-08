package task1ir;

 // Class for storing values to calculate TFIDF of query term

public class TermScoreData {
	int termFreq;
	
	int docId;
	
	float docLen;
	
	
	public TermScoreData(int termFreq, int docId, float lenOfDoc) {
		super();
		this.termFreq = termFreq;
		this.docId = docId;
		this.docLen = lenOfDoc;
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
		return docLen;
	}

	public void setLenOfDoc(float lenOfDoc) {
		this.docLen = lenOfDoc;
	}
	
	
}
