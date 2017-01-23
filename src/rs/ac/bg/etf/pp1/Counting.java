package rs.ac.bg.etf.pp1;

import java.util.HashMap;

public class Counting {

	private static String[] KeyValues = {
			"globalVars", "mainLocalVars", "globalConsts", "globalVarArrays", "globalMethods",
			"classGlobalMethods", "classStaticMethods", "statementBlocks", "mainInvokeCalls",
			"methodsFormPars", "classes", "classMethods", "classFields"};
	
	public static MJParser Parser;
	public static HashMap<String, Integer> counting = new HashMap<String, Integer>();  
	
	static {	
		for (String keyValue : KeyValues) {
			counting.put(keyValue, 0);
		}			
	}
			
	public static void found(String key) {
		int value = counting.get(key);		
		counting.put(key, ++value);
	}
	
	public static void print() {
		Parser.report_info("----------------------------", null);
		Parser.report_info("> Symbol Count", null);
		Parser.report_info("----------------------------", null);
		for (String keyValue : KeyValues) {			
			Parser.report_info(keyValue + " = " + counting.get(keyValue), null);
		}
		Parser.report_info("----------------------------", null);
	}	
	
}
