package task1ir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Evaluation {
	
	// Evaluate precision,relevance 
	
	public static void main(String[] args) throws IOException {
		Path csvFile = Paths.get("./task1Input/IR.csv");
		String line = "";
		
		HashMap<String, List<String>> userBusinessMap = new HashMap<String, List<String>>();
		HashMap<String, List<String>> groundTruthMap = new HashMap<String, List<String>>();

		try (BufferedReader br = new BufferedReader(new FileReader(csvFile.toString()))) {
			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] mapping = line.split(",");
				List<String> res = userBusinessMap.get(mapping[0]);
				if (res == null) {
					res = new ArrayList<String>();
					res.add(mapping[1]);
					userBusinessMap.put(mapping[0], res);
				} else {
					userBusinessMap.get(mapping[0]).add(mapping[1]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		csvFile = Paths.get("./task1Input/ground_truth.csv");
		try (BufferedReader br = new BufferedReader(new FileReader(csvFile.toString()))) {
			while ((line = br.readLine()) != null) {
				
				String[] mapping = line.split(",");
				if (userBusinessMap.containsKey(mapping[0])) {
					List<String> res = groundTruthMap.get(mapping[0]);
					if (res == null) {
						res = new ArrayList<String>();
						res.add(mapping[1]);
						groundTruthMap.put(mapping[0], res);
					} else {
						groundTruthMap.get(mapping[0]).add(mapping[1]);
					}
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		int commonRestaurant = 0;
		int totalRestaurant = 0;
		int retrievedDocument = 0;
		int relevantDocument = 0;
		int totalRelevant = 0;

		File f = new File("./task1Output/output.csv");
		f.delete();
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		bw.write("users,precision,recall,mape,commonResto\n");
		System.out.println(userBusinessMap.size());
		for (String user : userBusinessMap.keySet()) {
			int n = 0;
			double sum = 0.0;
			relevantDocument = 0;
			totalRelevant = 0;
			retrievedDocument = 0;
			commonRestaurant = 0;
			if (groundTruthMap.get(user) != null) {
				retrievedDocument = userBusinessMap.get(user).size();
				for (String business_id : userBusinessMap.get(user)) {
					if (groundTruthMap.get(user).contains(business_id)) {
						commonRestaurant += 1;
						relevantDocument += 1;
					}

				}
				totalRestaurant = groundTruthMap.get(user).size();
				totalRelevant = groundTruthMap.get(user).size();
				sum += ((double) (totalRelevant - relevantDocument) / totalRelevant);
				n++;
			}

			double Precision = 0.0;
			if (retrievedDocument != 0) {
				double res = (double) ((double) commonRestaurant / retrievedDocument);
				Precision = res;
			}
			double Recall = 0.0;
			if (totalRelevant != 0) {

				Recall = (double) ((double) commonRestaurant / totalRestaurant);
			}

			double res = sum / n;

			bw.write(user + "," + Precision + "," + Recall + "," + res + "," + commonRestaurant + "\n");

		}
		bw.close();
	}
}
