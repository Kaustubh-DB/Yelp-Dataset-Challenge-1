package task1ir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.FSDirectory;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class RecommendBusiness {

	public static String posReviewFile = "./task1Input/posReviewFile.txt";
	public static String taggermodel = "./stanford-postagger/models/english-left3words-distsim.tagger";
	public static String trainingIndex = "./task1index/Training_set";
	public static String IRFile = "./task1Input/IR.csv";

	
	// Query Expansion using POS tagging and Getting TFIDF for each word for considering top 100 words
	
	public static void main(String[] args) throws IOException, ParseException {
		File irFile = new File(IRFile);
		irFile.delete();
		GetIRData gd = new GetIRData();
		HashMap<String, List<String>> userReviewMap = gd.getUserReviewData();
		ArrayList<String> stopwords = new ArrayList<String>();
		String filename = "./task1Input/Irrelevant_words.txt";
		File datasetFile = new File(filename);
		FileInputStream fis = new FileInputStream(datasetFile);
		BufferedReader bufreader = new BufferedReader(new InputStreamReader(fis));
		
		String line = bufreader.readLine();

		while (line != null) {
			stopwords.add(line);
			line = bufreader.readLine();
		}
		bufreader.close();
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(trainingIndex)));
		IndexSearcher searcher = new IndexSearcher(reader);

		searcher.setSimilarity(new LMJelinekMercerSimilarity((float) 0.7));
		
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("reviews", analyzer);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(IRFile, true));

		String str = "";
		int rank;
		Query query;
		ScoreDoc[] sDocs;
		System.out.println(userReviewMap.size());
		int countUser=0;
		for (String user_id : userReviewMap.keySet()) {
			System.out.println(countUser);
			countUser++;
			String reviews = "";
			for (String s : userReviewMap.get(user_id)) {
				reviews += s;
			}
			createReviewfile(reviews);
			ArrayList<String> tempNounReviewList = getNounsListUserReview();
			TFIDF sc = new TFIDF();
			
			ArrayList<String> topNouns = sc.getTopQueryWords(tempNounReviewList);
			
		
			String nounReviewList = "";
			int count=0;
			
			for (String nouns : topNouns) {
				if(count>1023)
					break;
				if (!stopwords.contains(nouns)) {
					nounReviewList += nouns + " ";
					count++;
				}
				
			}
			
			rank = 1;
			
			query = parser.parse(QueryParser.escape(nounReviewList.toString().trim()));
	
			// top 10 prediction 
			TopDocs results = searcher.search(query, 10);
			sDocs = results.scoreDocs;
			
			for (int i = 0; i < sDocs.length; i++) {
				Document doc = searcher.doc(sDocs[i].doc);
				str = user_id + "," + doc.get("business_id") + "," + rank + "," + sDocs[i].score;
				rank++;
				bw.write(str + "\n");
			}
		
		}
		bw.close();
		reader.close();
	}

	private static void createReviewfile(String reviewsTips) throws IOException {
		File f = new File(posReviewFile);
		f.delete();
		reviewsTips.replaceAll(".", " ");
		BufferedWriter bw = new BufferedWriter(new FileWriter(posReviewFile, true));
		bw.write(reviewsTips);
		bw.close();
	}

	
	public static ArrayList<String> getNounsListUserReview()
			throws UnsupportedEncodingException, FileNotFoundException {
		ArrayList<String> temp_nouns = new ArrayList<String>();

		MaxentTagger tagger = new MaxentTagger(taggermodel);

		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(),
				"untokenizable=noneKeep");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(posReviewFile), "utf-8"));

		PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out, "utf-8"));

		DocumentPreprocessor docPrePro = new DocumentPreprocessor(br);
		docPrePro.setTokenizerFactory(tokenizerFactory);

		for (List<HasWord> sentence : docPrePro) {
			List<TaggedWord> tSentence = tagger.tagSentence(sentence);
//			System.out.println(tSentence);
			for (TaggedWord tw : tSentence) {
//				System.out.println(tw.tag());
				if (tw.tag().equals("NN")||tw.tag().equals("NNP")||tw.tag().equals("JJ")) {

					temp_nouns.add(tw.word());
					
				}
			}
		}

//			System.out.println("temp list:"+temp_nouns);
		return temp_nouns;

	}
}
