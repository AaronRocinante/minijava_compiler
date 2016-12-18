/**
 * Created by Aaron on 10/30/16.
 */
import visitor.*;
import syntaxtree.*;
import vaportree.*;

public class VaporAssignmentVisitor extends GJDepthFirst<Instr, FunDecl>{
    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public Instr visit(AssignmentStatement n, FunDecl fun_decl) {
        String id_str = n.f0.f0.toString();
        VaporIdentifier id = new VaporIdentifier(id_str);
        return n.f2.accept(this, fun_decl);
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
    public Instr visit(Expression n, FunDecl fun_decl) {
        return n.f0.accept(this, fun_decl);
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
    public Instr visit(PrimaryExpression n, FunDecl argu) {
        return n.f0.accept(this, argu);
    }
}
