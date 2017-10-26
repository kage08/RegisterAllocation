package regAlloc;

import java.util.*;

public class Blocks {

    public ArrayList<Blocks> succ;
    public ArrayList<Blocks> pred;
    public ArrayList<Variables> livein;
    public ArrayList<Variables> liveout;
    public ArrayList<Variables> def;
    public ArrayList<Variables> use;


    public Blocks(){
        succ = new ArrayList<Blocks>();
        pred = new ArrayList<Blocks>();
        livein = new ArrayList<Variables>();
        liveout = new ArrayList<Variables>();
        def = new ArrayList<Variables>();
        use = new ArrayList<Variables>();
    }
    
}