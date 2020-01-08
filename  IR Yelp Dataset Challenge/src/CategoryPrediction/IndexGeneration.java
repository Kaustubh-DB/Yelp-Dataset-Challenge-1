package taskone;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.document.Field;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class IndexGeneration {
	private static String indexPath = ("./index");

	public static void main(String[] args) throws IOException {
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase db = mongoClient.getDatabase("YelpDataset");
		// String testingIndexPath =
		// ("C:\\\\Users\\\\kaust\\\\Downloads\\\\yelp-dataset\\\\Index\\\\Testing");
		MongoCollection<org.bson.Document> training_collection = db.getCollection("training_data");
		MongoCollection<org.bson.Document> testing_collection = db.getCollection("testing_data");
		createTrainingnTestingIndex("Training_set",training_collection);
		createTrainingnTestingIndex("Testing_set",testing_collection);
		mongoClient.close();
		Directory dir1;
		Directory dir2;
		dir1 = FSDirectory.open(Paths.get(indexPath +"/Training_set"));
		dir2 = FSDirectory.open(Paths.get(indexPath +"/Testing_set"));
        IndexReader trainingReader = DirectoryReader.open(dir1);
        int trainingDocs = trainingReader.maxDoc();
        System.out.println("The total docs in training index file are:" +trainingDocs);
        IndexReader testReader = DirectoryReader.open(dir2);
        int testDocs = testReader.maxDoc();
        System.out.println("The total docs in test index file are:" +testDocs);
	}

	private static void createTrainingnTestingIndex(String collectionName,MongoCollection<org.bson.Document> trainOrTestCollection ) throws IOException {
		
//		MongoCollection<org.bson.Document> table = db.getCollection("training_data");
//		MongoCollection<org.bson.Document> table2 = db.getCollection("testing_data");
		
		MongoCursor<org.bson.Document> cursor = trainOrTestCollection.find().iterator();

		Directory dir;
		try {
			dir = FSDirectory.open(Paths.get(indexPath + "/" + collectionName));
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
			IndexWriter writer = new IndexWriter(dir, iwc);

			while (cursor.hasNext()) {
				// Create a lucene document and add to lucene index
				org.bson.Document businessDoc = cursor.next();
				Document ldoc = new Document();

				String businessID = (String) businessDoc.get("business_id");
				//System.out.println(businessID);
				ldoc.add(new StringField("business_id", businessID, Field.Store.YES));
				
				if ((String) businessDoc.get("categories") != null) {
					String[] categoryForBusiness = ((String) businessDoc.get("categories")).split(", ");
					for (String cat : categoryForBusiness) {
						ldoc.add(new StringField("categories", cat, Field.Store.YES));
					}
				}
					@SuppressWarnings("unchecked")
					ArrayList<String> reviews = (ArrayList<String>) businessDoc.get("review");
					String reviewAndTips = "";
					if (reviews != null) {
						for (String str : reviews) {
							reviewAndTips+=str;
						}
					}
					@SuppressWarnings("unchecked")
					ArrayList<String> tips = (ArrayList<String>) businessDoc.get("tip");
//					System.out.println(tips.size());
					for (String str : tips) {
						reviewAndTips+=str;
					}
					ldoc.add(new TextField("reviewstips", reviewAndTips, Field.Store.YES));
//					System.out.println(ldoc.get("REVIEWSTIPS"));
//					System.exit(0);
					//System.out.println(ldoc);
					writer.addDocument(ldoc);
				}
			cursor.close();
			writer.forceMerge(1);
			writer.commit();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}