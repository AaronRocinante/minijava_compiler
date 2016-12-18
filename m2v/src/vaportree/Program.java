package vaportree;
import vaporprintvisitor.*;
import java.util.*;

/**
 * Created by Aaron on 10/28/16.
 */
public class Program {
    public Vector<ConstDecl> constant_declarations = new Vector<ConstDecl>();
    public LinkedHashMap<String, FunDecl> function_declarations = new LinkedHashMap<String, FunDecl>();

    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
