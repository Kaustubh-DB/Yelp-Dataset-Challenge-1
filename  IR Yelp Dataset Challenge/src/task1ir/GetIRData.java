package task1ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class GetIRData {
	// Get Training Data from MongoDb and Store in HashMap for UserReview
	public HashMap<String, List<String>> getUserReviewData() {
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase db = mongoClient.getDatabase("YelpDatasetCF");
		MongoCollection<Document> trainingCollection = db.getCollection("training_review");
		// MongoCollection<Document> testCollection =
		// db.getCollection("testing_review");
		HashMap<String, List<String>> userAndReviewMap = new HashMap<String, List<String>>();

		FindIterable<Document> train_docs = trainingCollection.find();
		MongoCursor<Document> train_cursor = train_docs.iterator();
		// List<Document> train_data = new ArrayList<Document>();

		while (train_cursor.hasNext()) {
			Document doc = train_cursor.next();
			String user_id = doc.getString("user_id");

			if (userAndReviewMap.containsKey(user_id)) {
				userAndReviewMap.get(user_id).add(doc.getString("text"));

			} else {
				List<String> review_list = new ArrayList<String>();
				review_list.add(doc.getString("text"));
				userAndReviewMap.put(user_id, review_list);
			}
		}
		mongoClient.close();
		return userAndReviewMap;

	}
	// Get Training Data from MongoDb and Store in HashMap for BusinessReview
	public HashMap<String, String> getBusinessReviewData() {
		
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase db = mongoClient.getDatabase("YelpDatasetCF");
		MongoCollection<Document> trainingCollection = db.getCollection("training_review");
		// MongoCollection<Document> testCollection =
		// db.getCollection("testing_review");
		HashMap<String, String> businessAndReviewMap = new HashMap<String, String>();

		FindIterable<Document> train_docs = trainingCollection.find();
		MongoCursor<Document> train_cursor = train_docs.iterator();
		// List<Document> train_data = new ArrayList<Document>();
		//MongoCollection<Document> tips = db.getCollection("training_review");
		
		while (train_cursor.hasNext()) {
			Document doc = train_cursor.next();
			String business_id = doc.getString("business_id");
			if (businessAndReviewMap.containsKey(business_id)) {
				businessAndReviewMap.put(business_id,businessAndReviewMap.get(business_id)+doc.getString("text"));
			} else {
				businessAndReviewMap.put(business_id, doc.getString("text"));
			}
		}
		mongoClient.close();
		return businessAndReviewMap;

	}
}
