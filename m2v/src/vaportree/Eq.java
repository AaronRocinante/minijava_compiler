package vaportree;

import vaporprintvisitor.PrinterVisitor;

/**
 * Created by Aaron on 10/29/16.
 */
public class Eq extends Operator{
    public Operand lhs;
    public Operand rhs;
    public Eq(Operand o1, Operand o2)
    {
        lhs = o1;
        rhs = o2;
    }
    public String toString()
    {
        return "Eq";
    }
    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
