import syntaxtree.*;
import vaportree.*;
import visitor.GJDepthFirst;

/**
 * Created by Aaron on 11/2/16.
 */
public class ExpToComplexVisitor extends GJDepthFirst<VaporIdentifier, CodeBlock> {
    /**
     * f0 ->              AndExpression()
     *              | CompareExpression()
     *                 | PlusExpression()
     *                | MinusExpression()
     *                | TimesExpression()
     *                    | ArrayLookup()
     *                    | ArrayLength()
     *       | MessageSend()
     *       | PrimaryExpression()
     */
    public VaporIdentifier visit(Expression n, CodeBlock code_block) {
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
    public VaporIdentifier visit(MessageSend n, CodeBlock code_block) {
        ComplexType class_type = n.f0.accept(new VaporClassTypeFromPrimaryExpVisitor(),code_block);
        ClassTable class_table = VaporProgramBuilder.symbolTable.get(class_type.toString());

        // get the function offset
        String method_name = n.f2.f0.toString();
        Integer func_offset_int = class_table.total_methods.get(method_name).position_in_vmt * 4;
        VaporIntegerLiteral func_offset = new VaporIntegerLiteral(func_offset_int.toString());

        VaporIdentifier caller_id = n.f0.accept(new ExpToComplexVisitor(), code_block);

        // check the caller isn't a null pointer
        VaporIdentifier is_null = new VaporIdentifier("is_null" + code_block.fun_decl.temp_variable_counts.toString());
        code_block.fun_decl.temp_variable_counts++;
        Eq eq_null = new Eq(caller_id, new VaporIntegerLiteral("0"));
        IdEqualOperator get_check_null_result_instr = new IdEqualOperator(is_null, eq_null);
        code_block.instructions.add(get_check_null_result_instr);

        FunDecl fun_decl = code_block.fun_decl;
        String local_class_name = code_block.symbol_table_class_key;
        String local_method_name = code_block.symbol_table_method_key;
        String null_pointer_str = local_class_name+"."+local_method_name+"."+"null_pointer";
        Label null_pointer_label = fun_decl.error_handling_blocks.get(null_pointer_str).first();

        VaporIdentifier not_null = new VaporIdentifier("not_null" + code_block.fun_decl.temp_variable_counts.toString());
        code_block.fun_decl.temp_variable_counts++;

        Sub negate = new Sub(new VaporIntegerLiteral("1"), is_null);
        IdEqualOperator negate_is_null = new IdEqualOperator(not_null, negate);
        code_block.instructions.add(negate_is_null);

        If0_goto if_null = new If0_goto(not_null, null_pointer_label);
        code_block.instructions.add(if_null);

        // get the vmt and store it at vmt_id
        VaporIdentifier vmt_id = new VaporIdentifier("_table" + code_block.fun_decl.temp_variable_counts.toString());
        code_block.fun_decl.temp_variable_counts++;
        MemRef load_vmt = new MemRef (caller_id, new VaporIntegerLiteral("0"));
        IdEqualMem set_vmt_id_instr = new IdEqualMem(vmt_id, load_vmt);
        code_block.instructions.add(set_vmt_id_instr);
        // get the method and store it at func_id
        VaporIdentifier func_id = new VaporIdentifier("_method" + code_block.fun_decl.temp_variable_counts.toString());
        code_block.fun_decl.temp_variable_counts++;
        MemRef get_func = new MemRef(vmt_id, func_offset);
        IdEqualMem set_func_id_instr = new IdEqualMem(func_id, get_func);
        code_block.instructions.add(set_func_id_instr);

        VaporIdentifier call_result = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
        code_block.fun_decl.temp_variable_counts++;

        IdEqualCall set_call_result_instr = new IdEqualCall(call_result, func_id);
        code_block.current_instruction.push(set_call_result_instr);
        // load in the parameters using a visitor
        set_call_result_instr.arguments.add(caller_id);
        if(n.f4.present())
        {
            n.f4.accept(new VaporLoadParamVisitor(), code_block);
        }
        code_block.current_instruction.pop();
        code_block.instructions.add(set_call_result_instr);

        return call_result;
    }

    /**
     * f0 ->                     IntegerLiteral()
     *                            | TrueLiteral()
     *                           | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()              done
     *              | ArrayAllocationExpression()
     *       | AllocationExpression()        done
     *                          | NotExpression()
     *       | BracketExpression()           done
     */
    public VaporIdentifier visit(PrimaryExpression n, CodeBlock code_block) {
        return n.f0.accept(this, code_block);
    }

    /**
     * f0 -> <IDENTIFIER>
     */

    //this has to be modified when we add objet oriented features
    public VaporIdentifier visit(Identifier n, CodeBlock code_block) {
        String id_str = n.f0.toString();

        ClassTable class_table = VaporProgramBuilder.symbolTable.get(code_block.symbol_table_class_key);
        MethodTable method_table = class_table.methods.get(code_block.symbol_table_method_key);
        if(method_table.variable_present(id_str))
        {
            String var_name = code_block.symbol_table_class_key+"."+code_block.symbol_table_method_key+"."+id_str;
            VaporIdentifier id = new VaporIdentifier(var_name);
            return id;
        }
        else
        {
            ComplexType class_containing = class_table.retrieve_class_containing_var(id_str,VaporProgramBuilder.symbolTable);
            String class_containing_str = class_containing.toString();
            Integer field_offset = class_table.total_fields_numbering.get(class_containing_str+"."+id_str) * 4;
            VaporIdentifier this_id = new VaporIdentifier("this");

            VaporIdentifier id_addr = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;

            Add add_id_addr = new Add(this_id, new VaporIntegerLiteral(field_offset.toString()));
            IdEqualOperator set_id_addr = new IdEqualOperator(id_addr, add_id_addr);
            code_block.instructions.add(set_id_addr);

            VaporIntegerLiteral zero_offset = new VaporIntegerLiteral("0");
            MemRef mem_ref = new MemRef(id_addr, zero_offset);

            VaporIdentifier result = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            IdEqualMem get_result_instr = new IdEqualMem(result, mem_ref);
            code_block.instructions.add(get_result_instr);
            return result;
        }
    }

    /**
     * f0 -> "this"
     */
    public VaporIdentifier visit(ThisExpression n, CodeBlock code_block) {
//        String class_name = code_block.symbol_table_class_key;
//        ClassTable class_table = VaporProgramBuilder.symbolTable.get(class_name);

//        VaporIdentifier this_id = new VaporIdentifier("t" + code_block.fun_decl.temp_variable_counts.toString());
//        code_block.fun_decl.temp_variable_counts++;

        VaporIdentifier this_id = new VaporIdentifier("this");
        return this_id;
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public VaporIdentifier visit(AllocationExpression n, CodeBlock code_block) {
        String class_type_str = n.f1.f0.toString();
        ComplexType class_type = new ComplexType(class_type_str);
        Integer byte_size_int = new VaporProgramBuilder().class_byte_size(class_type);
        VaporIntegerLiteral byte_size = new VaporIntegerLiteral(byte_size_int.toString());

        VaporIdentifier temp_class_id = new VaporIdentifier("t" + code_block.fun_decl.temp_variable_counts.toString());
        code_block.fun_decl.temp_variable_counts++;

        HeapAllocZ allocating_object_instr = new HeapAllocZ(temp_class_id, byte_size);
        code_block.instructions.add(allocating_object_instr);

        Label vmt_label = new Label("vmt_" + class_type_str);
        MemRef vmt = new MemRef(temp_class_id, new VaporIntegerLiteral("0"));

        MemEqualLabel store_vmt_instr = new MemEqualLabel(vmt,vmt_label);
        code_block.instructions.add(store_vmt_instr);

        return temp_class_id;
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public VaporIdentifier visit(BracketExpression n, CodeBlock code_block) {
        return n.f1.accept(this, code_block);
    }
}
