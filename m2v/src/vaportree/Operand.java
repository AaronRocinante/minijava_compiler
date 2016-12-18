package vaportree;


import vaporprintvisitor.PrinterVisitor;

import java.util.HashMap;

/**
 * Created by Aaron on 10/28/16.
 */
public class Operand {

    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
