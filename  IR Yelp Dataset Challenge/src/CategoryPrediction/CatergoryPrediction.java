package taskone;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.FSDirectory;


public class CatergoryPrediction {

	public static String categoriesFile = "./InputFiles/categories.txt";
	public static String trainingIndex="./index/Training_set";
	public static String testIndex="./index/Testing_set";
	public static String posFile="./InputFiles/posFile.txt";
	public static String topQueryFile="./InputFiles/queries.txt";
	public static String outputFilePath="./OutputFiles/output.txt";
	
	public static IndexReader reader;
	public static IndexSearcher searcher;
//	public static HashMap<String,Integer> categoryMap = new HashMap<String, Integer>();
	public static HashMap<String,List<String>> userBusinessMap=new HashMap<String,List<String>>();
	
	
	public static void setuserBusinessMap(HashMap<String,List<String>> map) {
        userBusinessMap = map;            
    }
	 public HashMap<String,List<String>> getuserBusinessMap() {
         return userBusinessMap;
	 }
         
	public CatergoryPrediction() throws IOException {
//			reader= DirectoryReader.open(FSDirectory.open(Paths.get(testIndex)));
//			searcher = new IndexSearcher(reader);
	}
	
	private static HashMap<String, Integer> categoryMap = new HashMap<String, Integer>();
	public static void main(String[] args) throws IOException, ParseException {
		String topQueryFile="./InputFiles/queries.txt";	
		ArrayList<String> queries = getQueries(topQueryFile);
		//System.out.println(queries);
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(testIndex)));
		IndexSearcher searcher = new IndexSearcher(reader);
		File file = new File(outputFilePath);
		file.delete();
		rankDocumentsForQuery(queries,searcher,reader);
		System.out.println(userBusinessMap);
	}

	public static void rankDocumentsForQuery(ArrayList<String> queries, IndexSearcher searcher, IndexReader reader) throws ParseException, IOException {
		ArrayList<String> algoList = new ArrayList<String>();
		Analyzer analyzer = new StandardAnalyzer();
		algoList.add("BM25");
		algoList.add("VSM");
		algoList.add("LMD");
		algoList.add("LMJ");
		for(int i=0;i<algoList.size();i++) {
			if(algoList.get(i)== "LMJ") {
				searcher.setSimilarity(new LMJelinekMercerSimilarity(
						(float) 0.7));
			}
		}
		for(String str :queries) {
			if(str.equals(""))
				continue;
			String[] strings = str.split(":");
			String categoryPredictor = strings[0].trim();
			//System.out.println(categoryPredictor);
			if (!categoryMap.containsKey(categoryPredictor))
				categoryMap.put(categoryPredictor, categoryMap.keySet().size() + 1);
			//System.out.println(categoryMap);
			QueryParser parser = new QueryParser("reviewstips", analyzer);
			Query query;

			query = parser.parse(parser.escape(strings[1].trim()));
			System.out.println(query);
			
			TopDocs results = searcher.search(query, Integer.MAX_VALUE);
			
			System.out.println(results.totalHits);
			System.exit(0);
//			System.out.println(results.scoreDocs.);
//			System.exit(0);
			saveScore(results.scoreDocs, categoryPredictor,reader,searcher);
		}
		
		
		
	}

	
	/**
	 * Function to extract top 75 business ids for each category and save the output to file
	 * @param docs
	 * @param category
	 * @param searcher 
	 * @param reader 
	 */
	public static void saveScore(ScoreDoc[] docs, String category, IndexReader reader, IndexSearcher searcher) {
	//	HashMap<String,List<String>> userBusinessMap=new HashMap<String,List<String>>();
		//List<String> res=new ArrayList<String>();
		int len = Math.min(80, docs.length);
		ArrayList<String> bussIds = new ArrayList<String>();
		try {
			//extract business id for doc and add to list
			for (ScoreDoc scoreDoc : docs) {
				
				Document doc = reader.document(scoreDoc.doc);
				//System.out.println(doc.toString());
				String businessId = doc.get("business_id");
				bussIds.add(businessId);
				len--;
				if (len <= 0)
					break;

			}
			userBusinessMap.put(category, bussIds);
			setuserBusinessMap(userBusinessMap);
			String toStringtopBussineessIDS = category + " : " + bussIds.toString();
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath,true));
			bw.write(toStringtopBussineessIDS);
			bw.write("\n\n");
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<String> getQueries(String posFile) throws IOException {
		ArrayList queriesList = new ArrayList<String>();
		BufferedReader in = new BufferedReader(new FileReader(topQueryFile));
		String str;
		while ((str = in.readLine()) != null) {
			queriesList.add(str);
		}
		return queriesList;
	}

}


