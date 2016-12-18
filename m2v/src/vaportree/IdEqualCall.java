package vaportree;

import vaporprintvisitor.PrinterVisitor;

import java.util.Vector;

/**
 * Created by Aaron on 10/29/16.
 */
public class IdEqualCall extends Instr{
    public VaporIdentifier id;
    public Operand o;
    public Vector<Operand> arguments = new Vector<Operand>();
    public IdEqualCall(VaporIdentifier id, Operand o)
    {
        this.id = id;
        this.o = o;
    }
    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
