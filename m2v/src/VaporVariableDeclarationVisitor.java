import syntaxtree.*;
import vaportree.*;
import visitor.*;

/**
 * Created by Aaron on 10/30/16.
 */

public class VaporVariableDeclarationVisitor extends GJDepthFirst<FunDecl, FunDecl>{

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public FunDecl visit(VarDeclaration n, FunDecl fun_decl) {
        return fun_decl;
    }
}
