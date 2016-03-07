import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import org.w3c.dom.Attr;

import java.lang.Math;

public class AvciC45 {

	public static int correct=0; //class counts - e
	public static int incorrect=0; //class counts - p
	public static ArrayList<Attribute> attributes = new ArrayList<Attribute>(); //holding the attr for root
	public static ArrayList<String[]> records = new ArrayList<String[]>(); //holding the records for root
	public static String posClassTitle = "e"; //easy to adjust to other sets
	public static String negClassTitle = "p";
	
	private static String trainingFile;
	private static String testFile;
	private static String outputFile;
	
	
	/**
	 * 
	 * @param args: name of the training data set file, the name of the test dataset file, the name of the output file
	 * @throws IOException
	 * USAGE:
	 * javac AvciC45.java
	 * java AvciC45 trainingFile testFile outputFile
	 * Prints the labels and test records to the output file as well as the accuracy
	 * Prints to the STDOUT the visualization and counts in each branch
	 */
	public static void main(String[] args) throws IOException {
		
		
		trainingFile = args[0]; testFile = args[1]; outputFile = args[2]; //configuration
		createAllAttributes(); //create as many Attribute objects as there are objects
		loadData(); //read the data set and store them to the root
		
		
		final PrintStream oldStdout = System.out;
		
		Node root = new Node("root"); //create the tree
		root.recordList = records;
		root.attachedAttr = null;
		
		splitTree(root); //split the tree
		treeVisualize(root); //show me where you split the tree and how many records are stored in each branch

		System.setOut(new PrintStream(new FileOutputStream(outputFile)));
		testTree(root); //test the tree
		
		System.out.println("Accuracy is: %"+ 100*(double)correct/(double)(incorrect+correct));
		System.setOut(oldStdout);
		System.out.println("Accuracy is: %"+ 100*(double)correct/(double)(incorrect+correct));
	  	
		}

	/**
	 * 
	 * @param root
	 * Visualizes tree in each split attribute and gives the count of records
	 * stored in each branch
	 */

	private static void treeVisualize(Node root) {
		LinkedList<Node> children = new LinkedList<Node>();
		
		children.add(root);
		while(!children.isEmpty()) {
			Node a = children.poll();
			if(a.attachedAttr!=null) {
				System.out.println("Splitting attribute: " +a.attachedAttr.index);
				System.out.println("Attribute: "+ a.name+" Edible:(" + a.attachedAttr.attrList.get(a.name).positive + ") Poison:(" + a.attachedAttr.attrList.get(a.name).negative+")");
				System.out.println();
			}
			
			if(a.children.size()!=0)
				children.addAll(a.children);
		}
		
		
	}

	/**
	 * 
	 * @param root
	 * @throws IOException
	 * Loads the test dataset
	 */

	private static void testTree(Node root) throws IOException {
		ArrayList<String[]> testRecords = new ArrayList<String[]>();
		Scanner test = new Scanner(new File(testFile));
		while(test.hasNextLine()) {
			String headerLine = test.nextLine();
			String testRecord[]  = headerLine.split("	");
			testRecords.add(testRecord);
			
			
			
		}
		root.testList.addAll(testRecords);
//		System.out.println("testing..");
		classify(root);

		test.close();
		
	}

	/**
	 * 
	 * @param root
	 * Classifies the test data set given the rules trained in the previous data set
	 * Recursively calls itself until it reaches a leaf node
	 * Prints out each record as well as its classified label
	 * Calculates the accuracy
	 */
	private static void classify(Node root) {

		
		if(root.children.size()==0) {
			
			for(String[] rec: root.testList) {
//				System.out.println(root.testList.size());
				if(root.rule!=null) {
					if(rec[0].equals(root.rule)) {
						System.out.println(Arrays.toString(rec) + " Correct classification label: "+root.rule);
						correct++;
					}
					else {
						System.out.println(Arrays.toString(rec) + " Incorrect classification label: "+root.rule);
						incorrect++;
					}
				}
				else {
					incorrect++;
					System.out.println(Arrays.toString(rec) + " Classification label: Impure class");
				}
				
				
				
					
			}
			return;
		}
		

			for(String[] rec: root.testList) {
				for(Node element : root.children) {
					if(rec[element.attachedAttr.index].equals(element.name)) {
						element.testList.add(rec);
					}
				
				}
				
			}
		
		for(Node element : root.children)
			classify(element);
			
		
	}
	/**
	 * 
	 * @param root
	 * Recursively splits the tree by finding the best attribute in each step
	 * Stops when it generates a pure class or runs out of attributes to split
	 */

