package regAlloc;

import java.util.*;

public class Blocks {

    public ArrayList<Blocks> succ;
    public ArrayList<Blocks> pred;
    public TreeSet<Variables> livein;
    public TreeSet<Variables> liveout;
    public TreeSet<Variables> def;
    public TreeSet<Variables> use;


    public Blocks(){
        succ = new ArrayList<Blocks>();
        pred = new ArrayList<Blocks>();
        livein = new TreeSet<Variables>();
        liveout = new TreeSet<Variables>();
        def = new TreeSet<Variables>();
        use = new TreeSet<Variables>();
    }

    public void printUseDef(){
        System.out.print("Def: ");
        if(def.size()>0) System.out.print(def.first().name);
        System.out.print(" Use: ");
        for(Variables vr: use){
            System.out.print(" "+vr.name);
        }
    }

    public void printInOut(){
        System.out.print(" In: ");
        for(Variables vr: livein){
            System.out.print(" "+vr.name);
        }
        System.out.print(" Out: ");
        for(Variables vr: liveout){
            System.out.print(" "+vr.name);
        } 
    }
    
    
}