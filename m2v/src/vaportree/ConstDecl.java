package vaportree;

import vaporprintvisitor.PrinterVisitor;

import java.util.LinkedHashSet;
import java.util.Vector;

/**
 * Created by Aaron on 10/28/16.
 */
public class ConstDecl {
    public Label name;
    public LinkedHashSet<Label> labels = new LinkedHashSet<Label>();
    public ConstDecl(Label name)
    {
        this.name = name;
    }
    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
