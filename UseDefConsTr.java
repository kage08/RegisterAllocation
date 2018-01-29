

import java.util.Iterator;
import java.util.Map.Entry;
import regAlloc.*;
import visitor.DepthFirstVisitor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Enumeration;
import syntaxtree.*;

public class UseDefConsTr extends DepthFirstVisitor{

    int labelct;


    public ArrayList<FunInfo> FunList;
    FunInfo currFun;
    Blocks currBloc;
    Blocks prevBloc;
    boolean isLabelStmt;
    HashMap<String, Blocks> labelMap;
    HashMap<Blocks, String> addRequests;
    HashMap<Integer, Variables> varMap;
    String currLabel;
    boolean defsucc;
    int currTemp;
    boolean implicitadd;


    public UseDefConsTr(){
        labelct = 1;
        FunList = new ArrayList<FunInfo>();
        currFun = null;
        prevBloc = null;
        isLabelStmt = false;
        labelMap = new HashMap<String, Blocks>();
        addRequests = new HashMap<Blocks, String>();
        varMap = new HashMap<Integer, Variables>();
        defsucc = true;
        implicitadd = false;
    }

    /**
    * f0 -> "MAIN"
    * f1 -> StmtList()
    * f2 -> "END"
    * f3 -> ( Procedure() )*
    * f4 -> <EOF>
    */
   public void visit(Goal n) {
    n.f0.accept(this);
    currFun = new FunInfo(n.f0.toString());
    currFun.noArgs=0;
    currBloc = prevBloc = null;
    prevBloc = new Blocks();
    n.f1.accept(this);
    n.f2.accept(this);
    FunList.add(currFun);
    satisfyReqs();
    currFun = null;
    currBloc = prevBloc = null;
    labelMap = null;
    addRequests = null;
    n.f3.accept(this);
    n.f4.accept(this);
 }

 /**
    * f0 -> ( ( Label() )? Stmt() )*
    */
    public void visit(StmtList n) {
        n.f0.accept(this);
     }

     public void visit(NodeOptional n) {
        if ( n.present() ){
            isLabelStmt = true;
            Label lb = (Label)n.node;
            currLabel = lb.f0.toString();
            currFun.labmap.put(currLabel, labelct);
            labelct++;

        }
           
     }

  /**
    * f0 -> Label()
    * f1 -> "["
    * f2 -> IntegerLiteral()
    * f3 -> "]"
    * f4 -> StmtExp()
    */
    public void visit(Procedure n) {
        n.f0.accept(this);
        currFun = new FunInfo(n.f0.f0.toString());
        labelMap = new HashMap<String, Blocks>();
        addRequests = new HashMap<Blocks, String>();
        varMap = new HashMap<Integer, Variables>();
        defsucc = true;
        isLabelStmt = false;
        n.f1.accept(this);
        n.f2.accept(this);
        currFun.noArgs = Integer.parseInt(n.f2.f0.toString());
        currBloc = prevBloc = null;
        prevBloc = new Blocks();
        currBloc = prevBloc;
        
        for(Integer i=0; i<currFun.noArgs; i++){
            varMap.put(i, new Variables(i, currFun));
            currFun.ranges.add(varMap.get(i));
            currBloc.def.add(varMap.get(i));
            currTemp = i;
            
        }
        currBloc = null;
        currFun.blocklist.add(prevBloc);

        n.f3.accept(this);
        
        

        n.f4.accept(this);

        currFun.blocklist.add(new Blocks());

        satisfyReqs();

        FunList.add(currFun);
        currFun = null;
        currBloc = prevBloc = null;
        labelMap = null;
        addRequests = null;
        varMap = null;
     }

