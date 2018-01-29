import regAlloc.*;
import java.util.*;
import visitor.GJNoArguDepthFirst;
import syntaxtree.*;

public class Translator extends GJNoArguDepthFirst<Variables>{
    int labelcounter;
    ArrayList<FunInfo> funlist;
    ArrayList<Variables> arglist;
    Blocks currBloc;
    FunInfo currFun;
    int funindex;
    int blocindex;
    Stack<String> code;
    int vtaken = 0;
    int extraoff;
    boolean addtolist = false;

    public Translator(ArrayList<FunInfo> fl){
        funlist = fl;
        labelcounter = 0;
        currBloc = null;
        currFun = null;
        funindex = blocindex = 0;
        code = new Stack<String>();
    }

    int nextct(){
        return (++labelcounter);
    }

    /**
    * f0 -> "MAIN"
    * f1 -> StmtList()
    * f2 -> "END"
    * f3 -> ( Procedure() )*
    * f4 -> <EOF>
    */
   public Variables visit(Goal n) {
    Variables _ret=null;
    n.f0.accept(this);
    currFun = funlist.get(funindex);
    System.out.println(currFun.name+"["+(currFun.noArgs)+"]["+(currFun.stkSlots)+"]["+(currFun.maxCallArgs+1)+"]");
    n.f1.accept(this);
    n.f2.accept(this);
    System.out.println("END");
    if(currFun.toSpill==0){
        System.out.println("// NOTSPILLED");
    }
    else{
        System.out.println("// SPILLED");
    }

    currBloc = null; currFun = null;
    System.out.println();
    n.f3.accept(this);
    n.f4.accept(this);
    return _ret;
 }

 public Variables visit(NodeOptional n) {
    if ( n.present() ){
        Label lb = (Label)n.node;
        String currLabel = lb.f0.toString();
        int mapint = currFun.labmap.get(currLabel);
        System.out.println("L"+mapint);
       return n.node.accept(this);
    }
    else
       return null;
 }

 /**
    * f0 -> Label()
    * f1 -> "["
    * f2 -> IntegerLiteral()
    * f3 -> "]"
    * f4 -> StmtExp()
    */
    public Variables visit(Procedure n) {
        Variables _ret=null;
        funindex++;
        blocindex = 1;
        currFun = funlist.get(funindex);
        n.f0.accept(this);
        if(currFun.doescall)
            System.out.println(currFun.name+"["+(currFun.noArgs)+"]["+(currFun.stkSlots+currFun.regUsed.size()-currFun.sregused)+"]["+(currFun.maxCallArgs+1)+"]");
        else
            System.out.println(currFun.name+"["+(currFun.noArgs)+"]["+(currFun.stkSlots)+"]["+(currFun.maxCallArgs)+"]"); 

        extraoff = Math.max(0, currFun.noArgs-4);
        //Store callee save
        for(String reg: currFun.regUsed){
            if(reg.charAt(0)=='s'){
                int stype = Integer.parseInt(Character.toString(reg.charAt(1)));
                System.out.println("ASTORE SPILLEDARG "+(stype+extraoff)+" s"+stype);
            }
        }

        //Transfer parameters to registers
        for(int i=0; i<currFun.noArgs; i++){
            if(i<4){
                Variables v = currFun.rangemap.get(i);
                if(v.spilled){
                    System.out.println("MOVE v0 a"+i);
                    System.out.println("ASTORE SPILLEDARG "+(currFun.sregused+v.spillnumber+extraoff)+" v0");
                }
                else{
                    System.out.println("MOVE "+v.regName()+" a"+i);
                }
            }
            else{
                Variables v = currFun.rangemap.get(i);
                
                if(v.spilled){
                    System.out.println("ALOAD v0 SPILLEDARG "+(i-4));
                    System.out.println("ASTORE SPILLEDARG "+(currFun.sregused+v.spillnumber+extraoff)+" v0");
                }
                else{
                    System.out.println("ALOAD "+v.regName()+" SPILLEDARG "+(i-4));
                }
            }
        }

    

        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);

        for(String reg: currFun.regUsed){
            if(reg.charAt(0)=='s'){
                int stype = Integer.parseInt(Character.toString(reg.charAt(1)));
                System.out.println("ALOAD "+reg+" SPILLEDARG "+(stype+extraoff));
            }
        }

