package vaportree;

import vaporprintvisitor.PrinterVisitor;

/**
 * Created by Aaron on 10/29/16.
 */
public class PrintIntS extends Instr{
    public Operand o;
    public PrintIntS(Operand o)
    {
        this.o = o;
    }
    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
