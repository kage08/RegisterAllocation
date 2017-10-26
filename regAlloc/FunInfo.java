package regAlloc;

import java.util.*;


public class FunInfo{
    public String name;
    public ArrayList<Blocks> blocklist;
    public ArrayList<Variables> ranges;
    public boolean doescall;
    public int noArgs, toSpill, maxCallArgs, stkSlots;

    public FunInfo(String nam){
        name = nam;
        blocklist = new ArrayList<Blocks>();
        doescall=false;
        noArgs = toSpill = maxCallArgs = stkSlots = 0; 
        ranges = new ArrayList<Variables>();
    }

    public void printDefUse(){
        for(int i=0; i<blocklist.size(); i++){
            System.out.print("Block: "+ i+ " ");
            blocklist.get(i).printUseDef();
            System.out.println();
        }
        for(int i=0; i<ranges.size(); i++){
            System.out.print(" "+ranges.get(i).name);
        }
        System.out.println("\n\n");
    }

    
}