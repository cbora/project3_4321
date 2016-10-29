package Operators;

import Project.Tuple;
import Project.TupleComparator;
import Project.TupleDiffComparator;
import net.sf.jsqlparser.expression.Expression;

public class SMJoinOperator extends JoinOperator {

	/* ================================== 
	 * Fields
	 * ================================== */
	private Tuple left; // pointer into the left relation
	private Tuple right; // pointer into the right relation
	private Tuple rightPartition; // pointer to beginning of right relation's current partition
	int index; // index of rightPartition in right relation
	
	/* ================================== 
	 * Constructors
	 * ================================== */
	/**
	 * Constructor
	 * @param leftChild
	 * @param rightChild
	 * @param exp - expression for the condition we are joining on
	 */
	public SMJoinOperator(Operator leftChild, Operator rightChild, Expression exp) {
		super(leftChild, rightChild, exp);
		this.left = leftChild.getNextTuple();
		this.right = rightChild.getNextTuple();
		this.rightPartition = right;
		this.index = 0;
	}

	/* ================================== 
	 * Methods
	 * ================================== */
	@Override
	public Tuple getNextTuple() {
		// reached the end, so return null
		if ((this.left == null || this.rightPartition == null)) {
			return null;
		}
		
		// comparators for comparing left and right tuples and right and right tuples
		TupleDiffComparator tc = new TupleDiffComparator(((SortOperator) leftChild).sort_order, ((SortOperator) rightChild).sort_order) ;
		TupleComparator tc2 = new TupleComparator(((SortOperator) rightChild).sort_order);
		
		// loop until left and rightPartition are equal
		while (tc.compare(left,  rightPartition) != 0) {
			// while left < rightPartition, increment left
			while (tc.compare(left, rightPartition) == -1) {
				left = leftChild.getNextTuple();
				// reached end of left, return null
				if (left == null) {
					return null;
				}
			}
			
			// while rightPartition > left, increment right & rightPartition
			while(tc.compare(left, rightPartition) == 1) {
				rightPartition = rightChild.getNextTuple();
				right = rightPartition;
				index++;
				// reached end of right relation, return null
				if (rightPartition == null)  {
					return null;
				}
			}
		}
				
		// at this point left and right are equal, concatenate them and increment right
		Tuple result = Tuple.concat(left, right);
		right = rightChild.getNextTuple();
		
		//if right is now null or we have left current partition, reset right to rightPartition and increment left
		if (right == null || tc2.compare(rightPartition, right) != 0) {
			left = leftChild.getNextTuple();
			((SortOperator) rightChild).reset(index);
			right = rightChild.getNextTuple();
		}
		return result;
	}

	@Override
	public void reset() {
		this.leftChild.reset();
		this.left = leftChild.getNextTuple();
		
		this.rightChild.reset();
		this.right = rightChild.getNextTuple();
		
		this.rightPartition = right;
		this.index = 0;
	}

}
