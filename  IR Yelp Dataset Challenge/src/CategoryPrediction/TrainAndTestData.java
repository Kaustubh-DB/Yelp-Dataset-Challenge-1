package taskone;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class TrainAndTestData {
	
	public static void main(String[] args) {
		
		MongoClient mongoClient=new MongoClient("localhost",27017);
		MongoDatabase db = mongoClient.getDatabase("YelpDatasetC");
		MongoCollection<Document> trainingCollection = db.getCollection("training_data");
		MongoCollection<Document> testCollection= db.getCollection("testing_data");
		//testCollection.fin
		insertData(mongoClient,db,trainingCollection,testCollection);
	}
	
	public static void insertData(MongoClient mc,MongoDatabase md,MongoCollection<Document> train ,MongoCollection<Document> test) {
		MongoCollection<Document> business = md.getCollection("business");
		MongoCollection<Document> reviews = md.getCollection("reviews");
		MongoCollection<Document> tips=md.getCollection("tips");
		train.drop();
		test.drop();
		FindIterable<Document> doc = business.find();
		MongoCursor<Document> cursor = doc.iterator();
		HashSet<String> categoriesSet = new HashSet<>();
		String businessId="";
		Document businessInfo;
		Document reviewDoc;
		Document tipDoc;
		int count=0;
		while (cursor.hasNext()) {
//			System.out.println(cursor.next().get("categories"));
			businessInfo=cursor.next();
			Document newDoc=new Document(businessInfo);
			//System.out.println();
			String business_id= (String) businessInfo.get("business_id");
			//String category = (String) businessInfo.get("categories");
			List <String> reviewList = new ArrayList<String>();
        	List <Double> ratingList = new ArrayList<Double>();
			FindIterable<Document> reviews_data = reviews.find(Filters.eq("business_id", business_id));
			MongoCursor<Document> reviews_cursor = reviews_data.iterator();
			
			while (reviews_cursor.hasNext())
			{
				reviewDoc=reviews_cursor.next();
				//System.out.println("review obj:  " +reviewObj);
				reviewList.add((String) reviewDoc.get("text"));
				
				if (reviewDoc.get("stars")!= null) {
					ratingList.add((double)reviewDoc.get("stars"));
				} else {
					ratingList.add((double) 0);					
				}
				 
			}
			newDoc.append("review",reviewList);
			newDoc.append("rating", ratingList);
			
			FindIterable<Document> tips_data = tips.find(Filters.eq("business_id", business_id));
			MongoCursor<Document> tips_cursor = tips_data.iterator();
			List<String> tipsList=new ArrayList<String>();
			
			while (tips_cursor.hasNext())
			{
				tipDoc=tips_cursor.next();	
				//System.out.println("tip obj:  " +tipObj);
				tipsList.add((String) tipDoc.get("text"));				
			}
			newDoc.append("tip",tipsList);
			//System.out.println(reviewList);
			//System.out.println(ratingList);
//			System.out.println(newDoc.toJson());
			System.out.println(count);
			//System.exit(0);
			
			// As per count of business user 60% of data is used for training and 40% for testing 
			// 
			if(count <= 50)
			{
				train.insertOne(newDoc);
				count++;				
			}
			else
			{
				test.insertOne(newDoc);
				count++;
			}
		}
		mc.close();
		
	}
}
