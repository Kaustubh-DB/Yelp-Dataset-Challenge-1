package taskone;

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

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import taskone.TermDetails;

/**
 * @author Renuka
 * Class to calculate top scoring nouns for a category from nouns present in tips and review
 */
public class ScoreCalculator {

	int dfti;
	public static String trainingIndex="./index/Training_set";
	public static IndexReader reader;
	public HashMap<String, Double> scoreMap = new HashMap<String, Double>();
	private HashMap<Integer, Integer> categoryDocIds;
	private ArrayList<TermDetails> termDetails;
	/**
	 * initialize lucene reader
	 */
	public ScoreCalculator() {
		
	}

	

	public int getDfti() {
		return dfti;
	}

	public void setDfti(int dfti) {
		this.dfti = dfti;
	}

	/**
	 * Function to calculate TFIDF for noun word in the sub-corpus
	 * @param nounword - nouns for which TFIDF is being calculated
	 * @param scoredocs - docs with the noun query term
	 * @return - score for the noun word in the docs
	 * @throws IOException 
	 */
	public Double calculateScore(String nounword, ScoreDoc[] scoredocs) throws IOException {

		categoryDocIds = getCategoryDocIds(scoredocs);
		termDetails = new ArrayList<TermDetails>();
		calculateTermDetails(nounword);

		int N = scoredocs.length;

		int dfti = getDfti();

		Double score = (double) 0;
		Double IDF = Math.log10(1 + ((double) N / dfti));
		for (TermDetails term : termDetails) {
			int cti = term.getTermFreq();
			float lenOfDoc = term.getLenOfDoc();
			Double TF = (double) (cti / lenOfDoc);

			score += (TF * IDF);
		}

		return score;
	}

	/** Function to calculate and save data needed for calculating TFIDF for noun
	 * @param nounword
	 * @throws IOException 
	 */
	public void calculateTermDetails(String nounword) throws IOException {
		reader = DirectoryReader.open(FSDirectory.open(Paths.get(trainingIndex)));
		try {
			int dfti = 0;
			// Get document length and term frequency
			ClassicSimilarity dSimi = new ClassicSimilarity();
			List<LeafReaderContext> leafContexts = reader.getContext().reader()
					.leaves();
			for (LeafReaderContext leafContext : leafContexts) {

				// Get frequency of the query term from its postings
				PostingsEnum de = MultiFields.getTermDocsEnum(leafContext
						.reader(), "reviewstips", new BytesRef(nounword));

				int doc;
				
				if (de != null) {
					while ((doc = de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
						if (categoryDocIds.containsKey(doc)) {

							int ct = de.freq();

							dfti++;

							// normalized doc length is docLength
							float normDocLength = dSimi
									.decodeNormValue(leafContext.reader()
											.getNormValues("reviewstips")
											.get(de.docID()));
							float docLength = 1 / (normDocLength * normDocLength);

							termDetails
									.add(new TermDetails(ct, doc, docLength));
						}
					}
				}
			}
			setDfti(dfti);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * Function to get document ids from scoredocs
	 * @param scoredocs
	 * @return - hashmap with document ids
	 */
	private HashMap<Integer, Integer> getCategoryDocIds(ScoreDoc[] scoredocs) {
		// TODO Auto-generated method stub
		categoryDocIds = new HashMap<Integer, Integer>();
		for (ScoreDoc scoreDoc : scoredocs) {
			categoryDocIds.put(scoreDoc.doc, 0);
		}
		return categoryDocIds;
	}

	
	/**
	 * Function to iterate over nouns, calculate score and get top query words for category 
	 * @param nouns
	 * @param topdocs
	 * @param category
	 * @return - top query words for category
	 * @throws IOException 
	 */
	public ArrayList<String> getTopQueryWordsForCategory(
			ArrayList<String> nouns, TopDocs topdocs, String category) throws IOException {

		for (String nounword : nouns) {
			if (!scoreMap.containsKey(nounword)) {
				Double score = calculateScore(nounword, topdocs.scoreDocs);
				scoreMap.put(nounword, score);
			}
		}
		ArrayList<String> topQueryWords = getTopWords();
		return topQueryWords;
	}

	/**
	 * Function to get top n words from hashmap for category
	 * @return
	 */
	public ArrayList<String> getTopWords() {
		ArrayList<String> top100 = new ArrayList<String>();
		HashMap<String, Double> sortedMap = sortByComparator();
		// num is number of query terms. num can be varied to get query with different number of terms
		int num = 90;  
		for (Entry<String, Double> entry : sortedMap.entrySet()) {
			if (num-- > 0) {
				top100.add(entry.getKey());
			}
		}
		return top100;
	}

	/**
	 * Function to sort the hashmap by score values
	 * @return - sorted hashmap
	 */
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
	
	
	public HashMap<String, Double> getTopQueryWordsForCategoryWithScore(
			ArrayList<String> nouns, TopDocs topdocs, String category) throws IOException {
		HashMap<String, Double> scores = new HashMap<String, Double>();
		for (String nounword : nouns) {
			if (!scoreMap.containsKey(nounword)) {
				Double score = calculateScore(nounword, topdocs.scoreDocs);
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
