package Operators;

import java.util.ArrayList;
import java.util.HashMap;

import IO.BinaryTupleReader;
import Indexing.BPlusTree;
import Indexing.LeafNode;
import Project.ColumnInfo;
import Project.TableInfo;
import Project.Tuple;
import net.sf.jsqlparser.schema.Table;

/**
 * Index Scan Operator abstract class
 * 
 * @author Richard Henwood (rbh228)
 * @author Chris Bora (cdb239)
 * @author Han Wen Chen (hc844)
 *
 */
public abstract class IndexScanOperator extends Operator {

	/* =====================================
	 * Fields
	 * ===================================== */
	protected int lowkey; // lowkey of range we care about
	protected int highkey; // highkey of range we care about
	protected BPlusTree bTree; // b+ tree index
	protected LeafNode currLeaf; // leaf node we are on right now
	
	protected HashMap<String, Integer> schema; // schema of the tuples we are reading
	protected TableInfo tableInfo; // info about table we are reading from
	protected String tableID; // id we are using for table
	protected BinaryTupleReader reader; // reads from table file

	/* =====================================
	 * Constructors
	 * ===================================== */
	/**
	 * Constructor
	 * @param tableInfo - info about table we are reading from
	 * @param tableID - id of table we are reading from
	 * @param lowkey - lowkey of range we want
	 * @param highkey - highkey of range we want
	 */
	public IndexScanOperator(TableInfo tableInfo, String tableID, int lowkey, int highkey) {
		super();
		
		this.tableInfo = tableInfo;
		this.tableID = tableID;

		ArrayList<ColumnInfo> columns = tableInfo.getColumns();
		this.schema = new HashMap<String, Integer>();
		
		this.reader = new BinaryTupleReader(this.tableInfo.getFilePath());
		
		// Read from columns in tableInfo
		// Add (<alias> + "." + <name of column i>, i) to hash map
		for (int i = 0; i < columns.size(); i++) {
			this.schema.put(this.tableID + "." + columns.get(i).column, i);
		}
		
		String indexFile = tableInfo.getIndexPath();
		this.bTree = new BPlusTree(indexFile);
		this.lowkey = lowkey;
		this.highkey = highkey;	
		this.currLeaf = this.bTree.search(lowkey);

	}
	
	/**
	 * Constructor
	 * @param tableInfo - info about table we are reading from
	 * @param tableID - id of table we are reading from
	 * @param lowkey - lowkey of range we want
	 * @param highkey - highkey of range we want
	 */
	public IndexScanOperator(TableInfo tableInfo, int lowkey, int highkey) {
		this(tableInfo, tableInfo.getTableName(), lowkey, highkey);
	}
	
	/**
	 * Constructor
	 * @param tableInfo - info about table we are reading from
	 * @param tableID - id of table we are reading from
	 * @param lowkey - lowkey of range we want
	 * @param highkey - highkey of range we want
	 */
	public IndexScanOperator(TableInfo tableInfo, Table tbl, int lowkey, int highkey) {
		this(tableInfo, tbl.getAlias() == null ? tbl.getName() : tbl.getAlias(), lowkey, highkey);
	}
	
	/* ===============================================
	 * Methods
	 * =============================================== */
	@Override
	public HashMap<String, Integer> getSchema() {
		return schema;
	}
	
	@Override
	public abstract Tuple getNextTuple();

	@Override
	public abstract void reset();

	@Override
	public abstract void reset(int index);

	@Override
	public void close() {
		reader.close();
		bTree.close();
	}
}
