
import syntaxtree.*;
import visitor.*;

public class P5 {
   public static void main(String [] args) {
      try {
         Node root = new microIRParser(System.in).Goal();
         //System.out.println("Program parsed successfully");
        
         UseDefConsTr udst = new UseDefConsTr();
         root.accept(udst);
         //udst.prettyPrintInfo();
         udst.getInOuts();

         Translator trs = new Translator(udst.FunList);
         root.accept(trs);
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      }
   }
} 

