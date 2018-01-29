package regAlloc;

import java.util.Comparator;





public class Variables implements Comparable<Variables>{
    public int size, start, end;
    public int name;
    public FunInfo proc;
    public int regnum;
    public char regtype;
    public boolean spilled; 
    public int spillnumber;

    public Variables(int nm, FunInfo pr){
        name = nm;
        proc = pr;
        start = Integer.MAX_VALUE;
        end = Integer.MIN_VALUE;
        spilled = false;
        spillnumber = 0;
    }

    public int compareTo (Variables other){
        return this.toString().compareTo(other.toString());
    }

    public void updatestart(int i){
        if(i<start)
            start = i;
    }

    public void updateend(int i){
        if(i>end)
            end = i;
    }
    public void updatesize(){
        size = end - start + 1;
    }
    public String regName(){
        String ans = Character.toString(regtype);
        ans = ans+Integer.toString(regnum);
        return ans;
    }

    
}