import syntaxtree.*;
import visitor.*;
import vaportree.*;
import java.util.*;
import vaportree.Pair;

public class VaporProgramBuilder extends GJVoidDepthFirst<ClassTable> {
    public static Program vapor_tree = new Program();
    public static HashMap<String, ClassTable> symbolTable;
    public static HashSet<ArrayList<String> > link_set;

    // create the const declaration labels as well as sort out all the inherited fields
    public static void unfold_inherited_things()
    {
        ClassTable current_class_table;
        for(Map.Entry<String,ClassTable> class_entry: symbolTable.entrySet())
        {
            current_class_table = class_entry.getValue();
            if(current_class_table.is_main_class)
            {
                current_class_table.total_methods = current_class_table.methods;
                current_class_table.total_fields = current_class_table.fields;
                continue;
            }
            LinkedList<ClassTable> pedigree = new LinkedList<ClassTable>();

            ClassTable moving_parent = current_class_table;
            while(moving_parent != null)
            {
                pedigree.push(moving_parent);
                if(moving_parent.parent_class_type == null)
                {
                    break;
                }
                moving_parent = symbolTable.get(moving_parent.parent_class_type.toString());
            }

            moving_parent = pedigree.pop();
            while(moving_parent != null)
            {
                String class_name = moving_parent.class_type.toString();
                for(Map.Entry<String, ComplexType> field: moving_parent.fields.entrySet())
                {
                    current_class_table.total_fields.put(class_name+"."+field.getKey(),field.getValue());
                }
                for(Map.Entry<String, MethodTable> method: moving_parent.methods.entrySet())
                {
                    current_class_table.total_methods.put(method.getKey(), method.getValue());
                }
                moving_parent = pedigree.pollFirst();

            }
            String class_name = current_class_table.class_type.toString();
            Label const_decl_label = new Label("vmt_" + class_name);
            ConstDecl const_declaration = new ConstDecl(const_decl_label);

            // number the method positions and add them to the constant declaration
            Integer position_tracker = 0;
            for(Map.Entry<String, MethodTable> method: current_class_table.total_methods.entrySet())
            {
                MethodTable current_method_table = method.getValue();
                current_method_table.position_in_vmt = position_tracker;
                position_tracker++;

                String method_name = method.getKey();
                ComplexType containing_class = current_class_table.retrieve_class_containing_method(method_name,symbolTable);
                String containing_class_str = containing_class.toString();
                const_declaration.labels.add(new Label(containing_class_str + "." + method_name));
            }

            // now we number the field positions
            // start from 1 as 0 is for the vmt_table pointer
            position_tracker = 1;
            for(Map.Entry<String,ComplexType> field: current_class_table.total_fields.entrySet())
            {
                String field_name = field.getKey();
                current_class_table.total_fields_numbering.put(field_name, position_tracker);
                position_tracker++;
            }
            vapor_tree.constant_declarations.add(const_declaration);
        }
    }

    public Integer class_byte_size (ComplexType class_type)
    {
        ClassTable class_table = symbolTable.get(class_type.toString());
        if(class_table == null) {
            return 0;
        }
        // one for the class itself i.e. 4 bytes for the virtual method table
        Integer size = 1 + class_table.total_fields.size();
        Integer byte_size = size * 4;
        return byte_size;
    }

    public ComplexType get_type(String class_key, String method_key, String id_str)
    {
        ComplexType type = symbolTable.get(class_key).methods.get(method_key).retrieve_type(id_str,symbolTable);
        if(type!=null)
        {
            return type;
        }
        type = symbolTable.get(class_key).retrieve_type(id_str,symbolTable);
            return type;
    }

