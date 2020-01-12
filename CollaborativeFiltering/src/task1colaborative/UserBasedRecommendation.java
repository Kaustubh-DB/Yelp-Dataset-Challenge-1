package task1colaborative;
/*
 *Implements User Based collaborative filtering algorithm.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;


public class UserBasedRecommendation  {
	public static HashMap<String,Long> userMapUserCF = new HashMap<String,Long>();
	public static HashMap<String,Long> businessMapUserCF = new HashMap<String,Long>();
	public static Map<String, Collection<String>> useritemMapUserCF = new HashMap<String, Collection<String>>();
	public static Map<String, Collection<String>> recomsimMapUsercf = new HashMap<String, Collection<String>>();
	public static HashMap<String,Long> newuserMap=new HashMap<String,Long>();
	public static Map<String, Collection<String>> groundTruthMap = new HashMap<String, Collection<String>>();

	
	public static void main(String[] args) throws IOException, TasteException
	{
		ConvertDataintoMatrix.main(args);
		userMapUserCF.putAll(ConvertDataintoMatrix.usersMap);
		businessMapUserCF.putAll(ConvertDataintoMatrix.businessMap);
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("C:\\Users\\kaust\\Downloads\\yelp-dataset\\Recommendation\\outputnew50.csv"));		
		BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\Users\\kaust\\Downloads\\yelp-dataset\\Training&GroundTruth\\ground_truth.csv"));			
		//Data Model Reads the input csv file and passes to the recommender
		DataModel dataModel = new FileDataModel(new File("C:\\Users\\kaust\\Downloads\\yelp-dataset\\Training&GroundTruth\\userbusinesscode.csv"));
		
		
		RecommenderBuilder recommenderBuilder=new RecommenderBuilder() 
		{
			//Recommender Builder builds recommendation model and parses the data sent by data model
			//Tried using all the similarities, best result were obtained by person Correlation similarity
			public Recommender buildRecommender(DataModel dataModel) throws TasteException{
			//TanimotoCoefficientSimilarity sim = new TanimotoCoefficientSimilarity(dm);
			//	ItemSimilarity sim = new LogLikelihoodSimilarity(dm);
			
//				ItemSimilarity sim = new EuclideanDistanceSimilarity(dm);
				//ItemSimilarity similarity = new GenericUserSimilarity(model);
//				ItemSimilarity sim = new PearsonCorrelationSimilarity(dm);
				UserSimilarity similarity = new PearsonCorrelationSimilarity(dataModel); 
				
				//Used K nearest Neighbor Algorithm
				UserNeighborhood neighborhood = new NearestNUserNeighborhood(25, similarity, dataModel); 

				return  new GenericUserBasedRecommender(dataModel,neighborhood, similarity);
			}
		};
		for(String key:userMapUserCF.keySet())
		{
			Recommender recommender=recommenderBuilder.buildRecommender(dataModel);
			List<RecommendedItem>recommendations = recommender.recommend(userMapUserCF.get(key), 50);
			for(RecommendedItem recommendedItem : recommendations) 
			{
				String finalRecommendation=null;
				for (Entry<String, Long> entry : businessMapUserCF.entrySet()) 
				{
					if (Objects.equals(recommendedItem.getItemID(), entry.getValue())) 
					{
						finalRecommendation=entry.getKey();
						Collection<String> newrecomms=recomsimMapUsercf.get(key);
						if (newrecomms==null)
						{
							newrecomms=new ArrayList<String>();
							recomsimMapUsercf.put(key, newrecomms);
						}
						newrecomms.add(finalRecommendation);
					}
				}	
			}
			}
		String line = "";
		String cvsSplitBy = ",";
		while((line = bufferedReader.readLine()) != null ) 
	    	{ 
			String[] items = line.split(cvsSplitBy);
			if(recomsimMapUsercf.containsKey(items[0]))
			{
				bufferedWriter.write(items[0]+","+recomsimMapUsercf.get(items[0])+"\n");
			}
	    	}		
		bufferedWriter.close();
		bufferedReader.close();
		EvaluationMetrics.main(args);		
	}

}
