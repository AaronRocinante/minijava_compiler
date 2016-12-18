import syntaxtree.*;
import visitor.*;
import java.util.*;

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
