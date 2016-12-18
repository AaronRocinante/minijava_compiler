/**
 * Created by Aaron on 10/14/16.
 */

import java.util.*;
import syntaxtree.*;
import visitor.*;

public class Typecheck extends GJDepthFirst<ComplexType, Table>{
    // classtype -> ClassTable
    static HashMap<String, ClassTable> symbolTable;
    static HashSet<ArrayList<String> > link_set;

    public static void main(String arg[])
    {
        try {
            Goal goal = new MiniJavaParser(System.in).Goal();
            SymbolTable table = new SymbolTable();
            // populate the symbol table
            goal.accept(table, null);
            symbolTable = table.sym_table;
            link_set = table.link_set;
            goal.accept(new Typecheck(), null);
            System.out.println("Program type checked successfully");
        }
        catch (ParseException e){
            System.out.println("Type error");
            System.exit(1);
        }
        catch (TypeException e){
            System.out.println("Type error");
            System.exit(1);
        }
    }

    // true if a is a subtype of b i.e. a is a derived class of b i.e. a <= b
    private boolean subtype(ComplexType a, ComplexType b)
    {
        if(a.toString().equals(b.toString()))
        {
            return true;
        }
        String parent;
        for (ArrayList<String> pair:link_set)
        {
            if(a.toString().equals(pair.get(0))) {
                parent = pair.get(1);
                return subtype(new ComplexType(parent), b);
            }
        }
        return false;
    }
    private boolean method_type_equal(Pair<ArrayList<Pair<String, ComplexType> >, ComplexType> mt1,
                                      Pair<ArrayList<Pair<String, ComplexType> >, ComplexType> mt2)
    {
        ArrayList<Pair<String, ComplexType> >list1 = mt1.first();
        ArrayList<Pair<String, ComplexType> >list2 = mt2.first();
        ComplexType return_type1 = mt1.second();
        ComplexType return_type2 = mt2.second();

        if(list1.size() != list2.size()) {
            return false;
        }
        if(!return_type1.equals(return_type2)){
            return false;
        }

        for (int i=0;i< list1.size();i++)
        {
            if (!list1.get(i).second().equals(list2.get(i).second())) {
                return false;
            }
        }
        return true;
    }
    public Pair<ArrayList<Pair<String, ComplexType> >, ComplexType> method_type(ComplexType class_type, String method_name)
    {
        String class_type_string = class_type.toString();
        String parent = null;
        for(ArrayList<String> list: link_set)
        {
            if(list.get(0).equals(class_type_string))
            {
                parent = list.get(1);
                break;
            }
        }
        ClassTable class_table = symbolTable.get(class_type_string);
        if(class_table == null || class_table.methods.get(method_name) == null)
        {
            if(parent != null)
            {
                return method_type(new ComplexType(parent), method_name);
            }
            return null;
        }
        MethodTable method_table = class_table.methods.get(method_name);
        if(method_table == null)
        {
            if(parent!= null)
            {
                return method_type(new ComplexType(parent), method_name);
            }
            return null;
        }
        return new Pair<ArrayList<Pair<String, ComplexType> >, ComplexType>(
                method_table.formal_params, method_table.return_type);
    }


    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public ComplexType visit(Goal g, Table table) {
        if (!acyclic()) {
            throw new TypeException();
        }
        ClassTable mc_table = symbolTable.get(g.f0.f1.f0.toString());
        g.f0.accept(this, mc_table);
        for(Node node: g.f1.nodes)
        {
            node.accept(this, null);
        }
        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    public ComplexType visit(MainClass n, Table table) {
        MethodTable main_method_table = ((ClassTable) table).methods.get("main");
        for(Node node: n.f15.nodes)
        {
            node.accept(this, main_method_table);
        }
        return null;
    }

    /**
     * f0 -> ClassDeclaration()
     *       | ClassExtendsDeclaration()
     */
    public ComplexType visit(TypeDeclaration n, Table table) {
        n.f0.accept(this, table);
        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public ComplexType visit(ClassDeclaration n, Table table) {
        String class_name = n.f1.f0.toString();
        ClassTable class_table = symbolTable.get(class_name);
        for(Node node: n.f4.nodes)
        {
            node.accept(this, class_table);
        }
        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    public ComplexType visit(ClassExtendsDeclaration n, Table table) {
        String class_name = n.f1.f0.toString();
        String parent = n.f3.f0.toString();
        ClassTable class_table = symbolTable.get(class_name);
        ClassTable parent_table = symbolTable.get(parent);
        for(Node node: n.f6.nodes)
        {
            String method_name = ((MethodDeclaration)node).f2.f0.toString();
            // check no overloading
            if(parent_table.methods.get(method_name) != null &&
                    !method_type_equal(method_type(new ComplexType(class_name), method_name),
                    method_type(new ComplexType(parent), method_name)))
            {
                throw new TypeException();
            }
            node.accept(this, class_table);
        }
        return null;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    public ComplexType visit(MethodDeclaration n, Table table) {
        MethodTable method_table = ((ClassTable)table).methods.get(n.f2.f0.toString());
        for(Node node: n.f8.nodes)
        {
            node.accept(this, method_table);
        }
        ComplexType t1 = n.f10.accept(this, method_table);
        ComplexType t2 = n.f1.accept(new GetNonExpressionTypeVisitor());
        if(!t1.equals(t2))
        {
            throw new TypeException();
        }
        return null;
    }

    /**
     * f0 -> AndExpression()
     *       | CompareExpression()
     *       | PlusExpression()
     *       | MinusExpression()
     *       | TimesExpression()
     *       | ArrayLookup()
     *       | ArrayLength()
     *       | MessageSend()
     *       | PrimaryExpression()
     */
    public ComplexType visit(Expression n, Table table) {
        return n.f0.accept(this, table);
    }

    /**
     * f0 -> Expression()
     * f1 -> ( ExpressionRest() )*
     */
    public ComplexType visit(ExpressionList n, Table table) {
        ComplexType t;
        t = n.f0.accept(this, table);
        for(Node node: n.f1.nodes)
        {
            t = node.accept(this, table);
        }
        return t;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public ComplexType visit(ExpressionRest n, Table table) {
        return n.f1.accept(this, table);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "&&"
     * f2 -> PrimaryExpression()
     */
    public ComplexType visit(AndExpression n, Table table) {
        if(n.f0.accept(this, table) != ComplexType.BOO || n.f2.accept(this, table)!=ComplexType.BOO)
        {
            throw new TypeException();
        }
        return ComplexType.BOO;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public ComplexType visit(CompareExpression n, Table table) {
        if(n.f0.accept(this,table)!=ComplexType.INT || n.f2.accept(this, table)!=ComplexType.INT)
        {
            throw new TypeException();
        }
        return ComplexType.BOO;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public ComplexType visit(PlusExpression n, Table table){
        if(n.f0.accept(this,table)!=ComplexType.INT || n.f2.accept(this,table)!=ComplexType.INT)
        {
            throw new TypeException();
        }
        return ComplexType.INT;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public ComplexType visit(MinusExpression n, Table table) {
        if(n.f0.accept(this,table)!=ComplexType.INT || n.f2.accept(this,table)!=ComplexType.INT)
        {
            throw new TypeException();
        }
        return ComplexType.INT;
    }

    /** (36)
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public ComplexType visit(TimesExpression n, Table table) {
        if(n.f0.accept(this,table)!=ComplexType.INT || n.f2.accept(this,table)!=ComplexType.INT)
        {
            throw new TypeException();
        }
        return ComplexType.INT;
    }

    /** (37)
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public ComplexType visit(ArrayLookup n, Table table) {
        if(n.f0.accept(this, table)!=ComplexType.IARR || n.f2.accept(this, table)!=ComplexType.INT)
        {
            throw new TypeException();
        }
        return ComplexType.INT;
    }

    /** (38)
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public ComplexType visit(ArrayLength n, Table table) {
        ComplexType t = n.f0.accept(this, table);
        if(t != ComplexType.IARR)
        {
            throw new TypeException();
        }
        return ComplexType.INT;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public ComplexType visit(MessageSend n, Table table) {
        ComplexType class_type_D = n.f0.accept(this, table);
        String method_name = n.f2.f0.toString();
        if(class_type_D == null || class_type_D == ComplexType.BOO ||
                class_type_D == ComplexType.INT || class_type_D == ComplexType.IARR)
        {
            throw new TypeException();
        }
        Pair<ArrayList<Pair<String, ComplexType> >, ComplexType> m_type = method_type(class_type_D, method_name);
        if (m_type == null)
        {
            throw new TypeException();
        }
        ArrayList<Pair<String, ComplexType> > formal_params = m_type.first();
        ComplexType return_type = m_type.second();
        int size = formal_params.size();
        if(!n.f4.present())
        {
            if (size != 0) {
                throw new TypeException();
            }
            return return_type;
        }
        // otherwise a node is present
        zipperVisitor zipper = new zipperVisitor(size, link_set, table);
        n.f4.node.accept(zipper, formal_params);
        return return_type;
    }

    /**
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | NotExpression()
     *       | BracketExpression()
     */
    public ComplexType visit(PrimaryExpression n, Table table) {
        return n.f0.accept(this,table);
    }

    /** (40)
     * f0 -> <INTEGER_LITERAL>
     */
    public ComplexType visit(IntegerLiteral n, Table table) {
        return ComplexType.INT;
    }

    /** (41)
     * f0 -> "true"
     */
    public ComplexType visit(TrueLiteral n, Table table) {
        return ComplexType.BOO;
    }

    /** (42)
     * f0 -> "false"
     */
    public ComplexType visit(FalseLiteral n, Table table) {
        return ComplexType.BOO;
    }

    /** (43)
     * f0 -> <IDENTIFIER>
     */
    public ComplexType visit(Identifier n, Table table) {
        ComplexType t;
        String id = n.f0.toString();
        if(table instanceof MethodTable)
        {
            t = ((MethodTable) table).variables.get(id);
            if (t == null)
            {
                t = ((MethodTable) table).fields.get(id);
            }
            if (t == null)
            {
                if(symbolTable.get(id) == null)
                {
                    throw new TypeException();
                }
                t = new ComplexType(id);
            }
        }
        else if(table instanceof ClassTable)
        {
            t = ((ClassTable)table).fields.get(id);
            if(t == null)
            {
                if(symbolTable.get(id) == null)
                {
                    throw new TypeException();
                }
                t = new ComplexType(id);
            }
        }
        else
        {
            throw new TypeException();
        }
        return t;
    }

    /** (44)
     * f0 -> "this"
     */
    public ComplexType visit(ThisExpression n, Table table) {
        if (table instanceof ClassTable)
        {
            return ((ClassTable) table).class_type;
        }
        else if (table instanceof MethodTable)
        {
            return ((MethodTable) table).class_type;
        }
        else
        {
            throw new TypeException();
        }
    }

    /** (45)
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public ComplexType visit(ArrayAllocationExpression n, Table table) {
        ComplexType t = n.f3.accept(this, table);
        if(t!=ComplexType.INT)
        {
            throw new TypeException();
        }
        return ComplexType.IARR;
    }

    /** (46)
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public ComplexType visit(AllocationExpression n, Table table) {
        String id = n.f1.f0.toString();
        if(symbolTable.get(id) == null)
        {
            throw new TypeException();
        }
        return new ComplexType(id);
    }

    /** (47)
     * f0 -> "!"
     * f1 -> Expression()
     */
    public ComplexType visit(NotExpression n, Table table) {
        ComplexType t = n.f1.accept(this, table);
        if(t != ComplexType.BOO)
        {
            throw new TypeException();
        }
        return ComplexType.BOO;
    }

    /** (48)
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public ComplexType visit(BracketExpression n, Table table) {
        return n.f1.accept(this, table);
    }

    /**
     * f0 -> Block()
     *       | AssignmentStatement()
     *       | ArrayAssignmentStatement()
     *       | IfStatement()
     *       | WhileStatement()
     *       | PrintStatement()
     */
    public ComplexType visit(Statement n,  Table table) {
        return n.f0.accept(this, table);
    }

    /** (26)
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
    public ComplexType visit(Block n, Table table) {
        for(Node node: n.f1.nodes) {
            node.accept(this, table);
        }
        return null;
    }

    /** (27)
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public ComplexType visit(AssignmentStatement n, Table table){
        String id = n.f0.f0.toString();
        ComplexType t1;
        if (table instanceof MethodTable)
        {
            t1 = ((MethodTable) table).variables.get(id);
            if (t1 == null)
            {
                t1 = ((MethodTable) table).fields.get(id);
            }
        }
        else if(table instanceof ClassTable)
        {
            t1 = ((ClassTable) table).fields.get(id);
        }
        else
        {
            throw new TypeException();
        }
        ComplexType t2 = n.f2.accept(this, table);
        if(t2 == null || t1 == null || !subtype(t2,t1)) {
            throw new TypeException();
        }
        return null;
    }

    /** (28)
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    public ComplexType visit(ArrayAssignmentStatement n, Table table) {
        String id = n.f0.f0.toString();
        ComplexType t1;
        if (table instanceof MethodTable)
        {
            t1 = ((MethodTable) table).variables.get(id);
            if (t1==null)
            {
                t1 = ((MethodTable) table).fields.get(id);
            }
        }
        else if(table instanceof ClassTable)
        {
            t1 = ((ClassTable) table).fields.get(id);
        }
        else
        {
            throw new TypeException();
        }
        ComplexType t2 = n.f2.accept(this, table);
        ComplexType t3 = n.f5.accept(this, table);
        if (t1 == null || t1 != ComplexType.IARR || t2 != ComplexType.INT || t3 != ComplexType.INT) {
            throw new TypeException();
        }
        return null;
    }

    /** (29)
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    public ComplexType visit(IfStatement n, Table table) {
        ComplexType t = n.f2.accept(this, table);
        n.f4.accept(this, table);
        n.f6.accept(this, table);
        if(t != ComplexType.BOO ) {
            throw new TypeException();
        }
        return null;
    }

    /** (30)
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public ComplexType visit(WhileStatement n, Table table) {
        ComplexType t = n.f2.accept(this, table);
        n.f4.accept(this, table);
        if(t != ComplexType.BOO) {
            throw new TypeException();
        }
        return null;
    }

    /** (31)
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    public ComplexType visit(PrintStatement n, Table table){
        ComplexType t = n.f2.accept(this, table);
        if(t != ComplexType.INT) {
            throw new TypeException();
        }
        return null;
    }

    private boolean acyclic()
    {
        int size = link_set.size();
        for (ArrayList<String> pair:link_set)
        {
            int counter = 0;
            String child = pair.get(0);
            String parent = pair.get(1);
            for(ArrayList<String> pair_two:link_set)
            {
                if (counter > size)
                {
                    throw new TypeException();
                }
                if (!parent.equals(pair_two.get(0)))
                {
                    counter++;
                    continue;
                }
                parent = pair_two.get(1);
                if (parent.equals(child))
                {
                    throw new TypeException();
                }
                counter++;
            }
        }
        return true;
    }
}


class ComplexType
{
    final String name;

    public ComplexType(String name)
    {
        this.name = name;
    }
    public String toString()
    {
        return name;
    }
    public boolean equals(Object o)
    {
        if (o!= null && o instanceof ComplexType)
        {
            return name.equals(((ComplexType)o).name);
        }
        else
        {
            return false;
        }
    }
    // int
    static final ComplexType INT = new ComplexType("INT");
    //boolean
    static final ComplexType BOO = new ComplexType("BOO");
    // int[]
    static final ComplexType IARR = new ComplexType("IARR");

    public boolean is_primitive_type ()
    {
        return (this.equals(INT) || this.equals(BOO) || this.equals(IARR));
    }

}

class TypeException extends RuntimeException
{

}

// used to check rule (39)
class zipperVisitor extends GJVoidDepthFirst<ArrayList<Pair<String, ComplexType> > >
{
    int num_params;
    int counter;
    HashSet<ArrayList<String> > link_set;
    Table table;
    public zipperVisitor(int num_params, HashSet<ArrayList<String> > link_set, Table table)
    {
        this.num_params = num_params;
        this.link_set = link_set;
        this.table = table;
        counter = 0;
    }

    // true if a is a subtype of b i.e. a is a derived class of b i.e. a <= b
    private boolean subtype(ComplexType a, ComplexType b)
    {
        if(a.toString().equals(b.toString()))
        {
            return true;
        }
        String parent;
        for (ArrayList<String> pair:link_set)
        {
            if(a.toString().equals(pair.get(0))) {
                parent = pair.get(1);
                return subtype(new ComplexType(parent), b);
            }
        }
        return false;
    }

    /**
     * f0 -> Expression()
     * f1 -> ( ExpressionRest() )*
     */
    public void visit (ExpressionList n, ArrayList<Pair<String, ComplexType>> formal_params)
    {
        n.f0.accept(this, formal_params);
        counter++;
        for(Node node: n.f1.nodes)
        {
            node.accept(this, formal_params);
            counter++;
        }
        if (counter != formal_params.size())
        {
            throw new TypeException();
        }
    }

    /**
     * f0 -> AndExpression()
     *       | CompareExpression()
     *       | PlusExpression()
     *       | MinusExpression()
     *       | TimesExpression()
     *       | ArrayLookup()
     *       | ArrayLength()
     *       | MessageSend()
     *       | PrimaryExpression()
     */
    public void visit (Expression n, ArrayList<Pair<String, ComplexType>> formal_params)
    {
        if(counter >= formal_params.size())
        {
            throw new TypeException();
        }
        ComplexType t = n.f0.accept(new GetExpressionTypeVisitor(table));
        if(!subtype(t, formal_params.get(counter).second()))
        {
            throw new TypeException();
        }
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public void visit(ExpressionRest n, ArrayList<Pair<String, ComplexType> > formal_params) {
        n.f1.accept(this, formal_params);
    }
}

class GetExpressionTypeVisitor extends GJNoArguDepthFirst<ComplexType>
{
    final Table table;
    public GetExpressionTypeVisitor(Table table)
    {
        this.table = table;
    }
    public ComplexType visit(Type t)
    {
        return t.f0.accept(this);
    }

    public ComplexType visit(ArrayType t)
    {
        return ComplexType.IARR;
    }

    public ComplexType visit(BooleanType t)
    {
        return ComplexType.BOO;
    }

    public ComplexType visit(IntegerType t)
    {
        return ComplexType.INT;
    }

    public ComplexType visit(Identifier n)
    {
        String id = n.f0.toString();
        ComplexType t;
        if(table instanceof MethodTable)
        {
            t = ((MethodTable) table).variables.get(id);
            if (t==null)
            {
                t = ((MethodTable) table).fields.get(id);
            }
            if (t==null)
            {
                throw new TypeException();
            }

        }
        else if (table instanceof ClassTable)
        {
            t = ((ClassTable) table).fields.get(id);
            if(t == null)
            {
                throw new TypeException();
            }
        }
        else
        {
            throw new TypeException();
        }

        return t;
    }

    /**
     * f0 -> AndExpression()
     *       | CompareExpression()
     *       | PlusExpression()
     *       | MinusExpression()
     *       | TimesExpression()
     *       | ArrayLookup()
     *       | ArrayLength()
     *       | MessageSend()
     *       | PrimaryExpression()
     */
    public ComplexType visit(Expression n) {
        return n.f0.accept(this);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "&&"
     * f2 -> PrimaryExpression()
     */
    public ComplexType visit(AndExpression n) {
        return ComplexType.BOO;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public ComplexType visit(CompareExpression n) {
        return ComplexType.BOO;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public ComplexType visit(PlusExpression n) {
        return ComplexType.INT;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public ComplexType visit(MinusExpression n) {
        return ComplexType.INT;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public ComplexType visit(TimesExpression n) {
        return ComplexType.INT;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public ComplexType visit(ArrayLookup n) {
        return ComplexType.INT;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public ComplexType visit(ArrayLength n) {
        return ComplexType.INT;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public ComplexType visit(MessageSend n) {
        HashMap<String, ClassTable> symbolTable = Typecheck.symbolTable;
        ComplexType class_type = n.f0.accept(new ClassTypeFromPrimaryExpressionVisitor(table));
        ClassTable class_table = symbolTable.get(class_type.toString());
        String method_name = n.f2.f0.toString();
        ComplexType return_type = class_table.methods.get(method_name).return_type;
        return return_type;
    }

    /**
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | NotExpression()
     *       | BracketExpression()
     */
    public ComplexType visit(PrimaryExpression n) {
        return n.f0.accept(this);
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public ComplexType visit(IntegerLiteral n) {
        return ComplexType.INT;
    }

    /**
     * f0 -> "true"
     */
    public ComplexType visit(TrueLiteral n) {
        return ComplexType.BOO;
    }

    /**
     * f0 -> "false"
     */
    public ComplexType visit(FalseLiteral n) {
        return ComplexType.BOO;
    }

    /**
     * f0 -> "this"
     */
    public ComplexType visit(ThisExpression n) {
        if (table instanceof ClassTable)
        {
            return ((ClassTable) table).class_type;
        }
        else if (table instanceof MethodTable)
        {
            return ((MethodTable) table).class_type;
        }
        else
        {
            return null;
        }
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public ComplexType visit(ArrayAllocationExpression n) {
        return ComplexType.IARR;
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public ComplexType visit(AllocationExpression n) {
        HashMap<String, ClassTable> symbolTable = Typecheck.symbolTable;
        String id = n.f1.f0.toString();
        if(symbolTable.get(id)==null)
        {
            throw new TypeException();
        }
        return new ComplexType(id);
    }

    /**
     * f0 -> "!"
     * f1 -> Expression()
     */
    public ComplexType visit(NotExpression n) {
        return ComplexType.BOO;
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public ComplexType visit(BracketExpression n) {
        return n.f1.accept(this);
    }
}

class ClassTypeFromPrimaryExpressionVisitor extends GJNoArguDepthFirst<ComplexType>
{
    final Table table;
    public ClassTypeFromPrimaryExpressionVisitor(Table table)
    {
        this.table = table;
    }
    /**
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | NotExpression()
     *       | BracketExpression()
     */
    public ComplexType visit(PrimaryExpression n) {
        return n.f0.accept(this);
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public ComplexType visit(IntegerLiteral n) {
        return null;
    }

    /**
     * f0 -> "true"
     */
    public ComplexType visit(TrueLiteral n) {
        return null;
    }

    /**
     * f0 -> "false"
     */
    public ComplexType visit(FalseLiteral n) {
        return null;
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public ComplexType visit(Identifier n)
    {
        String id = n.f0.toString();
        ComplexType t;
        if (table instanceof MethodTable)
        {
            t = ((MethodTable) table).variables.get(id);
            if (t != null && !t.is_primitive_type())
            {
                return ((MethodTable) table).class_type;
            }
            // else t == null
            t = ((MethodTable) table).fields.get(id);
            if (t != null && !t.is_primitive_type())
            {
                return ((MethodTable) table).class_type;
            }
            // else t == null i.e. the id isn't
        }
        return null;
    }

    /**
     * f0 -> "this"
     */
    public ComplexType visit(ThisExpression n) {
        if (table instanceof ClassTable)
        {
            return ((ClassTable) table).class_type;
        }
        else if (table instanceof MethodTable)
        {
            return ((MethodTable) table).class_type;
        }
        else
        {
            return null;
        }
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public ComplexType visit(ArrayAllocationExpression n) {
        return null;
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public ComplexType visit(AllocationExpression n) {
        HashMap<String, ClassTable> symbolTable = Typecheck.symbolTable;
        String id = n.f1.f0.toString();
        // id has to refer to a class type
        if(symbolTable.get(id)==null)
        {
            throw new TypeException();
        }
        return symbolTable.get(id).class_type;
    }

    /**
     * f0 -> "!"
     * f1 -> Expression()
     */
    public ComplexType visit(NotExpression n) {
        return null;
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public ComplexType visit(BracketExpression n) {
        return n.f1.accept(new GetExpressionTypeVisitor(table));
    }
}