    public VaporProgramBuilder(){}
    public VaporProgramBuilder(HashMap<String, ClassTable> symbolTable, HashSet<ArrayList<String> > link_set)
    {
        this.symbolTable = symbolTable;
        this.link_set = link_set;
    }

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public void visit(Goal n,ClassTable class_name) {
        unfold_inherited_things();
        n.f0.accept(this,null);
        n.f1.accept(this,null);
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
    public void visit(MainClass n, ClassTable argu) {
        String main_class_name = n.f1.f0.toString();

        FunDecl main_function = new FunDecl(new Label("Main"));
        main_function.symbol_table_class_key = main_class_name;
        main_function.symbol_table_method_key = "main";

        Label init_label = new Label(main_class_name+".init");
        CodeBlock init_block = new CodeBlock(main_function);

        // add array access out of bounds handling block
        String out_of_bounds_str = main_class_name+"."+"main"+"."+"out_of_bounds";
        Label out_of_bounds_label = new Label(out_of_bounds_str);
        main_function.label_counts++;
        Err outta_bounds_err_instr = new Err("array index out of bounds");   // this is an instruction
        CodeBlock err_block1 = new CodeBlock(main_function);
        err_block1.instructions.add(outta_bounds_err_instr);
        err_block1.jump = new Ret();
        Pair<Label, CodeBlock> out_of_bounds = new Pair<Label, CodeBlock>(out_of_bounds_label, err_block1);
        main_function.error_handling_blocks.put(out_of_bounds_str,out_of_bounds);

        // add null pointer handling block
        String null_pointer_str = main_class_name+"."+"main"+"."+"null_pointer";
        Label null_pointer_label = new Label(null_pointer_str);
        main_function.label_counts++;
        Err null_pointer_err_instr = new Err("null pointer");
        CodeBlock err_block_2 = new CodeBlock(main_function);
        err_block_2.instructions.add(null_pointer_err_instr);
        err_block_2.jump = new Ret();
        Pair<Label, CodeBlock> null_pointer = new Pair<Label, CodeBlock>(null_pointer_label, err_block_2);
        main_function.error_handling_blocks.put(null_pointer_str, null_pointer);


        // add new int negative size handling block
        String negative_size_str = main_class_name+"."+"main"+"."+"negative_size";
        Label neg_array_size_label = new Label(negative_size_str);
        main_function.label_counts++;
        Err neg_array_size_err_instr = new Err("negative array size");
        CodeBlock err_block3 = new CodeBlock(main_function);
        err_block3.instructions.add(neg_array_size_err_instr);
        err_block3.jump = new Ret();
        Pair<Label,CodeBlock> neg_arr_size =new Pair<Label, CodeBlock>(neg_array_size_label, err_block3);
        main_function.error_handling_blocks.put(negative_size_str, neg_arr_size);

        Pair<Label, CodeBlock> working_pair = new Pair<Label, CodeBlock>(init_label, init_block);
        main_function.labeled_blocks.put("init_pair",working_pair);
        CodeBlock working_block = working_pair.second();

        for (Node node: n.f15.nodes)
        {
            working_block = node.accept(new FunctionVisitor(), working_block);
        }

        // add the ret statement
        Ret ret = new Ret();
        working_block.jump = ret;

        vapor_tree.function_declarations.put("main", main_function);
    }

    /**
     * f0 -> ClassDeclaration()
     *       | ClassExtendsDeclaration()
     */
    public void visit(TypeDeclaration n, ClassTable argu) {
        n.f0.accept(this, argu);
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public void visit(ClassDeclaration n, ClassTable argu) {
        String class_name = n.f1.f0.toString();
        ClassTable class_table = symbolTable.get(class_name);
        n.f4.accept(this, class_table);
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
    public void visit(ClassExtendsDeclaration n, ClassTable argu) {
        String class_name = n.f1.f0.toString();
        ClassTable class_table = symbolTable.get(class_name);
        n.f6.accept(this, class_table);
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public void visit(VarDeclaration n, ClassTable class_table) {

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
    public void visit(MethodDeclaration n, ClassTable class_table) {
        String class_name = class_table.class_type.toString();
        String method_name = n.f2.f0.toString();

        Label func_label = new Label(class_name + "." + method_name);
        FunDecl method_declaration = new FunDecl(func_label);
        method_declaration.symbol_table_class_key = class_name;
        method_declaration.symbol_table_method_key = method_name;

        // add array access out of bounds handling block
        String out_of_bounds_str = class_name+"."+method_name+"."+"out_of_bounds";
        Label out_of_bounds_label = new Label(out_of_bounds_str);
        method_declaration.label_counts++;
        Err outta_bounds_err_instr = new Err("array index out of bounds");   // this is an instruction
        CodeBlock err_block1 = new CodeBlock(method_declaration);
        err_block1.instructions.add(outta_bounds_err_instr);
        err_block1.jump = new Ret();
        Pair<Label, CodeBlock> out_of_bounds = new Pair<Label, CodeBlock>(out_of_bounds_label, err_block1);
        method_declaration.error_handling_blocks.put(out_of_bounds_str,out_of_bounds);

        // add null pointer handling block
        String null_pointer_str = class_name+"."+method_name+"."+"null_pointer";
        Label null_pointer_label = new Label(null_pointer_str);
        method_declaration.label_counts++;
        Err null_pointer_err_instr = new Err("null pointer");
        CodeBlock err_block_2 = new CodeBlock(method_declaration);
        err_block_2.instructions.add(null_pointer_err_instr);
        err_block_2.jump = new Ret();
        Pair<Label,CodeBlock> null_pointer = new Pair<Label,CodeBlock>(null_pointer_label, err_block_2);
        method_declaration.error_handling_blocks.put(null_pointer_str, null_pointer);

        // add new int negative size handling block
        String negative_size_str = class_name+"."+method_name+"."+"negative_size";
        Label neg_array_size_label = new Label(negative_size_str);
        method_declaration.label_counts++;
        Err neg_array_size_err_instr = new Err("negative array size");
        CodeBlock err_block_3 = new CodeBlock(method_declaration);
        err_block_3.instructions.add(neg_array_size_err_instr);
        err_block_3.jump = new Ret();
        Pair<Label,CodeBlock> neg_arr_size =new Pair<Label, CodeBlock>(neg_array_size_label, err_block_3);
        method_declaration.error_handling_blocks.put(negative_size_str, neg_arr_size);

        VaporIdentifier this_param = new VaporIdentifier("this");
        method_declaration.parameters.add(this_param);

        n.f4.accept(new FuncParamVisitor(), method_declaration);

        Label init_label = new Label(class_name + "." + method_name + ".init");
        CodeBlock init_block = new CodeBlock(method_declaration);

        Pair<Label, CodeBlock> working_pair = new Pair<Label, CodeBlock>(init_label, init_block);
        method_declaration.labeled_blocks.put("init_pair", working_pair);
        CodeBlock working_block = working_pair.second();

        for (Node node: n.f8.nodes)
        {
            working_block = node.accept(new FunctionVisitor(), working_block);
        }

        ComplexType return_type = class_table.methods.get(method_name).return_type;
        Operand return_target;
        if(return_type.equals(ComplexType.INT))
        {
            return_target = n.f10.accept(new ExpToIntVisitor(),working_block);
        }
        else if (return_type.equals(ComplexType.BOO))
        {
            return_target = n.f10.accept(new ExpToBooVisitor(), working_block);
        }
        else if (return_type.equals(ComplexType.IARR))
        {
            return_target = n.f10.accept(new ExpToArrVisitor(), working_block);
        }
        else
        {
            return_target = n.f10.accept(new ExpToComplexVisitor(),working_block);
        }
        working_block.jump = new RetOperand(return_target);

        vapor_tree.function_declarations.put(class_name+"."+method_name, method_declaration);
    }
}
