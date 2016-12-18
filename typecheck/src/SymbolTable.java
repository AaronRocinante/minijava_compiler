/**
 * Created by Aaron on 10/14/16.
 */
import visitor.*;
import syntaxtree.*;

import java.util.*;

class SymbolTable extends GJVoidDepthFirst<ClassTable>
{
    static HashMap<String, ClassTable> sym_table = new HashMap<String, ClassTable>();
    static HashSet<ArrayList<String> > link_set = new HashSet<ArrayList<String> >();
    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public void visit(Goal n, ClassTable table) {
        String main_class_name = n.f0.f1.f0.toString();
        ClassTable mc_table = new ClassTable(main_class_name);
        sym_table.put(main_class_name, mc_table);
        n.f0.accept(this, mc_table);
        for (Node node: n.f1.nodes)
        {
            node.accept(this, null);
        }
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
    public void visit(MainClass n, ClassTable class_table) {
        MethodTable method_table = new MethodTable(class_table);
        method_table.method_name = "main";
        method_table.return_type = null;
        method_table.variables.put(n.f11.f0.toString(), null);
        method_table.formal_params.add(new Pair<String, ComplexType>(n.f11.f0.toString(), null));
        ClassMethodVisitor class_method_visitor = new ClassMethodVisitor();
        for (Node node: n.f14.nodes)
        {
            method_table = node.accept(class_method_visitor, method_table);
        }
        class_table.methods.put(method_table.method_name, method_table);
        String class_name = class_table.class_type.toString();
        sym_table.put(class_name, class_table);
    }

    /**
     * f0 -> ClassDeclaration()
     *       | ClassExtendsDeclaration()
     */
    public void visit(TypeDeclaration n, ClassTable table) {
        n.f0.accept(this, null);
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public void visit(ClassDeclaration n, ClassTable table) {
        String class_name = n.f1.f0.toString();
        if(sym_table.get(class_name) != null)
        {
            throw new TypeException();
        }
        ClassTable class_table = new ClassTable(class_name);
        sym_table.put(class_name, class_table);
        for (Node node: n.f3.nodes)
        {
            class_table = sym_table.get(class_name);
            node.accept(this, class_table);
        }
        ClassMethodVisitor class_method_visitor = new ClassMethodVisitor();
        for (Node node: n.f4.nodes)
        {
            MethodTable method_table = new MethodTable(class_table);
            method_table = node.accept(class_method_visitor, method_table);
            if (class_table.methods.get(method_table.method_name) != null)
            {
                throw new TypeException();
            }
            // update the class_table
            class_table.methods.put(method_table.method_name, method_table);
            // update the sym_table
            sym_table.put(class_name, class_table);
        }
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
    public void visit(ClassExtendsDeclaration n, ClassTable table) {
        String class_name = n.f1.f0.toString();
        if(sym_table.get(class_name) != null)
        {
            throw new TypeException();
        }
        String parentclass_name = n.f3.f0.toString();
        ClassTable parent_table = sym_table.get(parentclass_name);
        if (parent_table == null)
        {
            throw new TypeException();
        }
        ClassTable class_table = new ClassTable(class_name);
        sym_table.put(class_name,class_table);

        ArrayList<String> new_pair = new ArrayList<String>();
        new_pair.add(class_name);
        new_pair.add(parentclass_name);
        link_set.add(new_pair);

        for (Node node: n.f5.nodes)
        {
            class_table = sym_table.get(class_name);
            node.accept(this, class_table);
        }

        ClassMethodVisitor classmethod_visitor = new ClassMethodVisitor();
        for (Node node: n.f6.nodes)
        {
            MethodTable method_table = new MethodTable(class_table);
            method_table = node.accept(classmethod_visitor, method_table);
            if (class_table.methods.get(method_table.method_name) != null)
            {
                throw new TypeException();
            }
            // update the class_table
            class_table.methods.put(method_table.method_name, method_table);
            // update the sym_table
            sym_table.put(class_name, class_table);
        }
        class_table.inherit(parent_table);
        sym_table.put(class_name, class_table);
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    // class fields declaration
    public void visit(VarDeclaration n, ClassTable class_table) {
        String classname = class_table.class_type.toString();
        String field_name = n.f1.f0.toString();
        if(class_table.fields.get(field_name)!= null) {
            throw new TypeException();
        }
        ComplexType type = n.f0.accept(new GetNonExpressionTypeVisitor());
        class_table.fields.put(field_name, type);
        sym_table.put(classname, class_table);
    }
}

class GetNonExpressionTypeVisitor extends GJNoArguDepthFirst<ComplexType>
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

class ClassMethodVisitor extends GJDepthFirst<MethodTable, MethodTable>
{

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public MethodTable visit(VarDeclaration n, MethodTable method_table) {
        String var_name = n.f1.f0.toString();
        if(method_table.variables.get(var_name) != null)
        {
            throw new TypeException();
        }
        ComplexType type = n.f0.accept(new GetNonExpressionTypeVisitor());
        method_table.variables.put(var_name, type);
        return method_table;
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
    public MethodTable visit(MethodDeclaration n, MethodTable method_table) {
        method_table.return_type = n.f1.accept(new GetNonExpressionTypeVisitor());
        method_table.method_name = n.f2.f0.toString();
        if (n.f4.present())
        {
            method_table = n.f4.accept(this, method_table);
        }
        for(Node node: n.f7.nodes)
        {
            method_table = node.accept(this,method_table);
        }
        return method_table;
    }


    /**
     * f0 -> FormalParameter()
     * f1 -> ( FormalParameterRest() )*
     */
    public MethodTable visit(FormalParameterList n, MethodTable method_table) {
        method_table = n.f0.accept(this, method_table);
        for(Node node: n.f1.nodes)
        {
            method_table = node.accept(this, method_table);
        }
        return method_table;
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    public MethodTable visit(FormalParameterRest n, MethodTable method_table) {
        return n.f1.accept(this, method_table);
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public MethodTable visit(FormalParameter n, MethodTable method_table) {
        ComplexType type = n.f0.accept(new GetNonExpressionTypeVisitor());
        String id = n.f1.f0.toString();
        if(method_table.variables.get(id)!=null)
        {
            throw new TypeException();
        }
        method_table.variables.put(id, type);
        method_table.formal_params.add(new Pair<String, ComplexType>(id,type));
        return method_table;
    }
}