/**
 * Created by Aaron on 10/30/16.
 */
import syntaxtree.*;
import visitor.*;

public class GetNonExpressionTypeVisitor extends GJNoArguDepthFirst<ComplexType>
{

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

    public ComplexType visit(Identifier id)
    {
        return new ComplexType(id.f0.toString());
    }
}
