package vaportree;

import vaporprintvisitor.PrinterVisitor;

/**
 * Created by Aaron on 10/29/16.
 */
public class MulS extends Operator{
    public Operand lhs;
    public Operand rhs;
    public String toString()
    {
        return "MulS";
    }

    // constructors
    public MulS() {}
    public MulS(Operand lhs, Operand rhs)
    {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