        System.out.println("END");
        if(currFun.toSpill==0){
            System.out.println("// NOTSPILLED");
        }
        else{
            System.out.println("// SPILLED");
        }
        currBloc = null; currFun = null;
        System.out.println();
        return _ret;
     }

     /**
    * f0 -> NoOpStmt()
    *       | ErrorStmt()
    *       | CJumpStmt()
    *       | JumpStmt()
    *       | HStoreStmt()
    *       | HLoadStmt()
    *       | MoveStmt()
    *       | PrintStmt()
    */
   public Variables visit(Stmt n) {
    Variables _ret=null;
    currBloc = currFun.blocklist.get(blocindex);
    vtaken = 0;
    if(!currBloc.def.isEmpty() && !currBloc.liveout.contains(currBloc.def.first())){}
    else
    n.f0.accept(this);
    blocindex++;
    return _ret;
 }

 /**
    * f0 -> "NOOP"
    */
    public Variables visit(NoOpStmt n) {
        Variables _ret=null;
        n.f0.accept(this);
        System.out.println("NOOP");
        return _ret;
     }
  
     /**
      * f0 -> "ERROR"
      */
     public Variables visit(ErrorStmt n) {
        Variables _ret=null;
        n.f0.accept(this);
        System.out.println("ERROR");
        return _ret;
     }

     /**
    * f0 -> "TEMP"
    * f1 -> IntegerLiteral()
    */
   public Variables visit(Temp n) {
    Variables _ret=null;
    n.f0.accept(this);
    n.f1.accept(this);
    int tpno = Integer.parseInt(n.f1.f0.toString());
    if(currBloc.def.size()>0 && currBloc.def.first().name==tpno)
        return currBloc.def.first();
    else{
        for(Variables var : currBloc.use){
            if(var.name == tpno){
                if(addtolist){
                    if(var.spilled){
                        System.out.println("ALOAD v0 SPILLEDARG "+(currFun.sregused+var.spillnumber+extraoff));
                        arglist.add(var);
                        if(arglist.size()>4)
                            System.out.println("PASSARG "+(arglist.size()-4)+" v0");
                        else
                            System.out.println("MOVE a"+(arglist.size()-1)+" v0");
                        
                    }
                    else{
                        arglist.add(var);
                        if(arglist.size()>4)
                            System.out.println("PASSARG "+(arglist.size()-4)+" "+var.regName());
                        else
                            System.out.println("MOVE a"+(arglist.size()-1)+" "+var.regName());
                    } 
                }
                return var;
            }
        }
    }
    System.out.println("Cant find temp1 "+blocindex+" "+tpno);
    for (Variables v: currBloc.use){
        System.out.print(" "+v.name);
    }
    return _ret;
 }

     /**
    * f0 -> "CJUMP"
    * f1 -> Temp()
    * f2 -> Label()
    */
   public Variables visit(CJumpStmt n) {
    Variables _ret=null;
    n.f0.accept(this);
    Variables tp = n.f1.accept(this);

    if(tp.spilled){
        System.out.println("ALOAD v0 SPILLEDARG "+(currFun.sregused+tp.spillnumber+extraoff));
        System.out.println("CJUMP v0 L"+currFun.labmap.get(n.f2.f0.toString()));
    }
    else{
        System.out.println("CJUMP "+tp.regName()+ " L"+currFun.labmap.get(n.f2.f0.toString()));
    }

    n.f2.accept(this);
    return _ret;
 }

 /**
    * f0 -> "JUMP"
    * f1 -> Label()
    */
    public Variables visit(JumpStmt n) {
        Variables _ret=null;
        n.f0.accept(this);
        n.f1.accept(this);
        System.out.println("JUMP  L"+currFun.labmap.get(n.f1.f0.toString()));
        return _ret;
     }

     /**
    * f0 -> "HSTORE"
    * f1 -> Temp()
    * f2 -> IntegerLiteral()
    * f3 -> Temp()
    */
   public Variables visit(HStoreStmt n) {
    Variables _ret=null;
    n.f0.accept(this);
    Variables v1 = n.f1.accept(this);
    
    n.f2.accept(this);
    int offset = Integer.parseInt(n.f2.f0.toString());
    Variables v2 = n.f3.accept(this);

    
    String r1 = (v1.spilled)?"v0":v1.regName();
    String r2 = (v2.spilled)?"v1":v2.regName();
    if(v1.spilled){
        System.out.println("ALOAD v0 SPILLEDARG "+(currFun.sregused+v1.spillnumber+extraoff));
    }
    if(v2.spilled){
        System.out.println("ALOAD v1 SPILLEDARG "+(currFun.sregused+v2.spillnumber+extraoff));
    }

    System.out.println("HSTORE "+r1+" "+offset+" "+r2);
    return _ret;
 }

 /**
    * f0 -> "HLOAD"
    * f1 -> Temp()
    * f2 -> Temp()
    * f3 -> IntegerLiteral()
    */
    public Variables visit(HLoadStmt n) {
        Variables _ret=null;
        n.f0.accept(this);
        Variables v1 = n.f1.accept(this);
        Variables v2 = n.f2.accept(this);
        int offset = Integer.parseInt(n.f3.f0.toString());
        String r1 = (v1.spilled)?"v0":v1.regName();
        String r2 = (v2.spilled)?"v1":v2.regName();
        if(v2.spilled){
            System.out.println("ALOAD v1 SPILLEDARG "+(currFun.sregused+v2.spillnumber+extraoff));
        }

        System.out.println("HLOAD "+r1+" "+r2+" "+offset);

        if(v1.spilled){
            System.out.println("ASTORE SPILLEDARG "+(currFun.sregused+v1.spillnumber+extraoff)+" v0");
        }


        return _ret;
     }

     /**
    * f0 -> "MOVE"
    * f1 -> Temp()
    * f2 -> Exp()
    */
   public Variables visit(MoveStmt n) {
    Variables _ret=null;
    n.f0.accept(this);
    Variables v1 = n.f1.accept(this);
    String r1 = (v1.spilled)?"v0":v1.regName();
    
    n.f2.accept(this);
    System.out.print("MOVE "+r1+" "+code.pop());
    System.out.println();
    if(v1.spilled){
        System.out.println("ASTORE SPILLEDARG "+(currFun.sregused+v1.spillnumber+extraoff)+" v0");
    }
    return _ret;
 }
 /**
    * f0 -> "PRINT"
    * f1 -> SimpleExp()
    */
    public Variables visit(PrintStmt n) {
        Variables _ret=null;
        n.f0.accept(this);
        n.f1.accept(this);
        System.out.println("PRINT "+code.pop());
        return _ret;
     }

     /**
    * f0 -> "BEGIN"
    * f1 -> StmtList()
    * f2 -> "RETURN"
    * f3 -> SimpleExp()
    * f4 -> "END"
    */
     public Variables visit(StmtExp n) {
        Variables _ret=null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        currBloc = currFun.blocklist.get(blocindex);
        n.f3.accept(this);
        System.out.println("MOVE v0 "+code.pop());
        n.f4.accept(this);
        return _ret;
     }

      /**
    * f0 -> "CALL"
    * f1 -> SimpleExp()
    * f2 -> "("
    * f3 -> ( Temp() )*
    * f4 -> ")"
    */
   public Variables visit(Call n) {
    Variables _ret=null;
    int ct = 0;
    for(Variables v: currBloc.liveout){
        if(!v.spilled && v.regtype=='t'){
            System.out.println("ASTORE SPILLEDARG "+(currFun.sregused+extraoff+currFun.toSpill+ct)+" "+v.regName());
            ct++;
        }
    }
    n.f0.accept(this);
    arglist = new ArrayList<Variables>();
    addtolist = true;
    n.f2.accept(this);
    n.f3.accept(this);
    n.f4.accept(this);
    addtolist = false;
    n.f1.accept(this);
    System.out.println("CALL "+code.pop());
    code.push(" v0 ");
    ct = 0;
    for(Variables v: currBloc.liveout){
        if(!v.spilled && v.regtype=='t'){
            System.out.println("ALOAD "+v.regName()+" SPILLEDARG "+(currFun.sregused+extraoff+currFun.toSpill+ct));
            ct++;
        }
    }
    return _ret;
 }

 /**
    * f0 -> "HALLOCATE"
    * f1 -> SimpleExp()
    */
    public Variables visit(HAllocate n) {
        Variables _ret=null;
        n.f0.accept(this);
        n.f1.accept(this);
        code.push("HALLOCATE "+code.pop());
        return _ret;
     }

     /**
    * f0 -> Operator()
    * f1 -> Temp()
    * f2 -> SimpleExp()
    */
   public Variables visit(BinOp n) {
    Variables _ret=null;
    String ans = " ";
    switch(n.f0.f0.which){
        case 0: ans = "LE "; break;
        case 1: ans = "NE "; break;
        case 2: ans = "PLUS "; break;
        case 3: ans = "MINUS "; break;
        case 4: ans = "TIMES "; break;
        case 5: ans="DIV "; break;
    }
    Variables v1 = n.f1.accept(this);
    String r1 = (v1.spilled)?"v1":v1.regName();
    if(v1.spilled){
        System.out.println("ALOAD v1 SPILLEDARG "+(currFun.sregused+v1.spillnumber+extraoff));
    }

    
    n.f2.accept(this);
    ans+= r1;
    ans+= " ";
    ans+= code.pop();
    code.push(ans);
    return _ret;
 }

 /**
    * f0 -> Temp()
    *       | IntegerLiteral()
    *       | Label()
    */
    public Variables visit(SimpleExp n) {
        Variables _ret=null;
        if(n.f0.which==0){
            Variables v1 = n.f0.accept(this);
            if(v1.spilled){
                System.out.println("ALOAD v0 SPILLEDARG "+(currFun.sregused+v1.spillnumber+extraoff));
                
                code.push(" v0 ");
            }
            else{
                code.push(" "+v1.regName()+" ");
            }

        }
        else if(n.f0.which==1){
            IntegerLiteral num = (IntegerLiteral)n.f0.choice;
            code.push(num.f0.toString());
        }
        else{
            Label lb = (Label)n.f0.choice;
            code.push(lb.f0.toString());
        }
        return _ret;
     }


}