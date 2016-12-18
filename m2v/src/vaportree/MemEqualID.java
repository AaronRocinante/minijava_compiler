package vaportree;

import vaporprintvisitor.PrinterVisitor;

/**
 * Created by Aaron on 10/29/16.
 */
public class MemEqualID extends Instr{
    public MemRef m;
    public VaporIdentifier id;

    public MemEqualID(MemRef memref, VaporIdentifier id)
    {
        this.m = memref;
        this.id = id;
    }
    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
