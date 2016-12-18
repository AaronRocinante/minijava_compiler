package vaportree;

import vaporprintvisitor.PrinterVisitor;

public class VaporIntegerLiteral extends Operand {
    public String int_string;
    public VaporIntegerLiteral(){}
    public VaporIntegerLiteral(String string)
    {
        int_string = string;
    }

    public Integer toInt()
    {
        return Integer.valueOf(int_string);
    }

    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }

}
