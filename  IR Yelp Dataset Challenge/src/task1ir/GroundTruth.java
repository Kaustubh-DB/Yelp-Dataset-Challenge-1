package task1ir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class GroundTruth {
	// Generating GroundTruth CSV using testing data
	
	public static void main(String[] args) throws IOException {
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase db = mongoClient.getDatabase("YelpDatasetCF");
		MongoCollection<Document> testCollection = db.getCollection("testing_review");

		FindIterable<Document> test_docs = testCollection.find();
		MongoCursor<Document> test_cursor = test_docs.iterator();
		List<Document> test_data = new ArrayList<Document>();

		while (test_cursor.hasNext()) {
			test_data.add(test_cursor.next());
		}
		test_cursor.close();
		String testFile = "./task1Input/ground_truth.csv";
		writeToCSV(test_data, testFile);
		mongoClient.close();
	}
	
	private static void writeToCSV(List<Document> train_data, String filename) throws IOException {
		File f = new File(filename);
		f.delete();
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filename)));

		for (Document data : train_data) {
			String s = data.getString("user_id")+","+ data.getString("business_id")+","+data.getDouble("stars").toString();
			
			bw.write(s);
			bw.write("\n");

		}
		bw.close();
	}
}
