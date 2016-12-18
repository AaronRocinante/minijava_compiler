import syntaxtree.*;
import visitor.*;
import java.util.*;

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
