import syntaxtree.*;
import vaportree.CodeBlock;
import visitor.GJDepthFirst;

import java.util.HashMap;

/**
 * Created by Aaron on 11/5/16.
 */
public class VaporClassTypeFromPrimaryExpVisitor extends GJDepthFirst<ComplexType, CodeBlock> {
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
    public ComplexType visit(Expression n, CodeBlock code_block) {
        return n.f0.accept(this, code_block);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public ComplexType visit(MessageSend n, CodeBlock code_block) {
        ComplexType second_class_type = n.f0.accept(new VaporClassTypeFromPrimaryExpVisitor(), code_block);
        ClassTable second_class_table = VaporProgramBuilder.symbolTable.get(second_class_type.toString());
        return second_class_table.total_methods.get(n.f2.f0.toString()).return_type;
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
    public ComplexType visit(PrimaryExpression n, CodeBlock code_block) {
        return n.f0.accept(this,code_block);
    }
    /**
     * f0 -> <IDENTIFIER>
     */
    public ComplexType visit(Identifier n, CodeBlock code_block)
    {
        String class_name = code_block.symbol_table_class_key;
        String method_name = code_block.symbol_table_method_key;
        ClassTable class_table = VaporProgramBuilder.symbolTable.get(class_name);

        ComplexType containing_class_type = class_table.retrieve_class_containing_method(method_name,VaporProgramBuilder.symbolTable);
        ClassTable containing_class_table = VaporProgramBuilder.symbolTable.get(containing_class_type.toString());
        MethodTable method_table = containing_class_table.methods.get(method_name);
//        MethodTable method_table = class_table.total_methods.get(method_name);

        String id = n.f0.toString();
        return method_table.retrieve_type(id, VaporProgramBuilder.symbolTable);
    }

    public ComplexType visit(ThisExpression n, CodeBlock code_block)
    {
        String class_name = code_block.symbol_table_class_key;
        return new ComplexType(class_name);
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public ComplexType visit(AllocationExpression n, CodeBlock code_block) {
        String id = n.f1.f0.toString();
        return new ComplexType(id);
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public ComplexType visit(BracketExpression n, CodeBlock code_block) {
        return n.f1.accept(this, code_block);
    }
}
