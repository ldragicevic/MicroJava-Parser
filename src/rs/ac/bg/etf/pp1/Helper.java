package rs.ac.bg.etf.pp1;

import java.util.HashMap;
import java.util.LinkedList;

import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.*;

public class Helper {
	
	public static MJParser Parser;	
	
	public Helper() {}
	
	// ------------		
	// ARRAY starting address
	//public static HashMap<Obj, Obj> arrayAddress = new HashMap<Obj, Obj>();
	
	public static LinkedList<ArrayElementAddress> elementAddresses = new LinkedList<>();
	public static HashMap<Obj, ArrayElementAddress> elementToArrayElementAddress = new HashMap<>();
	
	public static int NewArraySize;
	
	public void insertNewArrayElementAddress(Obj array, Obj expr) {
		elementAddresses.add(new ArrayElementAddress(array, expr));
	}
	
	class ArrayElementAddress {
		Obj array;
		Obj expr;
		Obj element;
		public ArrayElementAddress(Obj a, Obj e) {	
			array = a;
			expr = e;
			element = new Obj(Obj.Elem, a.getName() + "." + e.getAdr(), a.getType().getElemType());
			elementToArrayElementAddress.put(element, this);
		}
	}	
	
	// ------------
	
	public Obj resolveArrayElem(Obj d, int line, Obj e) {
		int indexValue = e.getAdr();
		for (ArrayElementAddress temp : elementAddresses) {
			System.out.println("indexValue : " + indexValue);												
			if (temp.array == d && temp.expr.getAdr() == indexValue) {									
				temp.element.setAdr(tempObj.getAdr());				
				return temp.element;
			}
		}		
		System.out.println("nije nadjen");
		return d;
	}	
	
	// Parser - Report Error
	public void report(String s, int line) {
		Parser.report_error("GRESKA: " + s + ". [" + line + "]", null);
	}
	
	public void oporavak(int line) {
 		Parser.report_info("> Uspesan oporavak. [" + line + "]", null);
	}

	// Program
	public Obj insertProgram(String progName) {
		Obj res = Tab.insert(Obj.Prog, progName, Tab.noType);
		Tab.openScope();
		CodeHelper.declareFunction("len");
		CodeHelper.declareFunction("ord");
		CodeHelper.declareFunction("chr");			
		return res;
	}
	
	public void closeProgram(Obj progObj) {
		boolean main = false;
		
		Code.dataSize = Tab.currentScope().getnVars();
		Tab.chainLocalSymbols(progObj);
		Tab.closeScope();
		
		for (Obj o : progObj.getLocalSymbols()) {
			if (o.getName().equals("main") && o.getType().equals(Tab.noType) && o.getKind() == Obj.Meth && o.getLevel() == 0) {
				main = true;
			}
		}
		if (!main) report("main mora biti definisan kao void bez argumenata", 1);
	}
	
	// Type
	public static Struct currentType;
	public static Obj currentTypeObj;
	
	public Struct resolveType(String typeName, int line) {
		Struct type = null;
		Obj typeObj = Tab.find(typeName);
		if (typeObj != Tab.noObj) {
			if (typeObj.getKind() == Obj.Type) {
				type = typeObj.getType();
			} else {
				report("navedeni identifikator ne predstavlja tip", line);
				type = Tab.noType;
			}
		} else {
			report("nije pronadjen navedeni tip", line);
			type = Tab.noType;
		}
		currentTypeObj = typeObj;
		currentType = type;
		return type;
	}
	
	// Const
	public int constValue;
	
	public Obj insertConstant(String constName, Struct type, int line) {
		Obj obj = Tab.find(constName);
		if (currentType.getKind() == type.getKind()) {
			if (obj == Tab.noObj) {
				obj = Tab.insert(Obj.Con, constName, type);
				obj.setAdr(constValue);
				//System.out.println("DEFINISANA KONSTANTA: " + constName);
				Counting.found("globalConsts");
			} else {
				report("identifikator je zauzet", line);
			}
		} else {
			report("tipovi se ne slazu", line);
		}
		return obj;
	}
	
	// Variable 
	public Obj insertVariable(String varName, int line, int isArray) {
		Obj obj = Tab.currentScope().findSymbol(varName);
		if (obj == null) {
			Struct struct = (isArray == 1) ? new Struct(Struct.Array, currentType) : currentType;
			obj = Tab.insert(insideClass && !insideMethod ? Obj.Fld : Obj.Var, varName, struct);			
						
			// New method's formal argument
			if (insideFormalArguments) {
				obj.setFpPos(formalParamNumber++);
				currentMethod.setLevel(currentMethod.getLevel() + 1);
			}
			
			if (insideMethod && insideFormalArguments) Counting.found("methodsFormPars");			
			if (insideMethod && currentMethod.getName().equals("main") && !insideFormalArguments) Counting.found("mainLocalVars"); 
			if (!insideClass && !insideMethod) Counting.found(isArray == 0 ? "globalVars" : "globalVarArrays");
			if (insideClass && !insideMethod) Counting.found("classFields");  
		} else {			
			report("identifikator " + varName + " se vec koristi", line);			
		}
		//System.out.println("Definisana promenljiva: " + varName + " tipa: " + obj.getKind() + " - " + obj.getType().getKind());
		return obj;		
	}
	
	// Method 
	public Obj currentMethod;
	public boolean insideMethod;
	public boolean isStatic;
	public boolean isVoid;
	public boolean insideFormalArguments;
	public static boolean hasReturn;
	public static boolean inFor;
	public int formalParamNumber;	
	
