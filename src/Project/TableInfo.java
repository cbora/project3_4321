package Project;

import java.util.ArrayList;

import Indexing.IndexInfo;

/**
 * Container to hold info about tables
 * @author Richard Henwood (rbh228)
 * @author Chris Bora (cdb239)
 * @author Han Wen Chen (hc844)
 *
 */
public class TableInfo {
	
	/* ================================== 
	 * Fields
	 * ================================== */
	private String filePath; // path to file containing table
	private String tableName; // name of table
	private IndexInfo indexInfo;
	private ArrayList<String> columns; // name of the columns in order
	
	/* ================================== 
	 * Constructors
	 * ================================== */
	/**
	 * Constructor
	 * @param filePath - path to table's data file
	 * @param tableName - name of table
	 */
	public TableInfo(String filePath, String tableName) {
		this.filePath = filePath;
		this.tableName = tableName;
		this.columns = new ArrayList<String>();
		this.indexInfo = null;
	}

	/* ================================== 
	 * Methods
	 * ================================== */
	/**
	 * getter for filePath
	 * @return this.filePath
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * setter for filePath
	 * @param filePath
	 */
	public void setFilePath(String fileName) {
		this.filePath = fileName;
	}
	
	/**
	 * getter for tableName
	 * @return tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * setter for tableName
	 * @param tableName
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * getter for list of columns
	 * @return ArrayList of columns in table
	 */
	public ArrayList<String> getColumns() {
		return columns;
	}	
	
	public void setIndexInfo(IndexInfo indexInfo) {
		this.indexInfo = indexInfo;
	}
	
	public IndexInfo getIndexInfo() {
		return this.indexInfo;
	}
	
	public String getIndexAttribute() {
		if (this.indexInfo == null)
			return null;
		return this.indexInfo.getAttribute();
	}
	
	public String getIndexPath() {
		if (this.indexInfo == null)
			return null;
		return this.indexInfo.getIndexPath();
	}
	
	
	public boolean isClustered() {
		if (this.indexInfo == null)
			return false;
		return this.indexInfo.isClustered();
	}
}






