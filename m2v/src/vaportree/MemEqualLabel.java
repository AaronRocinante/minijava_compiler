package vaportree;

import vaporprintvisitor.PrinterVisitor;

/**
 * Created by Aaron on 11/5/16.
 */
public class MemEqualLabel extends Instr {
    public MemRef mem_ref;
    public Label label;
    public MemEqualLabel(MemRef m, Label l)
    {
        mem_ref = m;
        label = l;
    }

    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
