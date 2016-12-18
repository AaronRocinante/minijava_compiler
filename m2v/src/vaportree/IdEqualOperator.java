package vaportree;

import vaporprintvisitor.PrinterVisitor;

/**
 * Created by Aaron on 10/29/16.
 */
public class IdEqualOperator extends Instr{
    public VaporIdentifier id;
    public Operator op;

    // constructors
    public IdEqualOperator(){}
    public IdEqualOperator(VaporIdentifier id, Operator op)
    {
        this.id = id;
        this.op = op;
    }

    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
