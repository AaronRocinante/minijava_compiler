import vaportree.*;
import syntaxtree.*;
import visitor.GJDepthFirst;

/**
 * Created by Aaron on 10/30/16.
 */
public class ExpToArrVisitor extends GJDepthFirst<VaporIdentifier, CodeBlock> {
    /**
     * f0 ->             AndExpression()
     *             | CompareExpression()
     *                | PlusExpression()
     *               | MinusExpression()
     *               | TimesExpression()
     *                   | ArrayLookup()
     *                   | ArrayLength()
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
        return n.accept(new ExpToComplexVisitor(), code_block);

    }

    /**
     * f0 ->                     IntegerLiteral()
     *                            | TrueLiteral()
     *                           | FalseLiteral()
     *  | Identifier()                      done
     *                         | ThisExpression()
     *  | ArrayAllocationExpression()       done
     *                   | AllocationExpression()
     *                          | NotExpression()
     *       | BracketExpression()          done
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
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public VaporIdentifier visit(ArrayAllocationExpression n, CodeBlock code_block) {
        String class_name = code_block.symbol_table_class_key;
        String method_name = code_block.symbol_table_method_key;

        Operand original_size = n.f3.accept(new ExpToIntVisitor(), code_block);

        // add one
        VaporIntegerLiteral one = new VaporIntegerLiteral("1");
        Add add_one = new Add(original_size, one);
        VaporIdentifier add_one_result = new VaporIdentifier("t" + code_block.fun_decl.temp_variable_counts.toString());
        code_block.fun_decl.temp_variable_counts++;
        IdEqualOperator add_one_instr = new IdEqualOperator(add_one_result, add_one);
        code_block.instructions.add(add_one_instr);

        // times four
        VaporIntegerLiteral four = new VaporIntegerLiteral("4");
        MulS num_to_byte = new MulS(add_one_result, four);
        VaporIdentifier byte_size = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
        code_block.fun_decl.temp_variable_counts++;
        IdEqualOperator set_byte_size = new IdEqualOperator(byte_size, num_to_byte);
        code_block.instructions.add(set_byte_size);

        //check non-negative
        VaporIntegerLiteral minus1 = new VaporIntegerLiteral("-1");
        LtS lts1 = new LtS(minus1, original_size);
        VaporIdentifier non_negative_cond = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
        code_block.fun_decl.temp_variable_counts++;
        IdEqualOperator id_eq_op_1 = new IdEqualOperator(non_negative_cond, lts1);
        code_block.instructions.add(id_eq_op_1);

        FunDecl fun_decl = code_block.fun_decl;
        String out_of_bounds_str = class_name+"."+method_name+"."+"negative_size";
        Label bounds_err_label = fun_decl.error_handling_blocks.get(out_of_bounds_str).first();
        If0_goto if_neg = new If0_goto(non_negative_cond, bounds_err_label);
        code_block.instructions.add(if_neg);

        // if passed then allocate heap memory
        VaporIdentifier temp_arr_id = new VaporIdentifier("t" + code_block.fun_decl.temp_variable_counts.toString());
        code_block.fun_decl.temp_variable_counts++;
        HeapAllocZ alloc_instr = new HeapAllocZ(temp_arr_id, byte_size);
        code_block.instructions.add(alloc_instr);

        //store the length of the array in the first entry of the allocatd array
        VaporIdentifier temp_size_id = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
        code_block.fun_decl.temp_variable_counts++;
        IdEqualO size_operand_to_id_instr = new IdEqualO(temp_size_id, original_size);
        code_block.instructions.add(size_operand_to_id_instr);

        MemRef first_entry_ref = new MemRef(temp_arr_id,new VaporIntegerLiteral("0"));
        MemEqualID store_arr_length_instr = new MemEqualID(first_entry_ref, temp_size_id);
        code_block.instructions.add(store_arr_length_instr);

        return temp_arr_id;
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