	private static void splitTree(Node root) {
	
		boolean flag=false;
		for(Attribute attr : attributes) {
			if(attr.used==false)
				flag = true;
		}
		if(!flag)
			return;
		
		
		int p=0; //local counters 
		int e=0;
		
		if(!root.name.equals("root")) {
			for(Attribute attr:attributes) {
				if(!attr.used)
					attr.update();
			}
		}
		
		for(String[] rec : root.recordList) {
			if(!root.name.equals("root")) {
				
					for(int i=1; i<rec.length; i++) {
						if(rec[0].equals(posClassTitle)) {
							if(attributes.get(i-1).used==false)
								attributes.get(i-1).addPositive(rec[i]);
							}
						else {
							if(attributes.get(i-1).used==false)
								attributes.get(i-1).addNegative(rec[i]);
							}
						}
							
					}
			
			if(rec[0].equals(posClassTitle)) {
				e++;
			}
			else
				p++;
		}
		
		//if either of the conditions fulfill set the rule
		
		if(p==0) {
			root.rule = posClassTitle;
			return;
		}
		
		if(e==0) {
			root.rule = negClassTitle;
			return;
		}
			
		Attribute bestAttribute = selectAttribute(e, p, root.recordList); //find the best attribute

		if(bestAttribute==null)
			return;
		else
			bestAttribute.used=true;
		
//		System.out.println("best index" +bestAttribute.index);
		
		for(String key : bestAttribute.attrList.keySet()) { //create as many children as there are options in that attr
			Node child = new Node(key);
			child.attachedAttr = bestAttribute;
			root.children.add(child);
		}
			
	
		for(String[] rec : root.recordList) {
			for(Node child : root.children) {
				if(rec[bestAttribute.index].equals(child.name)) {
					child.recordList.add(rec);
					
				}
			}
		}
		
		for(Node child : root.children) {
			splitTree(child);
		}
				
	}

	
	//info gain

	private static double info(double e, double p) {
		return (-1 * e / (e+p)) * (Math.log(e / (e+p)) / Math.log(2))  + (-1 * p / (e+p)) * (Math.log(p / (e+p)) / Math.log(2));
		
	}


	/**
	 * 
	 * @param e
	 * @param p
	 * @param recordList
	 * @return best Attribute found
	 * goes through all attributes not used to find the best attr that gives most information gain
	 */
	private static Attribute selectAttribute(int e, int p, ArrayList<String[]> recordList) {
		
		double info = info(e, p);
		double max = -Double.MIN_VALUE;
		
		Attribute bestAttribute = null;

		for(Attribute attr : attributes) {
				
			double attrInfo = 0.0; //info gain given an attribute
			
			double splitInfo = 0.0; // split info
			
			
			if(attr.used)//skip used attrs
				continue;
			
			
			
			for(String key : attr.attrList.keySet()) {
				
				double a = attr.attrList.get(key).positive;
				double b = attr.attrList.get(key).negative;
				if(b==0) b++; //avoid division 0
				if(a==0) a++;
				
				attrInfo += (a/(double)recordList.size()) * info(a,b);

				splitInfo += ((-a/(a+b)) *(Math.log(a/(a+b))     /      Math.log(2)   )    );
				
			}
			

			
			
			double gain =(info-attrInfo);
			double gainRatio = gain/splitInfo;
		
			if( gainRatio >= max) {
				max = gainRatio;
				bestAttribute = attr; //best attr so far
			}
			
			
		}
		return bestAttribute;
	}


/**
 * Loads the data
 * @throws IOException
 */

	private static void loadData() throws IOException {
		
		Scanner scan = new Scanner(new File(trainingFile));
		while(scan.hasNextLine()) {
			String headerLine = scan.nextLine();
			String record[]  = headerLine.split("	");
			
				for(int i=1; i<record.length; i++) {
					if(record[0].equals(posClassTitle)) 
						attributes.get(i-1).addPositive(record[i]);
					
					else
						attributes.get(i-1).addNegative(record[i]);
				}
				
			records.add(record);
		}
		scan.close();
	}

	/**
	 * Creates all the attributes
	 * @throws IOException
	 */
	private static void createAllAttributes() throws IOException {

		Scanner scanAt = new Scanner(new File(trainingFile));
		
		String attr = scanAt.nextLine();
		String attrList[] = attr.split("	");
		
		for(int i=1; i<attrList.length; i++) {
			Attribute attribute = new Attribute(i);
			attributes.add(attribute);
		}
		
		scanAt.close();
		
	}

}
