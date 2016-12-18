import syntaxtree.*;
import vaportree.*;
import visitor.*;

public class ExpToIntVisitor extends GJDepthFirst<VaporIdentifier, CodeBlock> {
    /**
     * f0 ->              AndExpression()
     *              | CompareExpression()
     *       | PlusExpression()     done
     *       | MinusExpression()    done
     *       | TimesExpression()    done
     *       | ArrayLookup()        done
     *       | ArrayLength()        done
     *       | MessageSend()
     *       | PrimaryExpression()  done
     */
    public VaporIdentifier visit(Expression n, CodeBlock code_block) {
        return n.f0.accept(this, code_block);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public VaporIdentifier visit(PlusExpression n, CodeBlock code_block) {
        Operand lhs = n.f0.accept(this, code_block);
        Operand rhs = n.f2.accept(this, code_block);
        Add add = new Add(lhs, rhs);
        VaporIdentifier result = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
        code_block.fun_decl.temp_variable_counts++;
        IdEqualOperator instr = new IdEqualOperator(result, add);
        code_block.instructions.add(instr);
        return result;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public VaporIdentifier visit(MinusExpression n, CodeBlock code_block) {
        Operand lhs = n.f0.accept(this, code_block);
        Operand rhs = n.f2.accept(this, code_block);
        Sub sub = new Sub(lhs,rhs);
        VaporIdentifier result = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
        code_block.fun_decl.temp_variable_counts++;
        IdEqualOperator instr = new IdEqualOperator(result,sub);
        code_block.instructions.add(instr);
        return result;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public VaporIdentifier visit(TimesExpression n, CodeBlock code_block) {
        Operand lhs = n.f0.accept(this, code_block);
        Operand rhs = n.f2.accept(this, code_block);
        MulS mul_s = new MulS(lhs, rhs);
        VaporIdentifier result = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
        code_block.fun_decl.temp_variable_counts++;
        IdEqualOperator instr = new IdEqualOperator(result, mul_s);
        code_block.instructions.add(instr);
        return result;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public VaporIdentifier visit(ArrayLookup n, CodeBlock code_block) {
        VaporIdentifier int_arr_id = n.f0.accept(new VaporArrayVisitor(), code_block);
        String int_arr_id_str = int_arr_id.id;

        ClassTable class_table = VaporProgramBuilder.symbolTable.get(code_block.symbol_table_class_key);
        MethodTable method_table = class_table.methods.get(code_block.symbol_table_method_key);

        if(method_table.variable_present(int_arr_id_str))
        {
            String int_arr_name_str = code_block.fun_decl.symbol_table_class_key + "." +
                    code_block.fun_decl.symbol_table_method_key+"."+ int_arr_id_str;
            VaporIdentifier int_arr_full_id = new VaporIdentifier(int_arr_name_str);

            // check int_arr_full_id isn't a null pointer
            VaporIdentifier is_null = new VaporIdentifier("is_null" + code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            Eq eq_null = new Eq(int_arr_full_id, new VaporIntegerLiteral("0"));
            IdEqualOperator get_check_null_result_instr = new IdEqualOperator(is_null, eq_null);
            code_block.instructions.add(get_check_null_result_instr);

            FunDecl fun_decl = code_block.fun_decl;
            String class_name = code_block.symbol_table_class_key;
            String method_name = code_block.symbol_table_method_key;
            String null_pointer_str = class_name+"."+method_name+"."+"null_pointer";
            Label null_pointer_label = fun_decl.error_handling_blocks.get(null_pointer_str).first();

            VaporIdentifier not_null = new VaporIdentifier("not_null" + code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;

            Sub negate = new Sub(new VaporIntegerLiteral("1"), is_null);
            IdEqualOperator negate_is_null = new IdEqualOperator(not_null, negate);
            code_block.instructions.add(negate_is_null);

            If0_goto if_null = new If0_goto(not_null, null_pointer_label);
            code_block.instructions.add(if_null);


            Operand original_offset = n.f2.accept(new ExpToIntVisitor(), code_block);
            // we're going to do [addr + 0] regardless of what subtype original_offset is
            // add 1
            VaporIntegerLiteral one = new VaporIntegerLiteral("1");
            Add squash = new Add(original_offset, one);
            VaporIdentifier squash_result = new VaporIdentifier("t" + code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            IdEqualOperator set_squash_result = new IdEqualOperator(squash_result, squash);
            code_block.instructions.add(set_squash_result);

            // times 4
            VaporIntegerLiteral four = new VaporIntegerLiteral("4");
            MulS num_to_byte = new MulS(squash_result, four);
            VaporIdentifier byte_offset = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            IdEqualOperator set_byte_offset = new IdEqualOperator(byte_offset, num_to_byte);
            code_block.instructions.add(set_byte_offset);

            // get the array length
            VaporIdentifier temp_length = new VaporIdentifier("t" + code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            VaporIntegerLiteral offset = new VaporIntegerLiteral("0");
            MemRef mem_ref = new MemRef(int_arr_full_id, offset);
            IdEqualMem instr = new IdEqualMem(temp_length, mem_ref);
            code_block.instructions.add(instr);

            // check non-negativity
            VaporIntegerLiteral minus1 = new VaporIntegerLiteral("-1");
            LtS lts1 = new LtS(minus1, original_offset);
            VaporIdentifier non_negative_cond = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            IdEqualOperator id_eq_op_1 = new IdEqualOperator(non_negative_cond, lts1 );
            code_block.instructions.add(id_eq_op_1);

            String out_of_bouds_str = class_name+"."+method_name+"."+"out_of_bounds";
            Label bounds_err_label = fun_decl.error_handling_blocks.get(out_of_bouds_str).first();
            If0_goto if_neg = new If0_goto(non_negative_cond, bounds_err_label);
            code_block.instructions.add(if_neg);

            // check index within bounds
            VaporIdentifier in_bounds = new VaporIdentifier("t" + code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            LtS lts2 = new LtS(original_offset, temp_length);
            IdEqualOperator id_eq_op_2 = new IdEqualOperator(in_bounds, lts2);
            code_block.instructions.add(id_eq_op_2);

            If0_goto if_outta_bounds = new If0_goto(in_bounds, bounds_err_label);
            code_block.instructions.add(if_outta_bounds);

            // looking up
            VaporIdentifier value = new VaporIdentifier("t"+ code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            VaporIdentifier new_starting_point = new VaporIdentifier("t" + code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            Add add_offset = new Add(int_arr_full_id, byte_offset);
            IdEqualOperator get_new_starting_point = new IdEqualOperator(new_starting_point, add_offset);
            code_block.instructions.add(get_new_starting_point);

            MemRef array_element = new MemRef(new_starting_point, new VaporIntegerLiteral("0"));
            IdEqualMem retrieve_array_element_instr = new IdEqualMem(value, array_element);
            code_block.instructions.add(retrieve_array_element_instr);

            return value;
        }
        else
        {
            ComplexType class_containing = class_table.retrieve_class_containing_var(int_arr_id_str,VaporProgramBuilder.symbolTable);
            String class_containing_str = class_containing.toString();
            Integer field_offset = class_table.total_fields_numbering.get(class_containing_str+"."+int_arr_id.id) * 4;
            VaporIdentifier this_id = new VaporIdentifier("this");
            VaporIdentifier arr_ptr_ptr = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;

            Add get_arr_address = new Add(this_id, new VaporIntegerLiteral(field_offset.toString()));
            IdEqualOperator set_arr_address_instr = new IdEqualOperator(arr_ptr_ptr, get_arr_address);
            code_block.instructions.add(set_arr_address_instr);

            VaporIdentifier arr_ptr = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            MemRef arr_ptr_mem_ref = new MemRef(arr_ptr_ptr,new VaporIntegerLiteral("0"));
            IdEqualMem get_arr_ptr_instr = new IdEqualMem(arr_ptr, arr_ptr_mem_ref);
            code_block.instructions.add(get_arr_ptr_instr);

            // check if arr_ptr is a null pointer
            VaporIdentifier is_null = new VaporIdentifier("is_null" + code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            Eq eq_null = new Eq(arr_ptr, new VaporIntegerLiteral("0"));
            IdEqualOperator get_check_null_result_instr = new IdEqualOperator(is_null, eq_null);
            code_block.instructions.add(get_check_null_result_instr);

            FunDecl fun_decl = code_block.fun_decl;
            String class_name = code_block.symbol_table_class_key;
            String method_name = code_block.symbol_table_method_key;
            String null_pointer_str = class_name+"."+method_name+"."+"null_pointer";
            Label null_pointer_label = fun_decl.error_handling_blocks.get(null_pointer_str).first();

            VaporIdentifier not_null = new VaporIdentifier("not_null" + code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;

            Sub negate = new Sub(new VaporIntegerLiteral("1"), is_null);
            IdEqualOperator negate_is_null = new IdEqualOperator(not_null, negate);
            code_block.instructions.add(negate_is_null);

            If0_goto if_null = new If0_goto(not_null, null_pointer_label);
            code_block.instructions.add(if_null);

            Operand original_offset = n.f2.accept(new ExpToIntVisitor(), code_block);
            // we're going to do [addr + 0] regardless of what subtype original_offset is
            // add 1
            VaporIntegerLiteral one = new VaporIntegerLiteral("1");
            Add squash = new Add(original_offset, one);
            VaporIdentifier squash_result = new VaporIdentifier("t" + code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            IdEqualOperator set_squash_result = new IdEqualOperator(squash_result, squash);
            code_block.instructions.add(set_squash_result);

            // times 4
            VaporIntegerLiteral four = new VaporIntegerLiteral("4");
            MulS num_to_byte = new MulS(squash_result, four);
            VaporIdentifier byte_offset = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            IdEqualOperator set_byte_offset = new IdEqualOperator(byte_offset, num_to_byte);
            code_block.instructions.add(set_byte_offset);

            // get the array length
            VaporIdentifier temp_length = new VaporIdentifier("t" + code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            VaporIntegerLiteral zero_offset = new VaporIntegerLiteral("0");
            MemRef mem_ref = new MemRef(arr_ptr, zero_offset);
            IdEqualMem instr = new IdEqualMem(temp_length, mem_ref);
            code_block.instructions.add(instr);

            // check non-negativity
            VaporIntegerLiteral minus1 = new VaporIntegerLiteral("-1");
            LtS lts1 = new LtS(minus1, original_offset);
            VaporIdentifier non_negative_cond = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            IdEqualOperator id_eq_op_1 = new IdEqualOperator(non_negative_cond, lts1);
            code_block.instructions.add(id_eq_op_1);

            String out_of_bounds_str = class_name+"."+method_name+"."+"out_of_bounds";
            Label bounds_err_label = fun_decl.error_handling_blocks.get(out_of_bounds_str).first();
            If0_goto if_neg = new If0_goto(non_negative_cond, bounds_err_label);
            code_block.instructions.add(if_neg);

            // check index within bounds
            VaporIdentifier in_bounds = new VaporIdentifier("t" + code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            LtS lts2 = new LtS(original_offset, temp_length);
            IdEqualOperator id_eq_op_2 = new IdEqualOperator(in_bounds, lts2);
            code_block.instructions.add(id_eq_op_2);

            If0_goto if_outta_bounds = new If0_goto(in_bounds, bounds_err_label);
            code_block.instructions.add(if_outta_bounds);

            // getting the value
            VaporIdentifier new_starting_point = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;

            Add add_offset = new Add(arr_ptr, byte_offset);
            IdEqualOperator add_offset_instr = new IdEqualOperator(new_starting_point, add_offset);
            code_block.instructions.add(add_offset_instr);

            VaporIdentifier value = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;

            MemRef array_element_mem_ref = new MemRef(new_starting_point, new VaporIntegerLiteral("0"));
            IdEqualMem get_array_element_instr = new IdEqualMem(value, array_element_mem_ref);
            code_block.instructions.add(get_array_element_instr);

            return value;
        }
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public VaporIdentifier visit(ArrayLength n, CodeBlock code_block) {
        VaporIdentifier int_arr_id = n.f0.accept(new VaporArrayVisitor(), code_block);
        String arr_id_str = int_arr_id.id;

        ClassTable class_table = VaporProgramBuilder.symbolTable.get(code_block.symbol_table_class_key);
        MethodTable method_table = class_table.methods.get(code_block.symbol_table_method_key);

        if(method_table.variable_present(arr_id_str))
        {
            String int_arr_name_str = code_block.fun_decl.symbol_table_class_key + "." +
                    code_block.fun_decl.symbol_table_method_key+"."+ int_arr_id.id;
            VaporIdentifier int_arr_full_id = new VaporIdentifier(int_arr_name_str);

            // check int_arr_full_id isn't a null pointer
            VaporIdentifier is_null = new VaporIdentifier("is_null" + code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            Eq eq_null = new Eq(int_arr_full_id, new VaporIntegerLiteral("0"));
            IdEqualOperator get_check_null_result_instr = new IdEqualOperator(is_null, eq_null);
            code_block.instructions.add(get_check_null_result_instr);

            FunDecl fun_decl = code_block.fun_decl;
            String class_name = code_block.symbol_table_class_key;
            String method_name = code_block.symbol_table_method_key;
            String null_pointer_str = class_name+"."+method_name+"."+"null_pointer";
            Label null_pointer_label = fun_decl.error_handling_blocks.get(null_pointer_str).first();

            VaporIdentifier not_null = new VaporIdentifier("not_null" + code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;

            Sub negate = new Sub(new VaporIntegerLiteral("1"), is_null);
            IdEqualOperator negate_is_null = new IdEqualOperator(not_null, negate);
            code_block.instructions.add(negate_is_null);

            If0_goto if_null = new If0_goto(not_null, null_pointer_label);
            code_block.instructions.add(if_null);

            VaporIdentifier temp_length = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;

            VaporIntegerLiteral offset = new VaporIntegerLiteral("0");
            MemRef mem_ref = new MemRef(int_arr_full_id, offset);
            IdEqualMem instr = new IdEqualMem(temp_length, mem_ref);
            code_block.instructions.add(instr);

            return temp_length;
        }
        else
        {
            ComplexType class_containing = class_table.retrieve_class_containing_var(arr_id_str,VaporProgramBuilder.symbolTable);
            String class_containing_str = class_containing.toString();
            Integer field_offset = class_table.total_fields_numbering.get(class_containing_str+"."+arr_id_str) * 4;
            VaporIdentifier this_id = new VaporIdentifier("this");
            VaporIdentifier arr_ptr_ptr = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;

            Add get_arr_address = new Add(this_id, new VaporIntegerLiteral(field_offset.toString()));
            IdEqualOperator set_arr_address_instr = new IdEqualOperator(arr_ptr_ptr, get_arr_address);
            code_block.instructions.add(set_arr_address_instr);

            VaporIdentifier arr_ptr = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            MemRef arr_ptr_mem_ref = new MemRef(arr_ptr_ptr,new VaporIntegerLiteral("0"));
            IdEqualMem get_arr_ptr_instr = new IdEqualMem(arr_ptr, arr_ptr_mem_ref);
            code_block.instructions.add(get_arr_ptr_instr);

            // check if arr_ptr is a null pointer
            VaporIdentifier is_null = new VaporIdentifier("is_null" + code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            Eq eq_null = new Eq(arr_ptr, new VaporIntegerLiteral("0"));
            IdEqualOperator get_check_null_result_instr = new IdEqualOperator(is_null, eq_null);
            code_block.instructions.add(get_check_null_result_instr);

            FunDecl fun_decl = code_block.fun_decl;
            String class_name = code_block.symbol_table_class_key;
            String method_name = code_block.symbol_table_method_key;
            String null_pointer_str = class_name+"."+method_name+"."+"null_pointer";
            Label null_pointer_label = fun_decl.error_handling_blocks.get(null_pointer_str).first();

            VaporIdentifier not_null = new VaporIdentifier("not_null" + code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;

            Sub negate = new Sub(new VaporIntegerLiteral("1"), is_null);
            IdEqualOperator negate_is_null = new IdEqualOperator(not_null, negate);
            code_block.instructions.add(negate_is_null);

            If0_goto if_null = new If0_goto(not_null, null_pointer_label);
            code_block.instructions.add(if_null);

            // get the length
            VaporIdentifier length_id = new VaporIdentifier("t" + code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            VaporIntegerLiteral zero_offset = new VaporIntegerLiteral("0");
            MemRef mem_ref = new MemRef(arr_ptr, zero_offset);
            IdEqualMem instr = new IdEqualMem(length_id, mem_ref);
            code_block.instructions.add(instr);
            return length_id;
        }
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
     * f0 -> IntegerLiteral()               done
     *                            | TrueLiteral()
     *                           | FalseLiteral()
     *       | Identifier()                 done
     *       | ThisExpression()
     *              | ArrayAllocationExpression()
     *                   | AllocationExpression()
     *                          | NotExpression()
     *       | BracketExpression()          done
     */
    public VaporIdentifier visit(PrimaryExpression n, CodeBlock code_block) {
        return n.f0.accept(this, code_block);
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public VaporIdentifier visit(IntegerLiteral n, CodeBlock code_block) {
        String int_string = n.f0.toString();
        VaporIntegerLiteral o = new VaporIntegerLiteral(int_string);
        VaporIdentifier id = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
        code_block.fun_decl.temp_variable_counts++;

        IdEqualO set_id_instr = new IdEqualO(id, o);
        code_block.instructions.add(set_id_instr);

        return id;
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
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public VaporIdentifier visit(BracketExpression n, CodeBlock code_block) {
        return n.f1.accept(this, code_block);
    }

}
