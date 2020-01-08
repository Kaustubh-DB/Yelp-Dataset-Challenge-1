package task1ir;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;


//Class to calculate top scoring nouns/adjectives/noun+adjectives present in user - reviews
 
public class TFIDF {

	int docfrq;
	public static String trainingIndex="./task1index/Training_set";
	public static IndexReader reader;
	public HashMap<String, Double> scoreMap = new HashMap<String, Double>();
	private ArrayList<TermScoreData> termDetails;
	
	

	public int getDocfrq() {
		return docfrq;
	}

	public void setDocFrq(int docfrq) {
		this.docfrq = docfrq;
	}

	
	 // Function to calculate TFIDF for noun word in the sub-corpus 
	 
	public Double calculateScore(String nounword) throws IOException, ParseException {

		termDetails = new ArrayList<TermScoreData>();
		calculateTermDetails(nounword);
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(trainingIndex)));
		IndexSearcher searcher = new IndexSearcher(reader);
		searcher.setSimilarity(new LMJelinekMercerSimilarity((float) 0.7));
		
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("reviews", analyzer);
		
		Double score = (double) 0;
		//System.out.println(nounword);
		
		// Check for operand words such as NOT OR AND and discard those
		if(nounword.equals("NOT")||nounword.equals("OR")||nounword.equals("AND"))
		{
			return score;
		}
		Query query = parser.parse(QueryParser.escape(nounword));
		TopDocs topDocs= searcher.search(query, Integer.MAX_VALUE);
		ScoreDoc[] sDocs = topDocs.scoreDocs;
		
		int N = sDocs.length;

		int docfreq = getDocfrq();

		
		Double IDF = Math.log10(1 + ((double) N / docfreq));
		for (TermScoreData term : termDetails) {
			int cti = term.getTermFreq();
			float lenOfDoc = term.getLenOfDoc();
			Double TF = (double) (cti / lenOfDoc);

			score += (TF * IDF);
		}

		return score;
	}

	// Function to calculate and save data needed for calculating TFIDF for noun
	 
	public void calculateTermDetails(String nounword) throws IOException {
		reader = DirectoryReader.open(FSDirectory.open(Paths.get(trainingIndex)));
		try {
			int docfreq = 0;
			// Get document length and term frequency
			ClassicSimilarity dSimi = new ClassicSimilarity();
			List<LeafReaderContext> leafContexts = reader.getContext().reader()
					.leaves();
			for (LeafReaderContext leafContext : leafContexts) {

				// Get frequency of the query term from its postings
				PostingsEnum de = MultiFields.getTermDocsEnum(leafContext
						.reader(), "reviews", new BytesRef(nounword));

				int doc;
				
				if (de != null) {
					while ((doc = de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
						

							int ct = de.freq();

							docfreq++;

							// normalized doc length is docLength
							float normDocLength = dSimi
									.decodeNormValue(leafContext.reader()
											.getNormValues("reviews")
											.get(de.docID()));
							float docLength = 1 / (normDocLength * normDocLength);

							termDetails
									.add(new TermScoreData(ct, doc, docLength));
						}
					}
				
			}
			setDocFrq(docfreq);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}



	 //Function to iterate over nouns, calculate score and get top query words for category 

	public ArrayList<String> getTopQueryWords(
			ArrayList<String> nouns) throws IOException, ParseException {

		for (String nounword : nouns) {
			if (!scoreMap.containsKey(nounword)) {
				if(!nounword.equals("NOT")||!nounword.equals("OR")||!nounword.equals("AND")) {
				Double score = calculateScore(nounword);
				scoreMap.put(nounword, score);
				}
			}
		}
		ArrayList<String> topQueryWords = getTopWords();
		return topQueryWords;
	}

	
	 // Function to get top n words from hashmap for category
	
	public ArrayList<String> getTopWords() {
		ArrayList<String> top100 = new ArrayList<String>();
		HashMap<String, Double> sortedMap = sortByComparator();
		// num is number of query terms. num can be varied to get query with different number of terms
		int num = 100;  
		for (Entry<String, Double> entry : sortedMap.entrySet()) {
			if (num-- > 0) {
				top100.add(entry.getKey());
			}
		}
		return top100;
	}

	
	 // Function to sort the hashmap by score values
	 // Reference : https://www.geeksforgeeks.org/sorting-a-hashmap-according-to-values/
	 
	private HashMap<String, Double> sortByComparator() {

		// Convert Map to List
		List<HashMap.Entry<String, Double>> list = new LinkedList<HashMap.Entry<String, Double>>(
				scoreMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<HashMap.Entry<String, Double>>() {
			public int compare(HashMap.Entry<String, Double> o1,
					HashMap.Entry<String, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// Convert sorted map back to a Map
		HashMap<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (Iterator<HashMap.Entry<String, Double>> it = list.iterator(); it
				.hasNext();) {
			HashMap.Entry<String, Double> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	
	// get top query words 
	public HashMap<String, Double> getTopQueryWordsWithScore(
			ArrayList<String> nouns) throws IOException, ParseException {
		HashMap<String, Double> scores = new HashMap<String, Double>();
		for (String nounword : nouns) {
			if (!scoreMap.containsKey(nounword)) {
				Double score = calculateScore(nounword);
				scoreMap.put(nounword, score);
			}
		}
		
		HashMap<String, Double> sortedMap = sortByComparator();
		int num = 300;
		for (Entry<String, Double> entry : sortedMap.entrySet()) {
			if (num-- > 0) {
				scores.put(entry.getKey(), entry.getValue() * 1000);
			}
		}
		return scores;
	}


}
