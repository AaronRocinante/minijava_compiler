package vaportree;

import vaporprintvisitor.PrinterVisitor;

/**
 * Created by Aaron on 10/29/16.
 */
public class If0_goto extends Instr{
    public Operand o;
    public Label l;

    public If0_goto(Operand o, Label l)
    {
        this.o = o;
        this.l = l;
    }

    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
