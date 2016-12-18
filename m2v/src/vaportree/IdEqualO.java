package vaportree;

import vaporprintvisitor.PrinterVisitor;

/**
 * Created by Aaron on 10/29/16.
 */
public class IdEqualO extends Instr{
    public VaporIdentifier id;
    public Operand o;

    // constructors
    public IdEqualO(){}
    public IdEqualO(VaporIdentifier id, Operand o)
    {
        this.id = id;
        this.o = o;
    }
    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
