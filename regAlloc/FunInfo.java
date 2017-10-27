package regAlloc;

import java.util.*;
import java.util.concurrent.TimeUnit;


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

    public void printInsOuts(){
        for(int i=0; i<blocklist.size(); i++){
            System.out.print("Block: "+ i+ " ");
            blocklist.get(i).printInOut();
            System.out.println();
        }
        for(int i=0; i<ranges.size(); i++){
            System.out.print(" "+ranges.get(i).name);
        }
        System.out.println("\n\n");
    }

    public void getInsOuts(){
        boolean changed = true; 
        Scanner sc = new Scanner(System.in);
        
        TreeSet<Variables> out2 = new TreeSet<Variables>();
        while(changed){
            changed = false;
            for(int i=blocklist.size()-1; i>=0; i--){
                Blocks currB = blocklist.get(i);
                out2.clear();
                for(Blocks succB: currB.succ){
                    out2.addAll(succB.livein);
                }
                if(!out2.equals(currB.liveout)){
                    changed = true;
                    
                    currB.liveout.clear();
                    for(Variables v: out2){
                        currB.liveout.add(v);
                    }
                    
                }
                else{
                    TreeSet<Variables> in2 = currB.livein;
                
                    in2.clear();
                    
                    for(Variables usev: currB.use){
                        in2.add(usev);
                    }
                    for(Variables outnv: currB.liveout){
                        if(!currB.def.contains(outnv))
                            in2.add(outnv);
                    }
                }
            }
            

        }
        updateRanges();
    }

    public void updateRanges(){
        for(int i=0; i<blocklist.size(); i++){
            Blocks blk = blocklist.get(i);
            for(Variables var: blk.liveout){
                var.updatestart(i);
            }
            for(Variables var : blk.livein)
                var.updateend(i);
        }
        for(Variables v: ranges){
            if(v.start==Integer.MAX_VALUE){
                v.start = 0;
            }
            if(v.end==Integer.MIN_VALUE)
                v.end = blocklist.size()-1;
            v.updatesize();
        }
        

        
    }


}