	public Obj insertMethod(String name, int line) {
		if (isStatic && !insideClass) report("samo metode klase mogu biti static", line);									
		Obj obj = Tab.currentScope().findSymbol(name);		
		if (obj == null) {
			currentMethod = Tab.insert(Obj.Meth, name, currentType);												
			if (!insideClass) Counting.found("globalMethods");						
			if (insideClass) {
				Counting.found(isStatic ? "classStaticMethods" : "classGlobalMethods");
				Counting.found("classMethods");
			}												
			currentMethod.setAdr(Code.pc);						
			if ("main".equals(name)) 
				Code.mainPc = Code.pc;											
		} 
		else {
			report("identifikator funkcije je vec iskoriscen", line);
		} 		
		
		if (currentType == Tab.noType) isVoid = true;
		insideMethod = true;
		insideFormalArguments = true;
		formalParamNumber = 0;
		
		Tab.openScope();
		return obj;
	}
	
	public void initializeCodeMethod() {
	
		// Temp obj for instructions
		Tab.insert(Obj.Var, "@@TEMPOBJ", Tab.intType);
		Tab.insert(Obj.Var, "@@TEMPOBJ2", Tab.intType);
		Tab.insert(Obj.Var, "@@TEMPOBJ3", Tab.intType);
		Tab.insert(Obj.Con, "@@CONSTTEMPOBJ", Tab.intType);
			
		int nPars = currentMethod.getLevel();
		int nVars = Tab.currentScope().getnVars();
		
		Code.put(Code.enter);
		Code.put(nPars);
		Code.put(nVars);	
	}
	
	public void closeMethod(int line) {
		Counting.found("statementBlocks");
		
		if (!isVoid && !hasReturn) report("nije pronadjena return naredba", line);
		
		if (currentMethod.getType().getKind() == Struct.None) { // VOID	
			Code.put(Code.exit);
			Code.put(Code.return_);	
		} else {
			Code.put(Code.trap);
			Code.put(1);
		}
		
		Tab.chainLocalSymbols(currentMethod);
		Tab.closeScope();
										
		formalParamNumber = 0;
		insideMethod = false;
		isVoid = false;
		hasReturn = false;		
	}

	public void checkMainInvokeCall() {
		if (insideMethod && currentMethod.getName().equals("main")) {
			Counting.found("mainInvokeCalls");
		}		
	}

	// Class
	public Obj currentClass;
	public Obj superClass;
	public boolean insideClass;
	
	public Obj insertClass(String name, int line) {
		Obj obj = Tab.currentScope().findSymbol(name);
		if (obj == null) {
			obj = currentClass = Tab.insert(Obj.Type, name, new Struct(Struct.Class));			
			Counting.found("classes");
		} else {
			report("identifikator " + name + " je vec u upotrebi", line);
		}
		insideClass = true;
		Tab.openScope();
		return obj;
	}
	
	public void closeClass(int line) {
		// Ulancavanje obj-eva u Scope-u u Struct Class cvor klase
		Tab.chainLocalSymbols(currentClass.getType());
		Tab.closeScope();
		insideClass = false;
	}
	
	public Obj superClass(String name, int line) {
		Obj obj = Tab.find(name); // obj natklase
		boolean validSuperClass = (obj != Tab.noObj) && (obj.getKind() == Obj.Type)	&& (obj.getType().getKind() == Struct.Class) && (!name.equals(currentClass.getName()));
		
		if (validSuperClass) {
			superClass = obj;
			// Kopiranje svih polja klase (polja, metode)
			for (Obj o : obj.getType().getMembers()) {
				Obj newClassField = Tab.insert(o.getKind(), o.getName(), o.getType());
				newClassField.setAdr(o.getAdr());
				newClassField.setFpPos(o.getFpPos());
				newClassField.setLevel(o.getLevel());
				// Ukoliko kopiramo Metodu - kopiranje svih njenih parametara
				if (newClassField.getKind() == Obj.Meth) {
					Tab.openScope();
					for (Obj methVar : o.getLocalSymbols()) {
						Obj newClassMethVar = Tab.insert(methVar.getKind(), methVar.getName(), methVar.getType());
						newClassMethVar.setFpPos(methVar.getFpPos());
						newClassMethVar.setLevel(methVar.getLevel());
						newClassMethVar.setAdr(methVar.getAdr());
					}
					Tab.chainLocalSymbols(newClassField);
					Tab.closeScope();					
				}																
			}			
		} else {
			report("identifikator natklase nije validan", line);
		}					
		return obj;
	}
	
	public boolean superClassFieldRedefinition(Obj field) {
		return superClass != null && superClass.getType().getMembers().contains(field) && field.getType() != currentType;
	}
	
	// Designator ( .ident |  [expr] )
	public Obj currentDesignator;
	public Obj tempObj = new Obj(Obj.Var, "", Tab.intType);	
	public boolean minus;
	
	public Obj resolveIdent(String name, int line) {
		Obj o = Tab.find(name);
		if (o == Tab.noObj) {
			report("nije pronadjen identifikator " + name, line);
		}		
		return o;
	}
	
	public Obj designatorField(Obj object, String name, int line) {			
	
		Obj result = Tab.noObj;
		
		for (Obj o : object.getType().getMembers()) {
			if (name.equals(o.getName())) {
				// Remove first class's method void param
				o.setLevel(o.getLevel() - 1);
				return o;
			}
		}

		return result;
	}


	
	
}
