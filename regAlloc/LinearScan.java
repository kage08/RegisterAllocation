package regAlloc;

import java.util.*;

public class LinearScan{
    FunInfo fun;
    TreeSet<Variables> ranges;
    TreeSet<Variables> active;
    ArrayList<Boolean> s_assigned;
    ArrayList<Boolean> t_assigned;
    int maxreg = 18;
    int currreg = 0;


    public LinearScan(FunInfo f){
        fun = f;
        startComp st = new startComp();
        endComp ed = new endComp();
        ranges = new TreeSet<Variables>(st);
        active = new TreeSet<Variables>(ed);
        ranges.addAll(f.ranges);
        s_assigned = new ArrayList<Boolean>(Collections.nCopies(8, false));
        t_assigned = new ArrayList<Boolean>(Collections.nCopies(10, false));


    }

    public void lscan(){
        while(!ranges.isEmpty()){
            
            expire();
            if(currreg == maxreg){
                spill();
            }
            else{
                assignreg(ranges.first());
                currreg++;
                active.add(ranges.first());
                ranges.pollFirst();
                
            }


        }
    }

    void assignreg(Variables var){
        for(int i=0; i<8; i++){
            if(s_assigned.get(i)==false){
                var.regnum = i;
                var.regtype = 's';
                s_assigned.set(i,true);
                fun.regUsed.add(var.regName());
                return;
            }
        }
        for(int i=0; i<10; i++){
            if(t_assigned.get(i)==false){
                var.regnum = i;
                var.regtype = 't';
                t_assigned.set(i,true);
                fun.regUsed.add(var.regName());
                return;
            }
        }
        
    }

    public void expire(){
        if(active.isEmpty()) return;

        Variables curr = ranges.first();

        while(!active.isEmpty()){
            if(active.first().end >= curr.start)
                return;
            freereg(active.first());
            active.pollFirst();
            
        }

    }

    void freereg (Variables v){
        if(v== null) return;
        char rtype = v.regtype;
        int rnum = v.regnum;
        switch(rtype){
            case 's': s_assigned.set(rnum,false); break;
            case 't': t_assigned.set(rnum, false); break;
        }
        currreg-- ;
    }

    void spill(){
        int res = fun.toSpill++;
        Variables last = getlast(active);
        if(last.end > ranges.first().end){
            ranges.first().regnum = last.regnum;
            last.spilled = true;
            last.spillnumber = res;
            fun.spilled.add(last);
            ranges.first().regtype = last.regtype;
            active.add(ranges.first());
            ranges.pollFirst();
        }
        else{
            ranges.first().spilled = true;
            ranges.first().spillnumber = res;
            fun.spilled.add(ranges.first());
            ranges.pollFirst();
            active.add(last);
        }

    }

    Variables getlast (TreeSet<Variables> pq){
        return pq.pollLast();
    }



}

class endComp implements Comparator<Object>{
    @Override
    public int compare(Object one1, Object two1){
        Variables one = (Variables)one1;
        Variables two = (Variables)two1;
        if(one.end==two.end)
            return one.name - two.name;
        return one.end - two.end;
    }
}

class startComp implements Comparator<Object>{
    @Override
    public int compare(Object one1, Object two1){
        Variables one = (Variables)one1;
        Variables two = (Variables)two1;
        if(one.start==two.start)
            return one.name - two.name;
        return one.start - two.start;
    }
    
}