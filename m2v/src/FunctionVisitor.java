import vaportree.Pair;
import syntaxtree.*;
import vaportree.*;
import visitor.GJDepthFirst;

public class FunctionVisitor extends GJDepthFirst<CodeBlock, CodeBlock> {
    /**
     * f0 -> Block()                        done
     *       | AssignmentStatement()        done
     *       | ArrayAssignmentStatement()   done
     *       | IfStatement()                done
     *       | WhileStatement()             done
     *       | PrintStatement()             done
     */
    public CodeBlock visit(Statement n, CodeBlock code_block) {
        return n.f0.accept(this, code_block);
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
    public CodeBlock visit(Block n, CodeBlock code_block) {
        for (Node node: n.f1.nodes)
        {
            code_block = node.accept(this, code_block);
        }
        return code_block;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public CodeBlock visit(AssignmentStatement n, CodeBlock code_block) {
        String id_str = n.f0.f0.toString();

        ComplexType type = new VaporProgramBuilder().get_type(code_block.fun_decl.symbol_table_class_key,
                code_block.fun_decl.symbol_table_method_key,id_str);

        ClassTable class_table = VaporProgramBuilder.symbolTable.get(code_block.symbol_table_class_key);
        MethodTable method_table = class_table.methods.get(code_block.symbol_table_method_key);

        if(method_table.variable_present(id_str))
        {
            String var_name = code_block.fun_decl.symbol_table_class_key + "." +
                    code_block.fun_decl.symbol_table_method_key + "." + id_str;
            VaporIdentifier id = new VaporIdentifier(var_name);

            if(type == null)
            {
                throw new TypeException();
            }

            if(type.equals(ComplexType.INT))
            {
                Operand rhs = n.f2.accept(new ExpToIntVisitor(), code_block);
                IdEqualO instr = new IdEqualO(id, rhs);
                code_block.instructions.add(instr);
                return code_block;
            }
            if(type.equals(ComplexType.BOO))
            {
                Operand rhs = n.f2.accept(new ExpToBooVisitor(), code_block);
                IdEqualO instr = new IdEqualO(id, rhs);
                code_block.instructions.add(instr);
                return code_block;
            }

            if(type.equals(ComplexType.IARR))
            {
                Operand arr_id = n.f2.accept(new ExpToArrVisitor(), code_block);
                IdEqualO instr = new IdEqualO(id, arr_id);
                code_block.instructions.add(instr);
                return code_block;
            }

            Operand rhs = n.f2.accept(new ExpToComplexVisitor(), code_block);
            IdEqualO instr = new IdEqualO(id, rhs);
            code_block.instructions.add(instr);
            return code_block;
        }
        else
        {
            ComplexType class_containing = class_table.retrieve_class_containing_var(id_str,VaporProgramBuilder.symbolTable);
            String class_containing_str = class_containing.toString();
            Integer field_offset = class_table.total_fields_numbering.get(class_containing_str+"."+id_str) * 4;
            VaporIdentifier this_id = new VaporIdentifier("this");
            MemRef class_field = new MemRef(this_id, new VaporIntegerLiteral(field_offset.toString()));

            VaporIdentifier rhs;
            if(type.equals(ComplexType.INT))
            {
                rhs = n.f2.accept(new ExpToIntVisitor(), code_block);
            }
            else if(type.equals(ComplexType.BOO))
            {
                rhs = n.f2.accept(new ExpToBooVisitor(), code_block);
            }
            else if(type.equals(ComplexType.IARR))
            {
                rhs = n.f2.accept(new ExpToArrVisitor(), code_block);
            }
            else
            {
                rhs = n.f2.accept(new ExpToComplexVisitor(), code_block);
            }
            MemEqualID set_mem_instr = new MemEqualID(class_field,rhs);
            code_block.instructions.add(set_mem_instr);
            return code_block;
        }


    }


    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    public CodeBlock visit(ArrayAssignmentStatement n, CodeBlock code_block) {
        String int_arr_id_str = n.f0.f0.toString();
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

            // get the array index
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
            VaporIdentifier byte_offset = new VaporIdentifier("_byte_offset"+code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            IdEqualOperator set_byte_offset = new IdEqualOperator(byte_offset, num_to_byte);
            code_block.instructions.add(set_byte_offset);

            // get the array length
            VaporIdentifier temp_length = new VaporIdentifier("t" + code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            VaporIntegerLiteral zero_offset = new VaporIntegerLiteral("0");
            MemRef mem_ref = new MemRef(int_arr_full_id, zero_offset);
            IdEqualMem instr = new IdEqualMem(temp_length, mem_ref);
            code_block.instructions.add(instr);

            // check non-negativity
            VaporIntegerLiteral minus1 = new VaporIntegerLiteral("-1");
            LtS lts1 = new LtS(minus1, original_offset);
            VaporIdentifier non_negative_cond = new VaporIdentifier("_non_neg"+code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            IdEqualOperator id_eq_op_1 = new IdEqualOperator(non_negative_cond, lts1);
            code_block.instructions.add(id_eq_op_1);

            String out_of_bounds_str = class_name+"."+method_name+"."+"out_of_bounds";
            Label bounds_err_label = fun_decl.error_handling_blocks.get(out_of_bounds_str).first();
            If0_goto if_neg = new If0_goto(non_negative_cond, bounds_err_label);
            code_block.instructions.add(if_neg);

            // check index within bounds
            VaporIdentifier in_bounds = new VaporIdentifier("_in_bounds" + code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            LtS lts2 = new LtS(original_offset, temp_length);
            IdEqualOperator id_eq_op_2 = new IdEqualOperator(in_bounds, lts2);
            code_block.instructions.add(id_eq_op_2);

            If0_goto if_outta_bounds = new If0_goto(in_bounds, bounds_err_label);
            code_block.instructions.add(if_outta_bounds);

            // setting value
            Operand target_value = n.f5.accept(new ExpToIntVisitor(), code_block);
            VaporIdentifier target_value_temp_id = new VaporIdentifier("t" + code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            IdEqualO operand_to_id_instr = new IdEqualO(target_value_temp_id, target_value);
            code_block.instructions.add(operand_to_id_instr);

            VaporIdentifier new_starting_point = new VaporIdentifier("_new_starting_point"+code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;

            Add add_offset = new Add(int_arr_full_id, byte_offset);
            IdEqualOperator add_offset_instr = new IdEqualOperator(new_starting_point, add_offset);
            code_block.instructions.add(add_offset_instr);

            MemRef array_element = new MemRef(new_starting_point, new VaporIntegerLiteral("0"));
            MemEqualID set_element_instr = new MemEqualID(array_element, target_value_temp_id);
            code_block.instructions.add(set_element_instr);

            return code_block;
        }
        else
        {
            // field_offset is in terms of bytes
            ComplexType class_containing = class_table.retrieve_class_containing_var(int_arr_id_str,VaporProgramBuilder.symbolTable);
            String class_containing_str = class_containing.toString();
            Integer field_offset = class_table.total_fields_numbering.get(class_containing_str+"."+int_arr_id_str) * 4;
            VaporIdentifier this_id = new VaporIdentifier("this");
            VaporIdentifier arr_ptr_ptr = new VaporIdentifier("_arr_ptr_ptr"+code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;

            Add get_arr_pointer_address = new Add(this_id, new VaporIntegerLiteral(field_offset.toString()));
            IdEqualOperator set_arr_address_instr = new IdEqualOperator(arr_ptr_ptr, get_arr_pointer_address);
            code_block.instructions.add(set_arr_address_instr);

            // get the array pointer
            VaporIdentifier arr_ptr = new VaporIdentifier("_arr_ptr"+code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            MemRef arr_ptr_mem_ref = new MemRef(arr_ptr_ptr, new VaporIntegerLiteral("0"));
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
            VaporIdentifier byte_offset = new VaporIdentifier("_byte_offset"+code_block.fun_decl.temp_variable_counts.toString());
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
            VaporIdentifier non_negative_cond = new VaporIdentifier("_non_neg"+code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            IdEqualOperator id_eq_op_1 = new IdEqualOperator(non_negative_cond, lts1);
            code_block.instructions.add(id_eq_op_1);

            String out_of_bounds_str = class_name+"."+method_name+"."+"out_of_bounds";
            Label bounds_err_label = fun_decl.error_handling_blocks.get(out_of_bounds_str).first();
            If0_goto if_neg = new If0_goto(non_negative_cond, bounds_err_label);
            code_block.instructions.add(if_neg);

            // check index within bounds
            VaporIdentifier in_bounds = new VaporIdentifier("_in_bounds" + code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            LtS lts2 = new LtS(original_offset, temp_length);
            IdEqualOperator id_eq_op_2 = new IdEqualOperator(in_bounds, lts2);
            code_block.instructions.add(id_eq_op_2);

            If0_goto if_outta_bounds = new If0_goto(in_bounds, bounds_err_label);
            code_block.instructions.add(if_outta_bounds);

            // setting value
            Operand target_value = n.f5.accept(new ExpToIntVisitor(), code_block);
            VaporIdentifier target_value_temp_id = new VaporIdentifier("t" + code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            IdEqualO operand_to_id_instr = new IdEqualO(target_value_temp_id, target_value);
            code_block.instructions.add(operand_to_id_instr);

            VaporIdentifier new_starting_point = new VaporIdentifier("_new_starting_point"+code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;

            Add add_offset = new Add(arr_ptr, byte_offset);
            IdEqualOperator add_offset_instr = new IdEqualOperator(new_starting_point, add_offset);
            code_block.instructions.add(add_offset_instr);

            MemRef array_element = new MemRef(new_starting_point, new VaporIntegerLiteral("0"));
            MemEqualID set_element_instr = new MemEqualID(array_element, target_value_temp_id);
            code_block.instructions.add(set_element_instr);

            return code_block;
        }
    }


    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    public CodeBlock visit(IfStatement n, CodeBlock code_block) {
        // both the true and false blocks will come back to this label
        // after execution
        String class_name = code_block.symbol_table_class_key;
        String method_name = code_block.symbol_table_method_key;

        // creating the true false labels
        String else_str = class_name+"."+method_name+"."+"else"+code_block.fun_decl.label_counts.toString();
        code_block.fun_decl.label_counts++;
        String if_str   = class_name+"."+method_name+"."+"if"  +code_block.fun_decl.label_counts.toString();

        Label else_label = new Label(else_str);
        Label true_label = new Label(if_str);

        // creating the true false blocks
        CodeBlock true_block = new CodeBlock(code_block.fun_decl);
        CodeBlock false_block = new CodeBlock(code_block.fun_decl);

        // current block ends here
        // go to the if block
        code_block.jump = new GOTO(true_label);

        // create the merge label
        String end_if_str = class_name+"."+method_name+"."+"End_If"+ code_block.fun_decl.label_counts.toString();
        code_block.fun_decl.label_counts++;
        Label end_if_label = new Label(end_if_str);
        GOTO merge = new GOTO(end_if_label);

        Operand condition = n.f2.accept(new ExpToBooVisitor(), code_block);
        If0_goto if_instr = new If0_goto(condition, else_label);
        true_block.instructions.add(if_instr);
        Pair<Label, CodeBlock> true_pair = new Pair<Label, CodeBlock>(true_label, true_block);
        true_block.fun_decl.labeled_blocks.put(true_label.label, true_pair);

        // process the remaining block for the true condition
        true_block = n.f4.accept(this, true_block);
        true_block.jump = merge;

        //add the else block to the function declaration
        Pair<Label, CodeBlock> false_pair = new Pair<Label, CodeBlock>(else_label, false_block);
        code_block.fun_decl.labeled_blocks.put(else_label.label, false_pair);

        false_block = n.f6.accept(this, false_block);

        // set the goto label for the else block
        false_block.jump = merge;

        // create a new code block both of the branches jump to
        // and carry on from that block
        CodeBlock end_if_block = new CodeBlock(code_block.fun_decl);
        Pair<Label, CodeBlock> continuation_pair = new Pair<Label, CodeBlock>(end_if_label, end_if_block);
        code_block.fun_decl.labeled_blocks.put(end_if_str, continuation_pair);
        return end_if_block;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public CodeBlock visit(WhileStatement n, CodeBlock code_block) {
        String class_name = code_block.symbol_table_class_key;
        String method_name = code_block.symbol_table_method_key;

        // create the begin while label
        String begin_while_str = class_name+"."+method_name+"."+"while"+code_block.fun_decl.label_counts.toString();
        code_block.fun_decl.label_counts++;
        Label begin_while_label = new Label(begin_while_str);

        // add the jump for the current block to the while label
        // current code_block ends here
        code_block.jump = new GOTO(begin_while_label);

        CodeBlock while_block = new CodeBlock(code_block.fun_decl);
        Pair<Label,CodeBlock> new_pair = new Pair<Label, CodeBlock>(begin_while_label, while_block);
        code_block.fun_decl.labeled_blocks.put(begin_while_label.toString(),new_pair);

        // create the end_while label
        String end_while_str = class_name+"."+method_name+"."+"End_While"+code_block.fun_decl.label_counts.toString();
        code_block.fun_decl.label_counts++;
        Label end_while_label = new Label(end_while_str);

        Operand condition = n.f2.accept(new ExpToBooVisitor(), while_block);
        If0_goto while_instr = new If0_goto(condition,end_while_label);
        while_block.instructions.add(while_instr);

        while_block = n.f4.accept(this, while_block);
        while_block.jump = new GOTO(begin_while_label);

        CodeBlock end_while_block = new CodeBlock(code_block.fun_decl);
        Pair<Label, CodeBlock> end_while_pair = new Pair<Label, CodeBlock>(end_while_label, end_while_block);
        code_block.fun_decl.labeled_blocks.put(end_while_str, end_while_pair);

        return end_while_block;
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    public CodeBlock visit(PrintStatement n, CodeBlock code_block) {
        Operand o = n.f2.accept(new ExpToIntVisitor(), code_block);

        PrintIntS instr = new PrintIntS(o);
        code_block.instructions.add(instr);
        return code_block;
    }
}
