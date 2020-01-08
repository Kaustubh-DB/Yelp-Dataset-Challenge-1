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
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class GenerateGroundTruth {
	public static String groundTruthFile = "./OutputFiles/GroundTruth_final.txt";
	public static String categoriesFile = "./InputFiles/categories.txt";
	public static String testIndex = "./index/Testing_set";
	public static HashSet<String> categories=new HashSet<String>();
	
	public static HashMap<String,List<String>> groundTruthMap=new HashMap<String,List<String>>();
	
	
	public static void setgroundTruthMap(HashMap<String,List<String>> map) {
		groundTruthMap = map;            
    }
	 public HashMap<String,List<String>> getgroundTruthMap() {
         return groundTruthMap;
	 }
    
	public static void main(String[] args) throws IOException {
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(testIndex)));
		IndexSearcher searcher = new IndexSearcher(reader);
		categories=readCategoriesFromFile(categoriesFile);
		File f=new File(groundTruthFile);
		f.delete();
		generateGroundTruth(reader,searcher, categories);
		//System.out.println(groundTruthMap);
	}

	public static void generateGroundTruth(IndexReader reader, IndexSearcher searcher, HashSet<String> categories) throws IOException {
			for (String category : categories) {
				//System.out.println(category);
				category = category.trim();
				
				//get topdocs for category
				TopDocs topdocs = getDocsForCategory(category,searcher);

				ArrayList<String> bussIds = new ArrayList<String>();
				
				//for each document in scoredocs, extract the business id and add to list
				for (ScoreDoc scoreDoc : topdocs.scoreDocs) {
					Document doc = reader.document(scoreDoc.doc);
					String businessId = doc.get("business_id");
					bussIds.add(businessId);

				}
				groundTruthMap.put(category, bussIds);
				setgroundTruthMap(groundTruthMap);
				String toStringtopBussineessIDS = category + " : " + bussIds.toString();
				BufferedWriter bw = new BufferedWriter(new FileWriter(groundTruthFile,true));
				bw.write(toStringtopBussineessIDS);
				bw.write("\n\n");
				bw.close();
			}
			System.out.println();

	}

	public static TopDocs getDocsForCategory(String category, IndexSearcher searcher) throws IOException {
		TermQuery qry = new TermQuery(new Term("categories", category));
		TopDocs topdocs = null;
			topdocs = searcher.search(qry, Integer.MAX_VALUE);
		return topdocs;
	}

	
	public static HashSet<String> readCategoriesFromFile(String categoriesFile) throws IOException {
		HashSet<String> temp_cat=new HashSet<String>();
		BufferedReader br=new BufferedReader(new FileReader(categoriesFile)); 
		String cat="";
		while ((cat=br.readLine())!=null ) {
			temp_cat.add(cat);
		}
		br.close();
		return temp_cat;
	}
}
