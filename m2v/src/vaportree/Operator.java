package vaportree;

import vaporprintvisitor.PrinterVisitor;

/**
 * Created by Aaron on 10/28/16.
 */
public class Operator {
    public String toString()
    {
        return "";
    }
    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}









