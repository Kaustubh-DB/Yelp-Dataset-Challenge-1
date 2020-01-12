package task1colaborative;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
/*
 * Write the data from Training and Testing Collection into a CSV File. 
 */
public class GenerateTrainnTestCSV {

	public static void main(String[] args) throws IOException {
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase db = mongoClient.getDatabase("YelpDatasetPhoenix");
		MongoCollection<Document> trainingCollection = db.getCollection("training_review");
		MongoCollection<Document> testCollection = db.getCollection("testing_review");

		FindIterable<Document> train_docs = trainingCollection.find();
		MongoCursor<Document> train_cursor = train_docs.iterator();
		List<Document> train_data = new ArrayList<Document>();

		while (train_cursor.hasNext()) {
			train_data.add(train_cursor.next());
		}
		train_cursor.close();
		String trainFile = "C:\\Users\\kaust\\Downloads\\yelp-dataset\\Training&GroundTruth\\train_csv.csv";
		writeToCSV(train_data, trainFile);

		FindIterable<Document> test_docs = testCollection.find();
		MongoCursor<Document> test_cursor = test_docs.iterator();
		List<Document> test_data = new ArrayList<Document>();

		while (test_cursor.hasNext()) {
			test_data.add(test_cursor.next());
		}
		test_cursor.close();
		String testFile = "C:\\Users\\kaust\\Downloads\\yelp-dataset\\Training&GroundTruth\\ground_truth.csv";
		writeToCSV(test_data, testFile);
		mongoClient.close();
	}

	//Write to Comma seperated File
	private static void writeToCSV(List<Document> train_data, String filename) throws IOException {
		File f = new File(filename);
		f.delete();
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filename)));
		for (Document data : train_data) {
			String s =Stream.of(data.getString("user_id"), data.getString("business_id"), data.getDouble("stars").toString())
            .map(value -> value.replaceAll("\"", "\"\""))
            .map(value -> Stream.of("\"", ",").anyMatch(value::contains) ? "\"" + value + "\"" : value)
            .collect(Collectors.joining(","));
			
			bw.write(s);
			bw.write("\n");

		}
		bw.close();

	}
}