     void satisfyReqs(){
         for(Blocks blk: addRequests.keySet()){
             Blocks target = labelMap.get(addRequests.get(blk));
             blk.succ.add(target);
             target.pred.add(blk);
         }
     }
      /**
    * f0 -> "BEGIN"
    * f1 -> StmtList()
    * f2 -> "RETURN"
    * f3 -> SimpleExp()
    * f4 -> "END"
    */
   public void visit(StmtExp n) {
    n.f0.accept(this);
    n.f1.accept(this);
    n.f2.accept(this);
    currBloc = new Blocks();
    currBloc.pred.add(prevBloc);
    prevBloc.succ.add(currBloc);
    n.f3.accept(this);
    currFun.blocklist.add(currBloc);
    prevBloc = currBloc;
    currBloc = null;

    n.f4.accept(this);
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
     public void visit(Stmt n) {
         currBloc = new Blocks();
         if(defsucc){

            prevBloc.succ.add(currBloc);
            currBloc.pred.add(prevBloc);
        }
        else{
            defsucc = true;
        }
         
        n.f0.accept(this);
        if(isLabelStmt){
            labelMap.put(currLabel, currBloc);
            isLabelStmt = false;
        }
        currFun.blocklist.add(currBloc);

        

        prevBloc = currBloc;
        currBloc = null;

     }

     /**
    * f0 -> "NOOP"
    */
   public void visit(NoOpStmt n) {
    n.f0.accept(this);
 }

 public void visit(ErrorStmt n) {
    n.f0.accept(this);
 }
 /**
    * f0 -> "TEMP"
    * f1 -> IntegerLiteral()
    */
    public void visit(Temp n) {
        n.f0.accept(this);
        n.f1.accept(this);
        currTemp = Integer.parseInt(n.f1.f0.toString());
        if(implicitadd)
            addUse();
     }

 /**
    * f0 -> "CJUMP"
    * f1 -> Temp()
    * f2 -> Label()
    */
    public void visit(CJumpStmt n) {
        n.f0.accept(this);
        n.f1.accept(this);
        if(varMap.containsKey(currTemp)){
            currBloc.use.add(varMap.get(currTemp));
        }
        else{
            varMap.put(currTemp, new Variables(currTemp, currFun));
            currBloc.use.add(varMap.get(currTemp));
        }

        String goLab = n.f2.f0.toString();
        if(labelMap.containsKey(goLab)){
            currBloc.succ.add(labelMap.get(goLab));
            labelMap.get(goLab).pred.add(currBloc);
        }
        else{
            addRequests.put(currBloc, goLab);
         }

        n.f2.accept(this);
     }

     /**
    * f0 -> "JUMP"
    * f1 -> Label()
    */
   public void visit(JumpStmt n) {
    n.f0.accept(this);
    n.f1.accept(this);
    String goLab = n.f1.f0.toString();
    if(labelMap.containsKey(goLab)){
        currBloc.succ.add(labelMap.get(goLab));
        labelMap.get(goLab).pred.add(currBloc);
    }
    else{
        addRequests.put(currBloc, goLab);
    }
    defsucc = false;

 }

 void addUse(){
    if(varMap.containsKey(currTemp)){
        currBloc.use.add(varMap.get(currTemp));
    }
    else{
        varMap.put(currTemp, new Variables(currTemp, currFun));
        currBloc.use.add(varMap.get(currTemp));
    }
 }

 void addDef(){
    if(varMap.containsKey(currTemp)){
        currBloc.def.add(varMap.get(currTemp));
    }
    else{
        varMap.put(currTemp, new Variables(currTemp, currFun));
        currBloc.def.add(varMap.get(currTemp));
        currFun.ranges.add(varMap.get(currTemp));
    }
 }

  /**
    * f0 -> "HSTORE"
    * f1 -> Temp()
    * f2 -> IntegerLiteral()
    * f3 -> Temp()
    */
    public void visit(HStoreStmt n) {
         n.f0.accept(this);
        n.f1.accept(this);
        addUse();
        n.f2.accept(this);
        n.f3.accept(this);
        addUse();
     }

     /**
    * f0 -> "HLOAD"
    * f1 -> Temp()
    * f2 -> Temp()
    * f3 -> IntegerLiteral()
    */
   public void visit(HLoadStmt n) {
    n.f0.accept(this);
    n.f1.accept(this);
    
    
    addDef();
    n.f2.accept(this);
    addUse();
    n.f3.accept(this);
 }

 /**
    * f0 -> "MOVE"
    * f1 -> Temp()
    * f2 -> Exp()
    */
    public void visit(MoveStmt n) {
        n.f0.accept(this);
        n.f1.accept(this);
        
        addDef();
        n.f2.accept(this);
     }

     /**
    * f0 -> "CALL"
    * f1 -> SimpleExp()
    * f2 -> "("
    * f3 -> ( Temp() )*
    * f4 -> ")"
    */
   public void visit(Call n) {
    n.f0.accept(this);
    n.f1.accept(this);
    n.f2.accept(this);

    implicitadd = true;
    n.f3.accept(this);
    implicitadd = false;

    currFun.doescall = true;

    if(n.f3.nodes.size()> currFun.maxCallArgs)
        currFun.maxCallArgs = n.f3.nodes.size();
    n.f4.accept(this);
 }
 /**
    * f0 -> Temp()
    *       | IntegerLiteral()
    *       | Label()
    */
    public void visit(SimpleExp n) {
        
        n.f0.accept(this);
        if(n.f0.which == 0)
            addUse();
     }

     /**
    * f0 -> Operator()
    * f1 -> Temp()
    * f2 -> SimpleExp()
    */
   public void visit(BinOp n) {
    n.f0.accept(this);
    n.f1.accept(this);
    addUse();
    n.f2.accept(this);
 }

 public ArrayList<FunInfo> getInfo(){
     return FunList;
 }

 public void prettyPrintInfo(){
     for(FunInfo fn: FunList){
         System.out.println("For Function: "+fn.name);
         fn.printDefUse();
     }
 }

 public void getInOuts(){
    for(FunInfo fn: FunList){
        //System.out.println("For Function: "+fn.name);
        fn.getInsOuts();
        //fn.printInsOuts();
        fn.doLinScan();
        //fn.printLinScan();
        //System.out.println();
    } 
 }

     
}