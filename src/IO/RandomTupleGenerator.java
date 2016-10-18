package IO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import Project.Tuple;
import Project.TupleComparator;

/**
 * Generates a file of random tuples
 * 
 * @author Richard Henwood (rbh228)
 * @author Chris Bora (cdb239)
 * @author Han Wen Chen (hc844)
 *
 */

public class RandomTupleGenerator {
	
	/*
	 * ================================== 
	 * Methods
	 * ==================================
	 */
	/**
	 * generates file of tuples
	 * @param output - name of output file
	 * @param numTuples - number tuples to generate
	 * @param numCols - number of columns to give each tuple
	 */
	public static void genTuples(String output, int numTuples, int numCols) {
		BinaryTupleWriter writer = new BinaryTupleWriter(output);
		Random rand = new Random();
		
		for (int i = 0; i < numTuples; i++) {
			Tuple t = new Tuple(numCols);
			t.add(i, 0);
			for (int j = 1; j < numCols; j++) {
				t.add(rand.nextInt(1000), j);
			}
			writer.write(t);
		}
		
		writer.finalize();
		writer.close();
	}
	
	/**
	 * generates sorted file of tuples with leftmost columns given priority of rightmost columns
	 * @param output - name of output file
	 * @param numTuples - number tuples to generate
	 * @param numCols - number of columns to give each tuple
	 */
	public static void genSortedTuples(String output, int numTuples, int numCols) {
		ArrayList<Tuple> tuples = new ArrayList<Tuple>();
		Random rand = new Random();
		
		for (int i = 0; i < numTuples; i++) {
			Tuple t = new Tuple(numCols);
			t.add(i, 0);
			for (int j = 1; j < numCols; j++) {
				t.add(rand.nextInt(1000), j);
			}
			tuples.add(t);
		}
		
		int order[] = new int[numCols];
		for (int i = 0; i < numCols; i++) 
			order[i] = i;
		
		Collections.sort(tuples, new TupleComparator(order)); 
		
		BinaryTupleWriter writer = new BinaryTupleWriter(output);
		for (int i = 0; i < tuples.size(); i++) {
			writer.write(tuples.get(i));
		}
		writer.finalize();
		writer.close();
	}
}
