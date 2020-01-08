package task1ir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

// Store data to MongoDB
	
public class DataToMongo {
	
	private HashSet<String> business_ids = new HashSet<String>();
	private HashMap<String, Integer> user_ids = new HashMap<String, Integer>();
	public HashMap<String, Integer> getUser_ids() {
		return user_ids;
	}
	
	public void setUser_ids(HashMap<String, Integer> user_ids) {
		this.user_ids = user_ids;
	}

	private HashSet<String> users_CF = new HashSet<String>();
	private HashSet<String> businessTestCollection = new HashSet<String>();

	public static void main(String[] args) throws IOException, ParseException {

		DataToMongo dc = new DataToMongo();

		dc.loadDataToMongoDB("./yelp-dataset/yelp_academic_dataset_business.json", "business");
		dc.loadDataToMongoDB("./yelp-dataset/yelp_academic_dataset_review.json", "reviews");
		dc.loadDataToMongoDB("./yelp-dataset/yelp_academic_dataset_tip.json", "tips");
		
		dc.loadDataToMongoDB("./yelp-dataset/yelp_academic_dataset_user.json", "users");
		dc.loadDataToMongoDB("./yelp-dataset/yelp_academic_dataset_review.json", "training_review");
		dc.loadDataToMongoDB("./yelp-dataset/yelp_academic_dataset_review.json", "testing_review");
	}

	public void loadDataToMongoDB(String fileP, String collectionName) throws IOException, ParseException {
		MongoClient mc = new MongoClient("localhost", 27017);
		MongoDatabase database = mc.getDatabase("YelpDatasetCF");
		MongoCollection<Document> collection = database.getCollection(collectionName);

		collection.drop(); // If you run file again first drop the collection (Avoid Overwriting of data)

		String collectionInfo = "Collection Name " + collectionName + ": ";
		String inputString = "2017-01-01";
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date inputDate = dateFormat.parse(inputString);
		File datasetFile = new File(fileP);
		FileInputStream fis = new FileInputStream(datasetFile);
		BufferedReader bufreader = new BufferedReader(new InputStreamReader(fis));

		String line = bufreader.readLine();
		Document doc;
		int count = 0;
		while (line != null) {

			doc = Document.parse(line);
			
			if (collectionName.equals("business")) {
				if (doc.get("city").equals("Phoenix")) {
					if ((String) doc.get("categories") != null) {
						String[] categoryForBusiness = ((String) doc.get("categories")).split(", ");
						for (String cat : categoryForBusiness) {
							// System.out.println(cat);
							if (cat.equals("Restaurants")) {
								if (((int) doc.get("review_count")) > 50) {
									business_ids.add((String) doc.get("business_id"));
									collection.insertOne(doc);
									count++;
								}
							}

						}
					}

				}
			}else if (collectionName.equals("reviews")) {
				if (business_ids.contains((String) doc.get("business_id"))) {
					collection.insertOne(doc);
					count++;
					if (user_ids.containsKey(doc.getString("user_id"))) {
						user_ids.put(doc.getString("user_id"), user_ids.get(doc.getString("user_id")) + 1);
					} else {
						user_ids.put(doc.getString("user_id"), 1);
					}
				}
			}else if (collectionName.equals("users")) {
				String user_id = doc.getString("user_id");
				if (user_ids.containsKey(user_id)) {
					if (user_ids.get(user_id) >= 20) {
						users_CF.add(doc.getString("user_id"));
						collection.insertOne(doc);
						count++;
					}
				}
			}else if (collectionName.equals("training_review")) {
				Date date = dateFormat.parse(doc.getString("date"));
				if (users_CF.contains(doc.getString("user_id"))
						&& business_ids.contains(doc.getString("business_id"))) {
					if (date.compareTo(inputDate) < 0) {
						collection.insertOne(doc);
						businessTestCollection.add(doc.getString("business_id"));
						count++;
					}
				}
			} else if (collectionName == "testing_review") {

				Date date = dateFormat.parse(doc.getString("date"));
				if (users_CF.contains((String) doc.getString("user_id"))
						&& businessTestCollection.contains(doc.getString("business_id"))) {
					if (date.compareTo(inputDate) > 0){
							collection.insertOne(doc);
							count++;
						}
				}
			}
			line = bufreader.readLine();
		}
		bufreader.close();
		mc.close();
		System.out.println("For " + collectionInfo + " " + count + " number of rows inserted.");
	}
}
