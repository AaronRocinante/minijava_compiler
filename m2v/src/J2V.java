/**
 * Created by Aaron on 10/28/16.
 */
import syntaxtree.*;
import vaporprintvisitor.PrinterVisitor;
import vaportree.*;
import visitor.*;
import java.util.*;

public class J2V extends DepthFirstVisitor{
    static HashMap<String, ClassTable> symbolTable;
    static HashSet<ArrayList<String> > link_set;
    public static void main(String[] arg) {
        try {
            Goal goal = new MiniJavaParser(System.in).Goal();
            SymbolTable table = new SymbolTable();
            goal.accept(table,null);
            symbolTable = table.sym_table;
            link_set = table.link_set;

            VaporProgramBuilder builder = new VaporProgramBuilder(symbolTable, link_set);
            goal.accept(builder,null);
            Program vapor_tree = builder.vapor_tree;
            vapor_tree.accept(new PrinterVisitor());

        } catch (ParseException e) {
            System.out.println("Parse Exception");
            System.exit(1);
        }

    }

}










