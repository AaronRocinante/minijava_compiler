package vaportree;

import vaporprintvisitor.PrinterVisitor;

/**
 * Created by Aaron on 10/29/16.
 */
public class Err extends Instr{
    public StringLiteral s = new StringLiteral();

    public Err(String content)
    {
        s.content = content;
    }


    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
