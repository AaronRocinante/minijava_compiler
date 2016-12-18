package vaportree;

import vaporprintvisitor.PrinterVisitor;

/**
 * Created by Aaron on 10/29/16.
 */
public class IdEqualMem extends Instr{
    public VaporIdentifier id;
    public MemRef m;

    public IdEqualMem(){}
    public IdEqualMem(VaporIdentifier id, MemRef mem_addr)
    {
        this.id = id;
        this.m = mem_addr;
    }

    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
