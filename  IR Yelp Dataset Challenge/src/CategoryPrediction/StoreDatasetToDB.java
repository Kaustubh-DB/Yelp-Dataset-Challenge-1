package taskone;

import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


public class StoreDatasetToDB {
	private String filePath;
	
	public static void main(String args[]) throws IOException {
		StoreDatasetToDB sd = new StoreDatasetToDB();
		sd.setFilePath("./yelp-dataset/");
		List<String> business_ids=new ArrayList<String>();
		sd.loadDataToMongoDB(sd.getFilePath()+"yelp_academic_dataset_business.json", "business",business_ids);
		sd.loadDataToMongoDB(sd.getFilePath() + "yelp_academic_dataset_review.json", "reviews",business_ids);
		sd.loadDataToMongoDB(sd.getFilePath() + "yelp_academic_dataset_tip.json", "tips",business_ids);

	}

	public void loadDataToMongoDB(String fileP, String collectionName,List<String> business) throws IOException {
		MongoClient mc = new MongoClient("localhost", 27017);
		MongoDatabase database = mc.getDatabase("YelpDataset");
		MongoCollection<Document> collection = database.getCollection(collectionName);

		collection.drop(); // If you run file again first drop the collection (Avoid Overwriting of data)

		String collectionInfo = "Collection Name " + collectionName + ": ";

		File datasetFile = new File(fileP);
		FileInputStream fis = new FileInputStream(datasetFile);
		BufferedReader bufreader = new BufferedReader(new InputStreamReader(fis));

		String line = bufreader.readLine();
		Document doc;
		int count = 0;
		while (line != null) {
			
			doc = Document.parse(line);
			if(collectionName.equals("business"))
			{
				if(doc.get("city").equals("Las Vegas")) {
					if((String) doc.get("categories")!=null) {
						String[] categoryForBusiness=((String) doc.get("categories")).split(", ");
						for(String cat:categoryForBusiness) {
							//System.out.println(cat);
							if(cat.equals("Restaurants")) {
								if(((int) doc.get("review_count")) > 100) {
									business.add((String) doc.get("business_id"));
									collection.insertOne(doc);
									count++;	
								}
							}
							
						}
					}
					
				}
			}
			if(collectionName.equals("reviews")||collectionName.equals("tips")) {
				if(business.contains((String)doc.get("business_id"))) {
					collection.insertOne(doc);
					count++;
				}
			}
			
			line = bufreader.readLine();
		}
		bufreader.close();
		mc.close();
		System.out.println("For " + collectionInfo + " " + count + " number of rows inserted.");
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

}
