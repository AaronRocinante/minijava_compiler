import syntaxtree.*;
import vaportree.*;
import visitor.GJVoidDepthFirst;

/**
 * Created by Aaron on 11/5/16.
 */
public class FuncParamVisitor extends GJVoidDepthFirst<FunDecl> {
    /**
     * f0 -> ArrayType()
     *       | BooleanType()
     *       | IntegerType()
     *       | Identifier()
     */
    public void visit(Type n, FunDecl fun_decl) {
        return;
    }
    /**
     * f0 -> <IDENTIFIER>
     */
    public void visit(Identifier n, FunDecl fun_decl) {
        VaporIdentifier param_id = new VaporIdentifier(n.f0.toString());
        String param_id_str = param_id.id;
        String class_name = fun_decl.symbol_table_class_key;
        String method_name = fun_decl.symbol_table_method_key;
        VaporIdentifier full_param_id = new VaporIdentifier(class_name+"."+method_name+"."+param_id_str);
        fun_decl.parameters.add(full_param_id);
    }
}
