package task1colaborative;
/*
 Implements Item Based collaborative filtering algorithm.
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
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

public class ItemBasedRecommendation  {
	public static HashMap<String,Long> userBasedMapUserCF = new HashMap<String,Long>();
	public static HashMap<String,Long> businessMapUserCF = new HashMap<String,Long>();
	public static Map<String, Collection<String>> useritemMap = new HashMap<String, Collection<String>>();
	public static Map<String, Collection<String>> recommendationMap = new HashMap<String, Collection<String>>();
	public static HashMap<String,Long> newuserMap=new HashMap<String,Long>();
	public static Map<String, Collection<String>> groundTruthMap = new HashMap<String, Collection<String>>();

	
	public static void main(String[] args) throws IOException, TasteException
	{
		ConvertDataintoMatrix.main(args);
		userBasedMapUserCF.putAll(ConvertDataintoMatrix.usersMap);
		businessMapUserCF.putAll(ConvertDataintoMatrix.businessMap);
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("C:\\Users\\kaust\\Downloads\\yelp-dataset\\Recommendation\\testoutputttt.csv"));		
		BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\Users\\kaust\\Downloads\\yelp-dataset\\Training&GroundTruth\\ground_truth.csv"));			
		//Data Model Reads the input csv file and passes to the recommender
		DataModel dataModel = new FileDataModel(new File("C:\\Users\\kaust\\Downloads\\yelp-dataset\\Training&GroundTruth\\userbusinesscode.csv"));
		
		//Recommender Builder builds recommendation model and parses the data sent by data model
		RecommenderBuilder recommenderBuilder=new RecommenderBuilder() 
		{
			//Tried using all the similarities, best result were obtained by person Correlation similarity
			public Recommender buildRecommender(DataModel dm) throws TasteException{
				
				ItemSimilarity sim = new PearsonCorrelationSimilarity(dm); 
				//TanimotoCoefficientSimilarity sim = new TanimotoCoefficientSimilarity(dm);
				//ItemSimilarity sim = new LogLikelihoodSimilarity(dm);
//				ItemSimilarity sim = new EuclideanDistanceSimilarity(dm);
				//ItemSimilarity similarity = new GenericUserSimilarity(model);
				return  new GenericItemBasedRecommender(dm, sim);
			}
		};
		
		for(String everyKey:userBasedMapUserCF.keySet())
		{
			Recommender recommender=recommenderBuilder.buildRecommender(dataModel);
			List<RecommendedItem>recommendations = recommender.recommend(userBasedMapUserCF.get(everyKey), 25);
			for(RecommendedItem recommendedItem : recommendations) 
			{
				String finalrecommendation=null;
				for (Entry<String, Long> entry : businessMapUserCF.entrySet()) 
				{
					if (Objects.equals(recommendedItem.getItemID(), entry.getValue())) 
					{
						finalrecommendation=entry.getKey();
						Collection<String> newrecomms=recommendationMap.get(everyKey);
						if (newrecomms==null)
						{
							newrecomms=new ArrayList<String>();
							recommendationMap.put(everyKey, newrecomms);
						}
						newrecomms.add(finalrecommendation);
					}
				}	

			}
				
		}
		String line = "";
		String cvsSplitBy = ",";
		while((line = bufferedReader.readLine()) != null ) 
	    	{ 
			String[] items = line.split(cvsSplitBy);
			if(recommendationMap.containsKey(items[0]))
			{
				bufferedWriter.write(items[0]+","+recommendationMap.get(items[0])+"\n");
			}
	    	}		
		bufferedWriter.close();
		bufferedReader.close();
		EvaluationMetrics.main(args);		
	}

}
