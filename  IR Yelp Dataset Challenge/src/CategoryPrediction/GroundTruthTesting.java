package taskone;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class GroundTruthTesting {
	public static String groundTruthFile = "./OutputFiles/GroundTruth_testing.txt";
	public static String categoriesFile = "./InputFiles/categories.txt";
	public static String testIndex = "./index/Testing_set";
	public static HashSet<String> categories = new HashSet<String>();
	public static HashMap<String, ArrayList<String>> gtmap = new HashMap<String, ArrayList<String>>();

	public static void main(String[] args) throws IOException {

		categories = readCategoriesFromFile();
//		generateGroundTruth(reader,searcher);
		System.out.println(categories);
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase db = mongoClient.getDatabase("YelpDatasetC");

		MongoCollection<Document> testCollection = db.getCollection("testing_data");
		FindIterable<Document> doc = testCollection.find();
		MongoCursor<Document> cursor = doc.iterator();
		HashMap<String, ArrayList<String>> gtmap = new HashMap<String, ArrayList<String>>();

		while (cursor.hasNext()) {
			Document temp_doc = cursor.next();
			String s = (String) temp_doc.get("categories");
			System.out.println("Categories of business :" + s);
			for (String category : categories) {
				category = category.trim();
				System.out.println("Category : " + category);

//					System.out.println(temp_doc.toJson());

				if (s.contains(category)) {
					if (gtmap.containsKey(category)) {
						System.out.println(temp_doc.getString("business_id"));
						gtmap.get(category).add(temp_doc.getString("business_id"));
					} else {
						ArrayList<String> temp = new ArrayList<String>();
						temp.add(temp_doc.getString("business_id"));
						gtmap.put(category, temp);
					}
				}
			}
		}

		System.out.println(gtmap);
	}

	private static HashSet<String> readCategoriesFromFile() throws IOException {
		HashSet<String> temp_cat = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(categoriesFile));
		String cat = "";
		while ((cat = br.readLine()) != null) {
			temp_cat.add(cat);
		}
		br.close();
		return temp_cat;
	}
}
