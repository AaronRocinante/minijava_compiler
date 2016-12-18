package vaportree;

import vaporprintvisitor.PrinterVisitor;

/**
 * Created by Aaron on 10/29/16.
 */
public class LtS extends Operator{
    public Operand o1;
    public Operand o2;

    public LtS(){}
    public LtS(Operand o1, Operand o2){
        this.o1 = o1;
        this.o2 = o2;
    }


    public String toString()
    {
        return "LtS";
    }
    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
