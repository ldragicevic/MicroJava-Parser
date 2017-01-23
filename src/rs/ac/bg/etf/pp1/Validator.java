package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import java.util.List;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class Validator {

	public static MJParser Parser;
	public static Helper Helper;
	public static List<Struct> actPars = new ArrayList<Struct>(); 
	public static List<Obj> actParsStack = new ArrayList<>();
	
	public Validator() {}
	
	public boolean intTypes(Obj obj1, Obj obj2) {
		return obj1.getType().getKind() == Struct.Int && obj2.getType().getKind() == Struct.Int;
	}
	
	public boolean intTypes(Struct first, Struct second) {
		return first.getKind() == Struct.Int && second.getKind() == Struct.Int;
	}
	
	public boolean intType(Struct struct) {
		return struct.equals(Tab.intType);
	}
	
	public boolean intType(Obj obj) {
		return obj.getType().equals(Tab.intType);
	}
	
	public boolean lvalue(Obj obj) {
		return ((obj.getKind() == Obj.Var) || (obj.getKind() == Obj.Elem) || (obj.getKind() == Obj.Fld));
	}
	
	public boolean userDefinedClass(Struct struct) {
		return (struct.getKind() == Struct.Class) && (Helper.currentTypeObj.getKind() == Obj.Type);
	}
		
	public void addActParam(Obj par) {
		actPars.add(par.getType());
		actParsStack.add(par);
	}
	
	public boolean methodParametersMatch(boolean insideClass, Obj methodObj) {
				
		boolean firstSkipped = false;
		int methodFormNumber = methodObj.getLevel() - ((insideClass) ? 1 : 0);	
				
		if (actPars.size() != methodFormNumber) { return false; }
		
		int i = 0;
		for (Obj o : methodObj.getLocalSymbols()) {
			if (o.getType() == Tab.noType && !firstSkipped) {
				firstSkipped = true;
				continue;
			}				
			if (i == actPars.size()) 
				break;
			if (!actPars.get(i).assignableTo(o.getType()) && (actPars.get(i).getKind() != o.getType().getKind())) {
				return false;
			}				
			i++;
		}		
		actPars.clear();
		return true; 
	}
	
	public boolean lvalueClass(Obj obj) {
		return obj.getType().getKind() == Struct.Class;
	}
	
	public boolean isArray(Obj obj) {
		return obj.getType().getKind() == Struct.Array;
	}
	
	public boolean isArrayElem(Obj obj) {
		return obj.getKind() == obj.Elem;
	}
	
	public boolean isMeth(Obj obj) {
		return obj.getKind() == Obj.Meth;
	}
	
	public boolean isClass(Obj obj) {
		return obj.getType().getKind() == Struct.Class;
	}
	
	public boolean isPrimitive(Obj obj) {
		int kind = obj.getType().getKind();
		return kind == Struct.Int || kind == Struct.Char || kind == Struct.Bool;
	}
	
	public boolean bothVariablesArrayOrClass(Obj first, Obj second, int opcode) {
		// None, Int, Char, Array, Class, Bool
		int firstKind = first.getType().getKind(); 
		int secondKind = second.getType().getKind();
		if (first.getKind() != Obj.Var || second.getKind() != Obj.Var) 
			return false;
		else if (firstKind == Struct.Class || firstKind == Struct.Array || secondKind == Struct.Class || secondKind == Struct.Array) {
			// Razliciti su
			if (firstKind != secondKind) 
				return false;
			// Isti tipovi - proveri da li se koristi == ili != 
			else if (opcode != Code.eq && opcode != Code.ne) 
				return false;			
		} 
		return true;
	}	
	
	public boolean boolTypes(Obj first, Obj second) {
		return boolType(first) && boolType(second);  
	}	
	
	public boolean boolRvalue(Obj first) {
		return boolType(first) && 
				(first.getKind() == Obj.Con || first.getKind() == Obj.Var || first.getKind() == Obj.Elem || first.getKind() == Obj.Fld);
	}
	
	public boolean boolLvalue(Obj first) {
		return (first.getKind() == Obj.Fld || first.getKind() == Obj.Var || first.getKind() == Obj.Elem) && boolType(first);
	}
	
	public boolean boolType(Obj obj) {
		return obj.getType().getKind() == Struct.Bool;
	}
	
	public boolean checkAssignment(Obj left, Obj right) {		
		if (lvalue(left) == false) {
			return false;
		}		
		if (((right != null && right.getType().assignableTo(left.getType())) || (boolRvalue(right) && boolLvalue(left)) || (objectLvalue(left) && (objectRvalue(right)))) == false) {
			return false;
		} 			
		return true;
	}
	
	public boolean objectLvalue(Obj o) {
		return o.getKind() == Obj.Var && o.getType().getKind() == Struct.Class;
	}
	
	public boolean objectRvalue(Obj o) {
		return o.getKind() == Obj.Var && o.getType().getKind() == Struct.Class;
	}
	
	public boolean sameObjSameStruct(Obj first, Obj second) {
		return first.getType().getKind() == second.getType().getKind() && first.getKind() == second.getKind();
	}
	
}
