package task1colaborative;
/*
This class is used to convert String User and Long Business ID's into a long Datatype.
which acts an input to the data model
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ConvertDataintoMatrix {
	public static HashMap<String,Long> usersMap = new HashMap<String,Long>();
	public static HashMap<String,Long> businessMap = new HashMap<String,Long>();
	public static Map<String, Collection<String>> useritemsMap = new HashMap<String, Collection<String>>();
	public static HashMap<String,Long> newuserGroundMap=new HashMap<String,Long>();
	public static Map<String, Collection<String>> groundTruthMap = new HashMap<String, Collection<String>>();

	//busineessMap= {business_id = longbussid} --> Training Data is in this form
	//userMap = {user_id = longuserid} --> Training Data
	public static void main(String args[]) throws IOException
	{
		BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\Users\\kaust\\Downloads\\yelp-dataset\\Training&GroundTruth\\train_csv.csv"));
		
		BufferedReader readUsers = new BufferedReader(new FileReader("C:\\Users\\kaust\\Downloads\\yelp-dataset\\Training&GroundTruth\\ground_truth.csv"));
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("C:\\Users\\kaust\\Downloads\\yelp-dataset\\Training&GroundTruth\\userbusinesscode.csv"));
		String line;
		Long longuserid=(long) 1;
		Long longbussid=(long) 1000000;
		while((line = bufferedReader.readLine()) != null) 
		{
			String[] data = line.split(",", -1);

			if(businessMap.containsKey(data[1]) && !usersMap.containsKey(data[0])) 
			{
				usersMap.put( data[0], longuserid);
				bufferedWriter.write(longuserid+","+businessMap.get(data[1])+","+data[2]+"\n");
				longuserid++;
			}
			//To check if the user id  is in the hashmap or not
			else if(!businessMap.containsKey(data[1]) && usersMap.containsKey(data[0]))
			{
				businessMap.put(data[1], longbussid);
				bufferedWriter.write(usersMap.get(data[0])+","+longbussid+","+data[2]+"\n");
				longbussid++;
			}
			//checks if both user Id and business ID's are there in map or not
			else if(!businessMap.containsKey(data[1]) && !usersMap.containsKey(data[0]))
			{
				businessMap.put(data[1], longbussid);
				usersMap.put( data[0], longuserid);
				bufferedWriter.write(longuserid+","+longbussid+","+data[2]+"\n");
				longuserid++;
				longbussid++;
			}
			else if(businessMap.containsKey(data[1]) && usersMap.containsKey(data[0]))
			{
				bufferedWriter.write(usersMap.get(data[0])+","+businessMap.get(data[1])+","+data[2]+"\n");
			}
			
			//Mappping the user ID's and Business ID's
			String bussiness_id=data[1];
			String user_id=data[0];
			Collection<String> bussids=useritemsMap.get(user_id);
			if (bussids==null)
			{
				bussids=new ArrayList<String>();
				useritemsMap.put(user_id,bussids);
			}
			bussids.add(bussiness_id);
			
		}
	//	System.out.println("BusinessMap:" + businessMap);
	//	System.out.println("UserMap:" + userMap);
		bufferedReader.close();
		bufferedWriter.close();	
		
		//User:{Business1,Business2,}
		//groundTruthMap ={user_id, buss_id}
		while((line = readUsers.readLine()) != null) 
		{
			String[] values = line.split(",", -1);
			//groundTruthMap.put(values[0], values[1]);
			Collection<String> bussids=groundTruthMap.get(values[0]);
			if (bussids==null)
			{
				bussids=new ArrayList<String>();
				groundTruthMap.put(values[0],bussids);
			}
			bussids.add(values[1]);
		}
		readUsers.close();
		newuserGroundMap.putAll(usersMap);
		for(String a:usersMap.keySet())
		{
			if(!groundTruthMap.containsKey(a))// if groundtruth does not contain that user_id, then remove it from newUserMap
			{
				newuserGroundMap.remove(a);//newuserMap will contain only user_id's that are there in groundTruth
			}
		}
		System.out.println("Converting the Data is finished");
	}

}
