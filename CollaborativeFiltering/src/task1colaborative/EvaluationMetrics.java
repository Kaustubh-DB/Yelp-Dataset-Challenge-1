package task1colaborative;
/*
 *Calculate precision, recall and F1 score for recommendations.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EvaluationMetrics {
	public static Map<String, Collection<String>> metricsTrainimMap = new HashMap<String, Collection<String>>();
	public static Map<String, Collection<String>> metricsGroundTruthMap = new HashMap<String, Collection<String>>();
	

	public static void main(String args[]) throws IOException
	{
		metricsGroundTruthMap.putAll(ConvertDataintoMatrix.groundTruthMap);
		metricsTrainimMap.putAll(ItemBasedRecommendation.recommendationMap);
	//	System.out.println("EvalgroundTruthMap :"+EvalgroundTruthMap);
		
		BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(new File("C:\\Users\\kaust\\Downloads\\yelp-dataset\\Recommendation\\UserStatsItem.csv")));
		bufferWriter.write("user" + "," + "recall"+ ","+ "Common"+ "," +"precision"+ "," + "MAPE" + "," + "F1 score");
		bufferWriter.write("\n");
		for(String user:metricsTrainimMap.keySet())
		{
			//if(user == "66R-1FGOg7SoJLtkSXVfuQ") {// checked for 1 user for testing
			int hitrate=0;
			int totalbusinessRecall=0;
			int totalbusinessPrecision=0;	
			int listrecommendedbusiness=0;
			int listactualbusiness=0;
			int hits=0;
			if(metricsGroundTruthMap.containsKey(user))
			{
				for(String abusiness:metricsGroundTruthMap.get(user))
				{
					if(metricsTrainimMap.get(user).contains(abusiness))
					{
						//System.out.println("hit");
						hitrate+=1;
						hits+=1; //RELEVANT DOCUMENT
					}
				}
				listrecommendedbusiness=(metricsTrainimMap.get(user)).size();// tOTAL RETRIEVED
				listactualbusiness=(metricsGroundTruthMap.get(user)).size();// TOTAL RELEVANT
				totalbusinessRecall=listactualbusiness;
				totalbusinessPrecision=listrecommendedbusiness;
			}
			float recall = (float)hitrate/totalbusinessRecall;
			float precision = (float)hitrate/totalbusinessPrecision;
			float f1 = 2*((precision*recall)/(precision+recall));
			System.out.println("Recall" + recall );
//			System.out.println("Precision" + precision );
			String stats = user + "," + recall+ ","+ hitrate+ ","+ precision +"," + "," + f1 ;
			bufferWriter.write(stats);
			bufferWriter.write("\n");
		}
		bufferWriter.close();
	}
}
