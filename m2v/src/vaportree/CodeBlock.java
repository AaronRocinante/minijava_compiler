package vaportree;
import vaporprintvisitor.PrinterVisitor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

public class CodeBlock {
    public LinkedList<Instr> current_instruction = new LinkedList<Instr>();
    public Vector<Instr> instructions = new Vector<Instr>();
    public Jump jump;
    public FunDecl fun_decl;
    public String symbol_table_class_key;
    public String symbol_table_method_key;

    public CodeBlock(FunDecl fd)
    {
        fun_decl = fd;
        this.symbol_table_class_key = fd.symbol_table_class_key;
        this.symbol_table_method_key = fd.symbol_table_method_key;
    }

    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
