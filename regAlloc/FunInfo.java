package regAlloc;

import java.util.*;


public class FunInfo{
    public String name;
    public ArrayList<Blocks> blocklist;
    public boolean doescall;
    public int noArgs, toSpill, maxCallArgs, stkSlots;

    public FunInfo(String nam){
        name = nam;
        blocklist = new ArrayList<Blocks>();
        doescall=false;
        noArgs = toSpill = maxCallArgs = stkSlots = 0; 
    }
}