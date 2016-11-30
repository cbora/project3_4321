package Project;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import IO.BinaryTupleWriter;
import IO.Configuration;
import Indexing.BPlusTree;
import Indexing.IndexInfo;
import LogicalOperator.LogicalOperator;
import LogicalOperator.LogicalPlanBuilder;
import Operators.InMemSortOperator;
import Operators.Operator;
import Operators.PhysicalPlanBuilder;
import Operators.ScanOperator;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

/**
 * Entry point to application
 * @author Richard Henwood (rbh228)
 * @author Chris Bora (cdb239)
 * @author Han Wen Chen (hc844)
 *
 */
public class Main {

	public static void main(String[] args) {
		
		// parse config file
		Configuration config = new Configuration(args[0]);
		
		// Build DbCatalog
		DbCatalog dbC = DbCatalog.getInstance();
		try {
			BufferedReader br = new BufferedReader(new FileReader(config.getInputDir() + "/db/schema.txt"));

			String read = br.readLine();
		   
		    while (read  != null) {
		    	String[] line  = read.split(" "); 
		    	TableInfo t = new TableInfo(config.getInputDir() + "/db/data/" + line[0], line[0]);
		    	for (int i=1; i < line.length; i++){
		    		t.getColumns().add(new ColumnInfo(line[i])); 
		    	}
		    	dbC.addTable(t.getTableName(), t);
        		read = br.readLine();
    		}
		    
		    br.close();
		    
		 } catch (Exception e) {
    			System.err.println("Exception occurred during schema reading");
    			e.printStackTrace();    			
		 }
		
		// associate indexes with tables
		String indexInfo = config.getInputDir() + "/db/index_info.txt";
		parseIndexConfig(indexInfo, config.getInputDir());
		
		// build stats.txt
		buildStats(config.getInputDir());
		
		// determine which build action to take
		if (config.runOption() == 1) {
			buildIndexes();
		}
		else if(config.runOption() == 2) {
			buildIndexes();
			runQueries(config.getInputDir(), config.getOutputDir(), config.getTmpDir());
		}
		else if(config.runOption() == 3) {
			runQueries(config.getInputDir(), config.getOutputDir(), config.getTmpDir());
		}
				
	}
	
	/**
	 * builds indexes 
	 */
	public static void buildIndexes() {
		DbCatalog dbC = DbCatalog.getInstance();
		ArrayList<String> tables = dbC.getTableNames();
		
		for (String table : tables){
			ScanOperator scan;
			IndexInfo info = dbC.get(table).getIndexInfo();
			
			if (info == null) // if index info is null, no index for this table. just continue
				continue;
			
			int pos;
			if (info.isClustered()){ // if it is clustered, sort and overwrite data
				scan = new ScanOperator(dbC.get(table), table);
				pos = scan.getSchema().get(table + "." + info.getAttribute());
				
				int sort_order[] = {pos};
				InMemSortOperator sort = new InMemSortOperator(scan, sort_order );
				
				BinaryTupleWriter writer = new BinaryTupleWriter(dbC.get(table).getFilePath());
				sort.dump(writer);
				writer.close();
				sort.close();
				
				scan = new ScanOperator(dbC.get(table), table);
			}				
			else {
				scan = new ScanOperator(dbC.get(table), table);
				pos = scan.getSchema().get(table + "." + info.getAttribute());
			}
			
			// build b+ tree index
			BPlusTree bplus= new BPlusTree(info.getD(), scan, pos, info.getIndexPath());
			bplus.close();
			scan.close();
		}
		
	}
	
	/**
	 * associates table in the catalog with appropriate index information
	 * @param configfile - path to index config file
	 * @param input - path to input directory
	 */
	public static void parseIndexConfig(String configfile, String input) {
		DbCatalog dbC = DbCatalog.getInstance();
		FileReader fileReader;
		BufferedReader bufferedReader;
		
		try {
			fileReader = new FileReader(configfile);
			bufferedReader = new BufferedReader(fileReader);
			
			String line;
			try { 
				while((line = bufferedReader.readLine()) != null) {
					String[] parts = line.split(" ");
					String indexPath = input + "/db/indexes/" + parts[0] + "." + parts[1];
					IndexInfo i = new IndexInfo(indexPath, parts[1], parts[2], parts[3]);
					dbC.get(parts[0]).setIndexInfo(i);
				}
				
			}catch (IOException ex) {
				System.out.println("error reading config file " + configfile);
			}
		}catch(FileNotFoundException ex) {
			System.out.println("Unable to open file " + configfile);
		}
	}
	
	public static void buildStats(String input) {
		DbCatalog dbC = DbCatalog.getInstance();
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(input + "/db/stats.txt", "UTF-8");
		} catch (Exception e) {
			System.out.println("error in stats");
		}
		
		for (String table : dbC.getTableNames()) {
			TableInfo tableInfo = dbC.get(table);
			ScanOperator scan = new ScanOperator(tableInfo, table);
			HashMap<String, Integer> schema = scan.getSchema();
			//System.out.println(schema);
			
			Tuple t;
			while ((t = scan.getNextTuple()) != null) {
				tableInfo.setNumTuples(tableInfo.getNumTuples() + 1);
				
				for (ColumnInfo col : tableInfo.getColumns()) {
					//System.out.println(t.getVal(0));
					if (t.getVal(schema.get(table + "." + col.column)) > col.max) {
						col.max = t.getVal(schema.get(table + "." + col.column));
					}
					if (t.getVal(schema.get(table + "." + col.column)) < col.min) {
						col.min = t.getVal(schema.get(table + "." + col.column));
					}
				}
			}
			
			try {
				writer.print(table + " " + tableInfo.getNumTuples() + " ");
				for (ColumnInfo col : tableInfo.getColumns()) {
					writer.print(col.column + "," + col.min + "," + col.max + " ");
				}
				writer.println();
			} catch (Exception e) {
				System.out.println("error writing to stats");
			}
		}
		
		writer.close();
	}

	/**
	 * runs queries
	 * @param input - path to input directory
	 * @param output - path to output directory
	 * @param tmp - path to temp directory
	 */
	public static void runQueries(String input, String output, String tmp) {
		 
		final String inputDir = input;
		final String outputDir = output;
		final String tmpDir = tmp;
		final String queriesFile = "queries.sql";
		
		String planConfig = inputDir + "/plan_builder_config.txt";
		
		// Parse and evaluate queries
		try {
			CCJSqlParser parser = new CCJSqlParser(new FileReader(inputDir + "/" + queriesFile));

			Statement statement;
			int queryNum = 1;
			
			while ((statement = parser.Statement()) != null) {
				try {
					Select select = (Select) statement;
					
					PlainSelect body = (PlainSelect) select.getSelectBody();	
					
					LogicalPlanBuilder d = new LogicalPlanBuilder(body);
					LogicalOperator po = d.getRoot();
					
						
					PhysicalPlanBuilder ppb = new PhysicalPlanBuilder(po, planConfig, tmpDir);
					Operator o = ppb.getResult();
					
					BinaryTupleWriter writ = new BinaryTupleWriter(outputDir + "/query" + queryNum );
					//HumanTupleWriter writ = new HumanTupleWriter(outputDir + "/query" + queryNum);
					
					long start = System.currentTimeMillis();
					o.dump(writ);
					long end = System.currentTimeMillis();

					writ.close();
					o.close();
					queryNum++;
				} catch (Exception e) {
						System.err.println("Exception occurred during processing query " + queryNum);
						e.printStackTrace();
						queryNum++;
				}
				
			} 
		} catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}	
		
		
	}
}
