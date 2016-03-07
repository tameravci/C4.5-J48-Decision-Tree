import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class AvciBayes {

	public static int correct=0;
	public static int incorrect=0;
	public static int p=0;
	public static int e=0;
	public static ArrayList<Attribute> attributes = new ArrayList<Attribute>();
	public static ArrayList<String[]> records = new ArrayList<String[]>();
	public static String posClassTitle = "e";
	public static String negClassTitle = "p";
	public static ArrayList<String[]> testRecords = new ArrayList<String[]>();
	
	private static String trainingFile;
	private static String testFile;
	private static String outputFile;
	
	public static void main(String[] args) throws IOException {
		trainingFile = args[0]; testFile = args[1]; outputFile = args[2]; //configuration
		createAllAttributes();
		loadData();
		final PrintStream oldStdout = System.out;
		System.setOut(new PrintStream(new FileOutputStream(outputFile)));
		Node root = new Node("root");
		root.recordList = records;
		root.attachedAttr = null;
		
		testTree();
		
		
		System.out.println("Accuracy is: "+ (double)correct/(double)(incorrect+correct));
		System.setOut(oldStdout);
		System.out.println("Accuracy is: %"+ 100*(double)correct/(double)(incorrect+correct));
					
		}

	private static void createAllAttributes() throws IOException {

		Scanner scanAt = new Scanner(new File(trainingFile));
		
		String attr = scanAt.nextLine();
		String attrList[] = attr.split("	");
		
		for(int i=0; i<attrList.length; i++) {
			Attribute attribute = new Attribute(i);
			attributes.add(attribute);
		}
		
		scanAt.close();
		
	}
	
	
	private static void loadData() throws IOException {
		
		Scanner scan = new Scanner(new File(trainingFile));
		while(scan.hasNextLine()) {
			String headerLine = scan.nextLine();
			String record[]  = headerLine.split("	");
			
				for(int i=1; i<record.length; i++) {
					if(record[0].equals(posClassTitle))  {
						e++;
						attributes.get(i-1).addPositive(record[i]);
					}
						
					
					else {
						p++;
						attributes.get(i-1).addNegative(record[i]);
					}
						
				}
				
			records.add(record);
		}
		scan.close();
	}
	private static void testTree() throws IOException {
		
		Scanner test = new Scanner(new File(testFile));
		while(test.hasNextLine()) {
			String headerLine = test.nextLine();
			String testRecord[]  = headerLine.split("	");
			testRecords.add(testRecord);
			
			
		}
		
		classify();
	
		test.close();
	
	}

	private static void classify() {
		
		for(String[] record : testRecords) {
			double positive_prob = 1;
			double negative_prob = 1;
			for(int i = 1; i<record.length; i++) {
				
				if(attributes.get(i-1).attrList.get(record[i]).positive==0)
					attributes.get(i-1).attrList.get(record[i]).positive++;
				if(attributes.get(i-1).attrList.get(record[i]).negative==0)
					attributes.get(i-1).attrList.get(record[i]).negative++;
				
				positive_prob *= (double)attributes.get(i-1).attrList.get(record[i]).positive/(double)e;
				negative_prob *= (double)attributes.get(i-1).attrList.get(record[i]).negative/(double)p;
				
			}
			
			positive_prob *= (double)e/(double)(p+e);
			negative_prob *= (double)p/(double)(p+e);
			
			if(positive_prob>=negative_prob && record[0].equals(posClassTitle)) {
				correct++;
				System.out.println(Arrays.toString(record) + "Correct classification label: "+posClassTitle);
			}
			else if(negative_prob>positive_prob && record[0].equals(negClassTitle)) {
				correct++;
				System.out.println(Arrays.toString(record) + "Correct classification label: "+negClassTitle);
			}
			else {
				incorrect++;
				System.out.println(Arrays.toString(record) + "Incorrect classification");
			}
			
		}
		
	}
	
	
}
