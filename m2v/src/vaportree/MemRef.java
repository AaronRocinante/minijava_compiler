package vaportree;

import vaporprintvisitor.PrinterVisitor;

/**
 * Created by Aaron on 10/28/16.
 */
public class MemRef {
    public VaporIdentifier id;
    public VaporIntegerLiteral c;

    public MemRef(){}
    public MemRef(VaporIdentifier id, VaporIntegerLiteral c)
    {
        this.id = id;
        this.c = c;
    }

    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
