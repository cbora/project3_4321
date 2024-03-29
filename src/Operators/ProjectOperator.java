package Operators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import Project.EvalSelectItemVisitor;
import Project.Tuple;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * Operator for projecting columns from a tuple
 * @author Richard Henwood (rbh228)
 * @author Chris Bora (cdb239)
 * @author Han Wen Chen (hc844)
 *
 */
public class ProjectOperator extends Operator {

	/* ================================== 
	 * Fields
	 * ================================== */
	private Operator child; // child operator in operator tree
	private ArrayList<SelectItem> items; // columns we want to retain
	private HashMap<String, Integer> schema; // schema
	private ArrayList<Table> tables;

	/* ================================== 
	 * Constructors
	 * ================================== */
	/**
	 * Constructor
	 * @param child
	 * @param items - ArrayList of columns we want to project
	 */
	public ProjectOperator(Operator child, ArrayList<SelectItem> items, ArrayList<Table> tables) {
		this.child = child;
		this.items = items;
		this.tables = tables;
		EvalSelectItemVisitor visitor = new EvalSelectItemVisitor(this.items, this.tables, child.getSchema());
		this.schema = visitor.getResult(); 
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
		sb.append("Project");
		sb.append(this.items);
		sb.append("\n");
		sb.append(this.child.prettyPrint(depth+1));
		return sb.toString();
	}
	
	

	@Override
	public HashMap<String, Integer> getSchema() {
		return this.schema;
	}

	@Override
	public Tuple getNextTuple() {
		Tuple t = child.getNextTuple();
		if (t == null)
			return null;
		return project(t);
	}

	@Override
	public void reset() {
		child.reset();
	}
	
	@Override
	public void reset(int index){}
	
	@Override
	public void close() {
		child.close();
	}
	
//	public int getRelationSize() {
//		return this.child.getRelationSize();
//	}
	
	/**
	 * Projects selected fields from tuple that has child's schema
	 * @param input - tuple we want to project
	 * @return projected tuple
	 */
	private Tuple project(Tuple input) {
		Tuple t = new Tuple(this.schema.size());
		for(Entry<String, Integer> entry : this.schema.entrySet()){
			int el = input.getVal(this.child.getSchema().get(entry.getKey()));
			t.add(el, entry.getValue());
		}
		return t;		
	}

}
