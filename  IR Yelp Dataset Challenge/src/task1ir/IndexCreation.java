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
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class IndexCreation {

	public static String posFile="./task1Input/posFile.txt";
	public static String taggermodel = "./stanford-postagger/models/english-left3words-distsim.tagger";
	
	// Indexing for Business - Reviews . Extracting nouns, adjectives and then indexing
	
	public static void main(String[] args) throws IOException {
		GetIRData gd =new GetIRData();
		HashMap<String,String> businessReview=gd.getBusinessReviewData();
		
		Directory dir = FSDirectory.open(Paths.get("./task1index/Training_set"));
		Analyzer analyzer = new StandardAnalyzer();
		
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		IndexWriter writer = new IndexWriter(dir, iwc);
		
		// Extracting Business Reviews for indexing
		for(String business_id :businessReview.keySet()) {
			Document ldoc = new Document();
			
			ldoc.add(new StringField("business_id", business_id, Field.Store.YES));
			
			createReviewfile(businessReview.get(business_id));
			String reviewNouns = getPOS();
			
			
			ldoc.add(new TextField("reviews",reviewNouns , Field.Store.YES));
			
			writer.addDocument(ldoc);
		}
		writer.forceMerge(1);
		writer.commit();
		writer.close();
	}

	private static void createReviewfile(String reviewsTips) throws IOException {
		File f = new File(posFile);
		f.delete();
		reviewsTips.replaceAll(".", " ");
		BufferedWriter bw = new BufferedWriter(new FileWriter(posFile,true));
		bw.write(reviewsTips);
		bw.close();
	}
		// Using stanford NLP Library for POS tagging 
	private static String getPOS() throws FileNotFoundException, UnsupportedEncodingException {
		String temp_nouns="";
		
		MaxentTagger tagger = new  MaxentTagger(taggermodel);
		
		TokenizerFactory<CoreLabel> tokenizerFactory= PTBTokenizer.factory(new CoreLabelTokenFactory(), "untokenizable=noneKeep");
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(posFile),"utf-8"));
		
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out,"utf-8"));

		DocumentPreprocessor docPrePro= new DocumentPreprocessor(br);
		docPrePro.setTokenizerFactory(tokenizerFactory);
		
		for(List<HasWord> sentence: docPrePro) {
			List<TaggedWord> tSentence=tagger.tagSentence(sentence);
			//System.out.println(tSentence);
			for(TaggedWord tw: tSentence) {
//				System.out.println(tw.tag());
				if(tw.tag().equals("NN")||tw.tag().equals("NNP")||tw.tag().equals("JJ")) {
					temp_nouns += tw.word()+" ";
				}
			}
		}
		pw.close();
//			System.out.println("temp list:"+temp_nouns);
		return temp_nouns;
	}
}
