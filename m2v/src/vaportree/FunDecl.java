package vaportree;
import vaporprintvisitor.PrinterVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;

public class FunDecl {
    public Integer temp_variable_counts = 0;
    public Integer label_counts = 0;
    public Label function_name_label;
    public Vector<VaporIdentifier> parameters = new Vector<VaporIdentifier>();
    public LinkedHashMap<String, Pair<Label, CodeBlock> > labeled_blocks = new LinkedHashMap<String, Pair<Label, CodeBlock> >();

    public HashMap<String, Pair<Label, CodeBlock> > error_handling_blocks = new HashMap<String, Pair<Label, CodeBlock> >();

    public String symbol_table_class_key;
    public String symbol_table_method_key;

    // constructor
    public FunDecl(Label label) {
        function_name_label = label;
    }
    public void accept(PrinterVisitor visitor)
    {
        visitor.visit(this);
    }
}
