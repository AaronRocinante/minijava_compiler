import syntaxtree.*;
import vaportree.*;
import visitor.GJDepthFirst;

/**
 * Created by Aaron on 10/30/16.
 *
 */
public class ExpToBooVisitor extends GJDepthFirst<VaporIdentifier, CodeBlock> {

    /**
     * f0 -> AndExpression()           done
     *       | CompareExpression()     done
     *                  |  PlusExpression()
     *                  | MinusExpression()
     *                  | TimesExpression()
     *                  |     ArrayLookup()
     *                  |     ArrayLength()
     *       | MessageSend()
     *       | PrimaryExpression()     done
     */
    public VaporIdentifier visit(Expression n, CodeBlock code_block) {
        return n.f0.accept(this,code_block);
    }
    /**
     * f0 -> PrimaryExpression()
     * f1 -> "&&"
     * f2 -> PrimaryExpression()
     */
    public VaporIdentifier visit(AndExpression n, CodeBlock code_block) {
        Operand lhs = n.f0.accept(this, code_block);
        Operand rhs = n.f2.accept(this, code_block);
        MulS and_instr = new MulS(lhs, rhs);
        VaporIdentifier result = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
        code_block.fun_decl.temp_variable_counts++;
        IdEqualOperator id_eq_op = new IdEqualOperator(result,and_instr);
        code_block.instructions.add(id_eq_op);
        return result;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public VaporIdentifier visit(CompareExpression n, CodeBlock code_block) {
        Operand lhs = n.f0.accept(new ExpToIntVisitor(), code_block);
        Operand rhs = n.f2.accept(new ExpToIntVisitor(), code_block);
        LtS less_instr = new LtS(lhs, rhs);
        VaporIdentifier result = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
        code_block.fun_decl.temp_variable_counts++;
        IdEqualOperator id_eq_op = new IdEqualOperator(result, less_instr);
        code_block.instructions.add(id_eq_op);
        return result;
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
     *       | TrueLiteral()                 done
     *       | FalseLiteral()                done
     *       | Identifier()                  done
     *                         | ThisExpression()
     *              | ArrayAllocationExpression()
     *                   | AllocationExpression()
     *       | NotExpression()               done
     *       | BracketExpression()           done
     */
    public VaporIdentifier visit(PrimaryExpression n, CodeBlock code_block) {
        return n.f0.accept(this, code_block);
    }

    /**
     * f0 -> "true"
     */
    public VaporIdentifier visit(TrueLiteral n, CodeBlock code_block) {
        VaporIntegerLiteral one = new VaporIntegerLiteral("1");
        VaporIdentifier id = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
        code_block.fun_decl.temp_variable_counts++;
        IdEqualO set_id = new IdEqualO(id,one);
        code_block.instructions.add(set_id);
        return id;
    }

    /**
     * f0 -> "false"
     */
    public VaporIdentifier visit(FalseLiteral n, CodeBlock code_block) {
        VaporIntegerLiteral one = new VaporIntegerLiteral("0");
        VaporIdentifier id = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
        code_block.fun_decl.temp_variable_counts++;
        IdEqualO set_id = new IdEqualO(id,one);
        code_block.instructions.add(set_id);
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
            String var_name = code_block.symbol_table_class_key + "." + code_block.symbol_table_method_key + "." + id_str;
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
     * f0 -> "!"
     * f1 -> Expression()
     */
    public VaporIdentifier visit(NotExpression n, CodeBlock code_block) {
        Operand original = n.f1.accept(new ExpToBooVisitor(), code_block);
        VaporIdentifier negated_variable = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
        code_block.fun_decl.temp_variable_counts++;
        VaporIntegerLiteral one = new VaporIntegerLiteral("1");
        Sub sub_instr = new Sub(one, original);
        IdEqualOperator id_eq_op = new IdEqualOperator(negated_variable, sub_instr );
        code_block.instructions.add(id_eq_op);
        return negated_variable;
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
