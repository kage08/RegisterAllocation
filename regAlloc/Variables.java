package regAlloc;

import java.util.Comparator;





public class Variables implements Comparable<Variables>{
    public int size, start, end;
    public int name;
    public FunInfo proc;
    public int regnum;
    public char regtype; 

    public Variables(int nm, FunInfo pr){
        name = nm;
        proc = pr;
        start = Integer.MAX_VALUE;
        end = Integer.MIN_VALUE;
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

    public static Comparator<Variables> endComp(){
        return new Comparator<Variables>(){
            public int compare(Variables one, Variables two){
                return one.end - two.end;
            }
        };
    }

    public static Comparator<Variables> startComp(){
        return new Comparator<Variables>(){
            public int compare(Variables one, Variables two){
                return one.start - two.start;
            }
        };
    }
}