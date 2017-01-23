package rs.ac.bg.etf.pp1;

import java.util.Stack;

import rs.ac.bg.etf.pp1.Helper.ArrayElementAddress;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class CodeHelper {
	
	public static MJParser Parser;
	public Validator validator = new Validator();	
	public Helper helper = new Helper();
	
	// ---
	// --- Arithmetics
	// ---
	
	// Transformation: a b -> b a
	public void swap() {
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
	}

	// Transformation: [value] Address Index -> Address Index [value]
	public void putValueBeforeArrayAddrIndex() {
		Obj index = Tab.currentScope().findSymbol("@@TEMPOBJ");
		Obj address = Tab.currentScope().findSymbol("@@TEMPOBJ2");
		Obj value = Tab.currentScope().findSymbol("@@TEMPOBJ3");
		
		Code.store(index);
		Code.store(address);
		Code.store(value);
		
		Code.load(address);
		Code.load(index);
		Code.load(value);		
	}
	
	public void assignOp(Obj left, int op, Obj right) {						
		Obj temp = Tab.currentScope().findSymbol("@@TEMPOBJ");
		Obj constTemp = Tab.currentScope().findSymbol("@@CONSTTEMPOBJ");		
		// --- Assign		
		if (op == Code.eq) {
			// -- 
			// -- ARRAY INITIALIZATION
			// -- 
			if (left.getType().getKind() == Struct.Array) {
				Code.put(Code.dup);
				Code.store(temp);				
				for (int i=0; i < Helper.NewArraySize; i++) {
					Obj indexConstObj = new Obj(Obj.Con, "const", Tab.intType);
					indexConstObj.setAdr(i);
					helper.insertNewArrayElementAddress(left, indexConstObj);
				}								
			}
			// --
			Code.store(left);
		}														
		// --- AddopRight | MulopRight
		else {
			// --- Array Element on left assignment side																													
			if (validator.isArrayElem(left)) {								
				Code.store(temp);												
				Code.put(Code.dup2);
				Code.load(left);								
				Code.load(temp);				
			}
			// --- Variable
			else {
				Code.load(left);
				swap();
			}
			Code.put(op);
			Code.store(left);																							
		}												
	}
	
	public void increment(Obj d) {
		if (validator.isArrayElem(d)) {
			Code.put(Code.dup2);
		}
		Code.load(d);
		Code.loadConst(1);
		Code.put(Code.add);		
		Code.store(d);
	}
	
	public void decrement(Obj d) {
		if (validator.isArrayElem(d)) {
			Code.put(Code.dup2);
		}
		Code.load(d);
		Code.loadConst(1);
		Code.put(Code.sub);		
		Code.store(d);
	}
	
	// Transformation: PUSH Address PUSH Index
	public void pushArrayElementAddress(Obj element) {			
		ArrayElementAddress aea = Helper.elementToArrayElementAddress.get(element);
		Code.load(aea.array);		
		Code.loadConst(aea.expr.getAdr());							
	}

	public Obj fetchResult(Obj l) {
		Code.put(Code.dup);
		Obj temp = Tab.currentScope().findSymbol("@@TEMPOBJ");
		Code.store(temp);
		Obj o = new Obj(Obj.Var, "", l.getType());
		o.setAdr(temp.getAdr());
		return o;
	}
	
	public Obj addopRight(Obj l, int op, Obj r) {								
		
		if (!validator.lvalue(l)) {
			helper.report("Levo od = mora biti lvalue", 0);
			return Tab.noObj;
		}
		
		Code.put(op);
		Obj o = fetchResult(l);	
		if (validator.isArrayElem(l)) {						
			pushArrayElementAddress(l);
			putValueBeforeArrayAddrIndex();			
		}								
		Code.store(l);
		if (validator.isArrayElem(l)) {						
			pushArrayElementAddress(l);			
		}		
		Code.load(l);		
		return o;
	}
	
	public Obj mulopRight(Obj l, int op, Obj r) {
		
		if (!validator.lvalue(l)) {
			helper.report("Levo od = mora biti lvalue", 0);
			return Tab.noObj;
		}
		
		Code.put(op);
		Obj o = fetchResult(l);		
		if (validator.isArrayElem(l)) {						
			pushArrayElementAddress(l);
			putValueBeforeArrayAddrIndex();			
		}								
		Code.store(l);
		if (validator.isArrayElem(l)) {						
			pushArrayElementAddress(l);			
		}		
		Code.load(l);
		return o;
	}
	
	public Obj addopLeft(Obj l, int op, Obj r) {
		Code.put(op);
		return fetchResult(l);
	}
		
	public Obj mulopLeft(Obj l, int op, Obj r) {		
		Code.put(op);
		return fetchResult(l);
	}

	public Obj kapica(Obj l, Obj r) {
		Code.put(Code.dup);
		Obj temp = Tab.currentScope().findSymbol("@@TEMPOBJ");
		Code.put(Code.mul);
		Code.store(temp);
		
		Code.put(Code.dup);
		Code.put(Code.mul);
		Code.load(temp);
		Code.put(Code.add);
		return fetchResult(l);
	}
	
	
	public Obj newArray(Struct type) {
		Obj newArrTemp = new Obj(Obj.Var, "", new Struct(Struct.Array, type));
		Code.put(Code.newarray);
		Code.put(type == Tab.intType ? 1 : 0);							
		return newArrTemp;															
	}
	
	// ---
	
	// ---
	// --- Function Call
	// ---
	
	// ---	
	
	public void designatorFunctionCall(Obj func) {
		int destAdr = func.getAdr() - Code.pc; 
		Code.put(Code.call);
		Code.put2(destAdr);	
	}
	
	public void factorFunctionCall(Obj func, int funcleft) {
		// NOT VOID functions
		Obj result = Tab.noObj;
		helper.checkMainInvokeCall();
		if (!validator.isMeth(func))
			Parser.report_error("Morate pozivati funkciju [" + funcleft + "]", null);
		else if (func.getType() == Tab.noType)
			Parser.report_error("Void funkcija ne moze ucestvovati u izrazu [" + funcleft + "]", null);
		/*else if (!validator.methodParametersMatch(helper.insideClass, func))
			Parser.report_error("Navedeni argumenti nisu ispravni pri pozivu [" + funcleft + "]", null);*/
		else {
			// relativna adresa
			int destAdr = func.getAdr() - Code.pc; 
			Code.put(Code.call);
			Code.put2(destAdr);																				
			result = new Obj(Obj.Var, "", func.getType());															
		}							
	}
		
	public void return_(boolean returnExpr, int sleft, boolean insideMethod, boolean isVoid, Obj currentMethod, Obj returnExpression) {
		
		// returnExpr == false ; Called from RETURN
		// returnExpr == true  ; Called from RETURN EXPR

		if (!insideMethod) {
			Parser.report_error("Return sme biti samo u funkcijama [" + sleft + "]", null);			
		}
		
		if (returnExpr && isVoid) {
			Parser.report_error("Void ne vraca vrednost [" + sleft + "]", null);
		} 
		
		if (!returnExpr && !isVoid) {
			Parser.report_error("Morate vratiti vrednost [" + sleft + "]", null);
		} 
		
		if (returnExpr && !isVoid && currentMethod.getType().getKind() != returnExpression.getType().getKind()) {
			Parser.report_error("Povratni tip se ne slaze sa tipom metode [" + sleft + "]", null);
		}
		
		Helper.hasReturn = true;
		Code.put(Code.exit);
		Code.put(Code.return_);

	}
	
	public static void declareFunction(String methodName) {
		Obj o = Tab.find(methodName);		
		o.setAdr(Code.pc);
		
		Code.put(Code.enter);
		Code.put(o.getLevel());
		Code.put(Tab.currentScope().getnVars());

		if (methodName.equals("len")) {
			Code.put(Code.load_n + 0); 
			Code.put(Code.arraylength);
		} else if (methodName.equals("ord")) {
			Code.put(Code.load_n + 0); 
			Code.loadConst(48);
			Code.put(Code.sub);
		} else {
			Code.put(Code.load_n + 0); 
			Code.loadConst(48);
			Code.put(Code.add);
		}

		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	// ---
	// --- IF, ELSE Conditions
	// ---	
	
	public static Stack<Integer> patch = new Stack<Integer> ();
	public static Stack<Integer> patch2 = new Stack<Integer> ();
	public static Stack<Boolean> cond = new Stack<Boolean> ();
	
	public Obj relop(Obj left, int opcode, Obj right, int line) {
		
		boolean arrayOrClass = validator.isArray(left) || validator.isArray(right) || validator.isClass(left) || validator.isClass(right); 		
		if (arrayOrClass && opcode != Code.eq && opcode != Code.ne) Parser.report_error("> Uz klasu ili niz samo == i != su dozvoljeni [" + line + "]", null);
		
		Code.putFalseJump(opcode, 0);	
		int p = Code.pc - 2;		
		// IF (left RelOp right)			
		// {
		Code.loadConst(1);				
		// }			
		Code.putJump(0);
		int p2 = Code.pc - 2;
		Code.fixup(p);
		// ELSE			
		// {
		Code.loadConst(0);
		// }
		Code.fixup(p2);				
		
		return new Obj(Obj.Con, "boolRelop_1_0", Parser.boolType);
	}	
	
	public void checkCond(int or) {
		
		// OR = 1
		// AND = 0
		
		// if (left && right)
		// {
		// 		pop, pop, load(0)
		// }
		// else {
		//		pop, load(1)
		//		if (left == right) {
		//			load(1);
		//		}
		//		else {
		//			load(0);
		//		}
		// }
		
		Code.put(Code.dup2);		
		Code.putFalseJump(Code.ne, 0);	
		int p = Code.pc - 2;
		// THEN
		Code.put(Code.pop);
		Code.put(Code.pop);
		Code.loadConst(or);			
		Code.putJump(0);
		int p2 = Code.pc - 2;
		Code.fixup(p);
		
		// ELSE
		Code.put(Code.pop);
		Code.loadConst(1);
		Code.putFalseJump(Code.eq, 0);	
		p = Code.pc - 2;
		Code.loadConst(1);			
		Code.putJump(0);
		int p3 = Code.pc - 2;
		Code.fixup(p);
		
		Code.loadConst(0);			
		Code.fixup(p2);
		Code.fixup(p3);
	}
	
	// ---
	// --- FOR Loop
	// ---	
	
	/*public static int loopCondition;
	public static int loopInc;
	public static int loopBody;
	public static int loopEnd;	
	public static int breakDest;
*/
	
	public static Stack<Boolean> inFor = new Stack<>();
	public static Stack<Integer> loopCondition = new Stack<>();
	public static Stack<Integer> loopInc = new Stack<>();
	public static Stack<Integer> loopBody = new Stack<>();
	public static Stack<Integer> loopEnd = new Stack<>();
	public static Stack<Integer> breakDest = new Stack<>();
	
	public static Stack<Integer> continueDest = new Stack<>();
	public static Stack<Boolean> continueFound = new Stack<>();
	//public static int continueDest; 
	//public static boolean continueFound;
	
	
	
	
	//public static Stack<Integer> continueAddresses = new Stack<>();
	//public static Stack<Integer> breakAddresses = new Stack<>();
	//public static Stack<Boolean> booleanFounds = new Stack<>();
	
}
