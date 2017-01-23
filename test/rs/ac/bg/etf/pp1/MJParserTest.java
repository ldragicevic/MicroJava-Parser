package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import java_cup.runtime.Symbol;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;

public class MJParserTest {

	 static {
		 DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		 Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	 }
	
	public static void main(String[] args) throws Exception {
		Logger log = Logger.getLogger(MJParser.class);
		Reader br = null;
		try {
			File sourceCode = new File("test/program.mj");
			log.info("Compiling source file: " + sourceCode.getAbsolutePath());
			br = new BufferedReader(new FileReader(sourceCode));
			Yylex lexer = new Yylex(br);
			MJParser p = new MJParser(lexer);
			
			//Symbol s = 
			p.parse();		
									
			Tab.dump(null);			
			
			//Counting.print();
			
			if (!p.errorDetected) {
				File objFile = new File("test/program.obj");
				if (objFile.exists())
					objFile.delete();
				Code.write(new FileOutputStream(objFile));
				log.info("Parsiranje uspesno zavrseno!");
			}
			else {
				log.error("Parsiranje NIJE uspesno zavrseno!");
			}
		} finally {
			if (br != null) {
				try {
					br.close();
				}
				catch(IOException e1) {
					log.error(e1.getMessage(), e1);
				}	
			}
		}
	}
	
}

/*log.info("------------- SINTAKSA [A] -------------------");
log.info("Global Constants: " + Utility.NumberGlobalConsts);
log.info("Global Variables: " + Utility.NumberGlobalVars);
log.info("Global Array Variables: " + Utility.NumberGlobalArrayVars);
log.info("Main Method Local Variables: " + Utility.NumberMainLocalVars);
log.info("------------- SINTAKSA [B] -------------------");
log.info("StatementsBlock: " + Utility.NumberStatementsBlock);
log.info("MainInvokeCalls: " + Utility.NumberMainInvokeCalls);
log.info("Class Global Methods: " + Utility.NumberClassMethods);
log.info("Class Static Methods: " + Utility.NumberClassStaticMethods);
log.info("Methods' Formal Arguments: " + Utility.NumberMethodFormalArguments);
log.info("------------- SINTAKSA [C] -------------------");
log.info("Classes Declared: " + Utility.NumberClassDeclared);
log.info("Classes Methods Declared: " + Utility.NumberClassMethods);
log.info("Syntax: Classes Fields Number: " + Utility.NumberClassFields);
log.info("----------------------------------------------");*/