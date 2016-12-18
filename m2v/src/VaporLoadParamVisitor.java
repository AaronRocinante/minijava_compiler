import syntaxtree.*;
import vaportree.*;
import visitor.GJVoidDepthFirst;

/**
 * Created by Aaron on 11/5/16.
 */
public class VaporLoadParamVisitor extends GJVoidDepthFirst<CodeBlock> {
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
    public void visit(Expression n, CodeBlock code_block)
    {
        n.f0.accept(this, code_block);
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "&&"
     * f2 -> PrimaryExpression()
     */
    public void visit(AndExpression n, CodeBlock code_block) {
        VaporIdentifier param_id = n.accept(new ExpToBooVisitor(), code_block);
        IdEqualCall instr = (IdEqualCall) code_block.current_instruction.peekLast();
        instr.arguments.add(param_id);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public void visit(CompareExpression n, CodeBlock code_block) {
        VaporIdentifier param_id = n.accept(new ExpToBooVisitor(), code_block);
        IdEqualCall instr = (IdEqualCall) code_block.current_instruction.peekLast();
        instr.arguments.add(param_id);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public void visit(PlusExpression n, CodeBlock code_block) {
        VaporIdentifier param_id = n.accept(new ExpToIntVisitor(), code_block);
        IdEqualCall instr = (IdEqualCall) code_block.current_instruction.peekLast();
        instr.arguments.add(param_id);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public void visit(MinusExpression n, CodeBlock code_block) {
        VaporIdentifier param_id = n.accept(new ExpToIntVisitor(), code_block);
        IdEqualCall instr = (IdEqualCall) code_block.current_instruction.peekLast();
        instr.arguments.add(param_id);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public void visit(TimesExpression n, CodeBlock code_block) {
        VaporIdentifier param_id = n.accept(new ExpToIntVisitor(), code_block);
        IdEqualCall instr = (IdEqualCall) code_block.current_instruction.peekLast();
        instr.arguments.add(param_id);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public void visit(ArrayLookup n, CodeBlock code_block) {
        VaporIdentifier param_id = n.accept(new ExpToIntVisitor(), code_block);
        IdEqualCall instr = (IdEqualCall) code_block.current_instruction.peekLast();
        instr.arguments.add(param_id);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public void visit(ArrayLength n, CodeBlock code_block) {
        VaporIdentifier param_id = n.accept(new ExpToIntVisitor(), code_block);
        IdEqualCall instr = (IdEqualCall) code_block.current_instruction.peekLast();
        instr.arguments.add(param_id);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public void visit(MessageSend n, CodeBlock code_block) {
        VaporIdentifier param_id = n.accept(new ExpToComplexVisitor(), code_block);
        IdEqualCall instr = (IdEqualCall) code_block.current_instruction.peekLast();
        instr.arguments.add(param_id);
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
    public void visit(PrimaryExpression n, CodeBlock code_block)
    {
        n.f0.accept(this,code_block);
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public void visit(IntegerLiteral n, CodeBlock code_block) {
        String int_str = n.f0.toString();
        VaporIntegerLiteral param_literal = new VaporIntegerLiteral(int_str);
        IdEqualCall instr = (IdEqualCall) code_block.current_instruction.peekLast();
        instr.arguments.add(param_literal);
    }

    /**
     * f0 -> "true"
     */
    public void visit(TrueLiteral n, CodeBlock code_block) {
        VaporIntegerLiteral one = new VaporIntegerLiteral("1");
        IdEqualCall instr = (IdEqualCall) code_block.current_instruction.peekLast();
        instr.arguments.add(one);
    }

    /**
     * f0 -> "false"
     */
    public void visit(FalseLiteral n, CodeBlock code_block) {
        VaporIntegerLiteral zero = new VaporIntegerLiteral("0");
        IdEqualCall instr = (IdEqualCall) code_block.current_instruction.peekLast();
        instr.arguments.add(zero);
    }


    /**
     * f0 -> <IDENTIFIER>
     */
    public void visit(Identifier n, CodeBlock code_block)
    {
        String class_name = code_block.symbol_table_class_key;
        String method_name = code_block.symbol_table_method_key;
        ClassTable class_table = VaporProgramBuilder.symbolTable.get(class_name);
        MethodTable method_table = class_table.methods.get(method_name);
        String param_id_str = n.f0.toString();
        IdEqualCall instr = (IdEqualCall) code_block.current_instruction.peekLast();

        if(method_table.variable_present(param_id_str))
        {
            String full_para_id_str = class_name+"."+method_name+"."+param_id_str;
            VaporIdentifier param_id = new VaporIdentifier(full_para_id_str);
            instr.arguments.add(param_id);
        }
        else
        {
            ComplexType class_containing = class_table.retrieve_class_containing_var(param_id_str,VaporProgramBuilder.symbolTable);
            String class_containing_str = class_containing.toString();
            Integer field_offset = class_table.total_fields_numbering.get(class_containing_str+"."+param_id_str) * 4;
            VaporIdentifier this_id = new VaporIdentifier("this");

            VaporIdentifier id_addr = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;

            Add add_id_addr = new Add(this_id, new VaporIntegerLiteral(field_offset.toString()));
            IdEqualOperator set_id_addr = new IdEqualOperator(id_addr, add_id_addr);
            code_block.instructions.add(set_id_addr);

            VaporIntegerLiteral zero_offset = new VaporIntegerLiteral("0");
            MemRef mem_ref = new MemRef(id_addr, zero_offset);

            VaporIdentifier field = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
            code_block.fun_decl.temp_variable_counts++;
            IdEqualMem get_result_instr = new IdEqualMem(field, mem_ref);
            code_block.instructions.add(get_result_instr);
            instr.arguments.add(field);
        }
    }

    /**
     * f0 -> "this"
     */
    public void visit(ThisExpression n, CodeBlock code_block) {
        VaporIdentifier this_id = new VaporIdentifier("this");
        IdEqualCall instr = (IdEqualCall) code_block.current_instruction.peekLast();
        instr.arguments.add(this_id);
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public void visit(ArrayAllocationExpression n, CodeBlock code_block) {
        Operand param = new VaporIdentifier("t"+code_block.fun_decl.temp_variable_counts.toString());
        code_block.fun_decl.temp_variable_counts++;

        param = n.f3.accept(new ExpToIntVisitor(),code_block);
        IdEqualCall instr = (IdEqualCall) code_block.current_instruction.peekLast();
        instr.arguments.add(param);
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public void visit(AllocationExpression n, CodeBlock code_block) {
        Operand param = n.accept(new ExpToComplexVisitor(), code_block);
        IdEqualCall instr = (IdEqualCall) code_block.current_instruction.peekLast();
        instr.arguments.add(param);
    }

    /**
     * f0 -> "!"
     * f1 -> Expression()
     */
    public void visit(NotExpression n, CodeBlock code_block) {
        Operand param = n.accept(new ExpToBooVisitor(),code_block);
        IdEqualCall instr = (IdEqualCall) code_block.current_instruction.peekLast();
        instr.arguments.add(param);
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public void visit(BracketExpression n, CodeBlock code_block) {
        n.f1.accept(this, code_block);
    }

}
