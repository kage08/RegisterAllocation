package regAlloc;




public class Variables{
    public int size, start, end;
    public int name;
    public FunInfo proc;
    public int regnum;
    public char regtype; 

    public Variables(int nm, FunInfo pr){
        name = nm;
        proc = pr;
    }
}