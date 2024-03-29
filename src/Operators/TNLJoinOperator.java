package Operators;

import Project.Tuple;
import net.sf.jsqlparser.expression.Expression;

/**
 * Join Operator that uses TNLJ
 * @author Richard Henwood (rbh228)
 * @author Chris Bora (cdb239)
 * @author Han Wen Chen (hc844)
 *
 */
public class TNLJoinOperator extends JoinOperator {
	
	/* ================================== 
	 * Fields
	 * ================================== */
	private Tuple lastLeft; // left tuple we have seen from leftChild
	
	/* ================================== 
	 * Constructors
	 * ================================== */
	/**
	 * Constructor
	 * @param leftChild
	 * @param rightChild
	 * @param exp - expression for the condition we are joining on
	 */
	public TNLJoinOperator(Operator leftChild, Operator rightChild, Expression exp) {
		super(leftChild, rightChild, exp);
		this.lastLeft = this.leftChild.getNextTuple();
	}
	
	/**
	 * Cartesian product constructor - set join condition to null
	 * @param leftChild
	 * @param rightChild
	 */
	public TNLJoinOperator(Operator leftChild, Operator rightChild) {
		this(leftChild, rightChild, null);
	}
	
	/**
	 * pretty print method
	 * @param depth
	 * @return this method's name
	 */
	public String prettyPrint(int depth){
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<depth; i++)
			sb.append("-");
		sb.append("TNLJ");
		sb.append("[");
		sb.append(this.exp);
		sb.append("]\n");
		sb.append(this.leftChild.prettyPrint(depth+1));
		sb.append(this.rightChild.prettyPrint(depth+1));
		return sb.toString();	
	}
	
	/* ================================== 
	 * Methods
	 * ================================== */

	@Override
	public Tuple getNextTuple() {
		do {
			Tuple right = this.rightChild.getNextTuple();
			if (lastLeft == null)
				return null;
			// if rightChild returns null, we should reset it and increment left
			if (right == null) { 
				this.rightChild.reset();
				lastLeft = this.leftChild.getNextTuple();
				right = this.rightChild.getNextTuple();
				// if left null, we've checked all tuples. if right still null, right is empty relation
				if (lastLeft == null || right == null)
					return null;
			}
			Tuple result = Tuple.concat(lastLeft, right);
			if (passesCondition(result))
				return result;
		} while (true);
	}

	@Override
	public void reset() {
		this.leftChild.reset();
		this.lastLeft = leftChild.getNextTuple();
		this.rightChild.reset();
	}
	
	@Override
	public void reset(int index){}

}
