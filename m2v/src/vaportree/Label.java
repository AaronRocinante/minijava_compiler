package vaportree;

import vaporprintvisitor.PrinterVisitor;

/**
 * Created by Aaron on 10/28/16.
 */
public class Label extends Operand {

    public String label;
    public Label(String name){
        label = name;
    }
    public String toString()
    {
        return label;
    }
    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
