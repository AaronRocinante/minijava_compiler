package vaportree;

import vaporprintvisitor.PrinterVisitor;

/**
 * Created by Aaron on 10/29/16.
 */
public class Ret extends Jump{
    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }

}
