package taskone;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
public class GenerateQuery {
	
	public static String categoriesFile = "./InputFiles/categories.txt";
	public static String trainingIndex="./index/Training_set";
	public static String posFile="./InputFiles/posFile.txt";
	public static String topQueryFile="./InputFiles/queries.txt";
	public static String taggermodel = "./stanford-postagger/models/english-left3words-distsim.tagger";
	
	
	
	public static void main(String[] args) throws IOException, ParseException {
		
		HashSet<String> categories=new HashSet<String>();
		categories=readCategoriesFromFile();
		
		IndexReader reader= DirectoryReader.open(FSDirectory.open(Paths.get(trainingIndex)));
		IndexSearcher searcher = new IndexSearcher(reader);
		
		buildQueries(categories,reader,searcher);
}

	private static HashSet<String> readCategoriesFromFile() throws IOException {
		HashSet<String> temp_cat=new HashSet<String>();
		BufferedReader br=new BufferedReader(new FileReader(categoriesFile)); 
		String cat="";
		while ((cat=br.readLine())!=null ) {
			temp_cat.add(cat);
		}
		br.close();
		return temp_cat;
	}
	
	private static void buildQueries(HashSet<String> categories,IndexReader reader,IndexSearcher searcher) throws IOException, ParseException {
		System.out.println(categories.size());
		deleteFile(topQueryFile);
		for(String category:categories) {
			category=category.trim();
			
			TermQuery tq=new TermQuery(new Term("categories",category));
			
//			System.out.println(tq.toString());
			TopDocs topDocs = searcher.search(tq, 1000);
			
			String reviewsTips= getReviewAndTipsforCategory(topDocs,reader);
			
//			System.out.println("reviews "+reviewsTips);
			
			createReviewfile(reviewsTips);
			
			ArrayList<String> nouns = getNounsForCategory(reviewsTips);
			
			ScoreCalculator sc = new ScoreCalculator();
			ArrayList<String> topNouns = sc.getTopQueryWordsForCategory(nouns, topDocs, category);
			
			//System.out.println("topNouns:"+topNouns);
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(topQueryFile,true));
			String topListNoun =category+":"+topNouns.toString();
			bw.write(topListNoun);
			bw.write("\n");
			bw.close();
		}
	}

	public static void deleteFile(String filename) {

		try {

			File file = new File(filename);

			if (file.delete()) {
				System.out.println(file.getName() + " is deleted!");
			} else {
				System.out.println("Delete operation is failed.");
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

	}
	
	private static void createReviewfile(String reviewsTips) throws IOException {
		deleteFile(posFile);
		reviewsTips.replaceAll(".", " ");
		BufferedWriter bw = new BufferedWriter(new FileWriter(posFile,true));
		bw.write(reviewsTips);
		bw.close();
	}

	private static ArrayList<String> getNounsForCategory(String reviewsTips) throws FileNotFoundException, UnsupportedEncodingException {
		ArrayList<String> temp_nouns=new ArrayList<String>();
		
		MaxentTagger tagger = new  MaxentTagger(taggermodel);
		
		TokenizerFactory<CoreLabel> tokenizerFactory= PTBTokenizer.factory(new CoreLabelTokenFactory(), "untokenizable=noneKeep");
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(posFile),"utf-8"));
		
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out,"utf-8"));

		DocumentPreprocessor docPrePro= new DocumentPreprocessor(br);
		docPrePro.setTokenizerFactory(tokenizerFactory);
		
		for(List<HasWord> sentence: docPrePro) {
			List<TaggedWord> tSentence=tagger.tagSentence(sentence);
//			System.out.println(tSentence);
			for(TaggedWord tw: tSentence) {
//				System.out.println(tw.tag());
				if(tw.tag().equals("NN")) {
					
					temp_nouns.add(tw.word());
				}
			}
		}
		pw.close();
//			System.out.println("temp list:"+temp_nouns);
		return temp_nouns;
	}

	private static String getReviewAndTipsforCategory(TopDocs topDocs,IndexReader reader) throws IOException {
		
		String temp_str="";
		
		for(ScoreDoc sd : topDocs.scoreDocs) {
			Document doc;
			doc= reader.document(sd.doc);
			//System.out.println(doc.toString());
			String reviewTip = doc.get("reviewstips");
			temp_str=temp_str.concat(reviewTip + " ");
			
		}
		return temp_str;
	}
}
