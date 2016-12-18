import vaportree.*;
import visitor.*;
import syntaxtree.*;

/**
 * Created by Aaron on 10/30/16.
 */

public class VaporArrayVisitor extends GJDepthFirst<VaporIdentifier, CodeBlock> {
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
    public VaporIdentifier visit(PrimaryExpression n, CodeBlock code_block) {
        return n.f0.accept(this, code_block);
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public VaporIdentifier visit(Identifier n, CodeBlock code_block) {
        String id_string = n.f0.toString();
        VaporIdentifier o = new VaporIdentifier(id_string);
        return o;
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
//    public VaporIdentifier visit(ArrayAllocationExpression n, CodeBlock code_block) {
//    }
}
