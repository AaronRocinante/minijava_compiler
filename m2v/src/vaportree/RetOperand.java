package vaportree;

import vaporprintvisitor.PrinterVisitor;

/**
 * Created by Aaron on 10/29/16.
 */
public class RetOperand extends Jump{

    public Operand o;
    public RetOperand(Operand target)
    {
        o = target;
    }
    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
