package vaportree;

import vaporprintvisitor.PrinterVisitor;

/**
 * Created by Aaron on 10/29/16.
 */
public class GOTO extends Jump{
    public Label label;

    public GOTO(Label l)
    {
        label = l;
    }
    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
