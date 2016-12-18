package vaportree;

import vaporprintvisitor.PrinterVisitor;

/**
 * Created by Aaron on 10/28/16.
 */
public class VaporIdentifier extends Operand{
    public String id;

    public String toString()
    {
        return id;
    }
    public VaporIdentifier(){}
    public VaporIdentifier(String id){
        this.id = id;
    }

    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
