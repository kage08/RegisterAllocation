package regAlloc;

import java.util.*;


public class FunInfo{
    public String name;
    public ArrayList<Blocks> blocklist;
    public ArrayList<Variables> ranges;
    public HashMap<Integer, Variables> rangemap;
    public boolean doescall;
    public int noArgs, toSpill, maxCallArgs, stkSlots, sregused;
    public ArrayList<Variables> spilled;
    public TreeSet<String> regUsed;
    public HashMap<String, Integer> labmap;

    public FunInfo(String nam){
        name = nam;
        blocklist = new ArrayList<Blocks>();
        doescall=false;
        noArgs = toSpill = maxCallArgs = stkSlots = 0; 
        ranges = new ArrayList<Variables>();
        spilled = new ArrayList<Variables>();
        regUsed = new TreeSet<String>();
        labmap = new HashMap<String, Integer>();
        rangemap = new HashMap<Integer, Variables>();
    }

    public void printDefUse(){
        for(int i=0; i<blocklist.size(); i++){
            System.out.print("Block: "+ i+ " ");
            blocklist.get(i).printUseDef();
            System.out.println();
            System.out.print("Pred: ");
            for(Blocks bk: blocklist.get(i).pred){
                System.out.print(blocklist.indexOf(bk)+" ");
            }
            System.out.println();
            System.out.print("Succ: ");
            for(Blocks bk: blocklist.get(i).succ){
                System.out.print(blocklist.indexOf(bk)+" ");
            }
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
            System.out.println(" "+ranges.get(i).name+" "+ranges.get(i).start+" "+ranges.get(i).end);
        }
        System.out.println("\n\n");
    }

    public void getInsOuts(){
        boolean changed = true; 
        Scanner sc = new Scanner(System.in);
        
        TreeSet<Variables> out2 = new TreeSet<Variables>();
        do{
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
            

        }while(changed);
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

    public void doLinScan(){
        new LinearScan(this).lscan();
        stkSlots = Math.min(regUsed.size(),8) + toSpill+1;
        stkSlots += (noArgs>4)?(noArgs-4):0;
        sregused = Math.min(regUsed.size(), 8);
        for(Variables v: ranges){
            rangemap.put(v.name, v);
        }
        
    }

    public void printLinScan(){
        for(Variables v: ranges){
            System.out.print("Var: "+ v.name);
            if(v.spilled){
                System.out.println("Spilled");
            }
            else{
                System.out.println("Reg: "+(char)v.regtype+" "+v.regnum);
            }
        }
    }


}