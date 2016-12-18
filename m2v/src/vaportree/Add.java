package vaportree;

import vaporprintvisitor.PrinterVisitor;

/**
 * Created by Aaron on 10/29/16.
 */
public class Add extends Operator{
    public Operand lhs;
    public Operand rhs;
    public String toString()
    {
        return "Add";
    }

    // constructors
    public Add(){}
    public Add(Operand lhs, Operand rhs)
    {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
