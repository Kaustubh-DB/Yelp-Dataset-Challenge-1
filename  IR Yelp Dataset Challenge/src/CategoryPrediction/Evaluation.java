package taskone;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

public class Evaluation {
	public static String categoriesFile = "./InputFiles/categories.txt";
	public static String trainingIndex = "./index/Training_set";
	public static String testIndex = "./index/Testing_set";
	public static String posFile = "./InputFiles/posFile.txt";
	public static String topQueryFile = "./InputFiles/queries.txt";
	public static String outputFile = "./OutputFiles/output.txt";

	public static String groundTruthFile = "./OutputFiles/GroundTruth_final.txt";
	public static HashSet<String> categories = new HashSet<String>();

	public static void main(String[] args) throws IOException, ParseException {
		CatergoryPrediction cp = new CatergoryPrediction();
		GenerateGroundTruth gt = new GenerateGroundTruth();
		ArrayList<String> queries = cp.getQueries(topQueryFile);
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(testIndex)));
		IndexSearcher searcher = new IndexSearcher(reader);
		File file = new File(outputFile);
		file.delete();
		cp.rankDocumentsForQuery(queries, searcher, reader);
		HashMap<String, List<String>> categoryPredictorMap = cp.getuserBusinessMap();
		System.out.println("Category Predictor HashMap: " + categoryPredictorMap);

		categories = gt.readCategoriesFromFile(categoriesFile);
		gt.generateGroundTruth(reader, searcher, categories);
		HashMap<String, List<String>> groundTruthMap = gt.getgroundTruthMap();
		System.out.println("Ground Truth HashMap: " + groundTruthMap);

		int commonRestaurant = 0;
		int totalRestaurant = 0;
		int retrievedDocument = 0;
		int relevantDocument = 0;
		int totalRelevant = 0;
		double sum = 0.0;
		int n = 0;
		for (String category : categoryPredictorMap.keySet()) {
			if (category.equals("Seafood")) {
				relevantDocument = 0;
				totalRelevant = 0;
				if (groundTruthMap.get(category) != null) {
					retrievedDocument = categoryPredictorMap.get(category).size();
					System.out.println(retrievedDocument);
					for (String business_id : categoryPredictorMap.get(category)) {
						if (groundTruthMap.get(category).contains(business_id)) {
							commonRestaurant += 1;
							relevantDocument += 1;
						}

					}
					totalRestaurant = groundTruthMap.get(category).size();
					totalRelevant += groundTruthMap.get(category).size();
					sum += ((double) (totalRelevant - relevantDocument) / totalRelevant);
					n++;
				}
			}

		}
		System.out.println("Common-Restaurant: " + commonRestaurant);
		System.out.println("TotalRestaurant: " + totalRestaurant);
		if (totalRestaurant != 0) {
			double res = (double) ((double) commonRestaurant / totalRestaurant) * 100;

			System.out.println("HitRate: " + res);
		}
		System.out.println("Relevant: " + relevantDocument);
		System.out.println("Retrieved: " + retrievedDocument);
		System.out.println("TotalRelevant: " + totalRelevant);
		if (retrievedDocument != 0) {
			double res = (double) ((double) commonRestaurant / retrievedDocument);

			System.out.println("Precision: " + res);
		}
		if (totalRelevant != 0)
			System.out.println("Recall: " + (double) ((double) commonRestaurant / totalRestaurant));
		double res = sum / n;
		System.out.println("MAPE: " + res * 100);

	}

}
