package vaporprintvisitor;
import vaportree.*;
import java.util.*;

public class PrinterVisitor {
    private static Integer indent_level = 0;
    private static final String spaces = "  ";
    private void print_indent()
    {
        for (int i = 0; i < indent_level; ++i)
        {
            System.out.print(spaces);
        }
    }

    public void visit(Add n)
    {
        System.out.print("Add (");
        n.lhs.accept(this);
        System.out.print(" ");
        n.rhs.accept(this);
        System.out.print(")\n");
    }

    public void visit(CodeBlock n)
    {
        for (Instr instr: n.instructions)
        {
            instr.accept(this);
        }
        n.jump.accept(this);
    }

    public void visit(ConstDecl n)
    {
        System.out.print("const ");
        n.name.accept(this);
        System.out.print("\n");
        for (Label label: n.labels)
        {
            System.out.print("  :");
            label.accept(this);
            System.out.print("\n");
        }
        System.out.print("\n");
    }

    public void visit(Eq n)
    {
        System.out.print("Eq (");
        n.lhs.accept(this);
        System.out.print(" ");
        n.rhs.accept(this);
        System.out.print(")\n");
    }

    public void visit(Err n)
    {
        System.out.print("Error (");
        n.s.accept(this);
        System.out.print(")\n");
    }

    public void visit(FunDecl n)
    {
        System.out.print("func " + n.function_name_label.label + "( ");
        for(VaporIdentifier id: n.parameters)
        {
            id.accept(this);
            System.out.print(" ");
        }
        System.out.println(")");

        Pair<Label, CodeBlock> pair;
        for (Map.Entry<String, Pair<Label,CodeBlock> > entry :n.labeled_blocks.entrySet())
        {
            pair = entry.getValue();
            System.out.println(pair.first().label + ":");
            indent_level++;
            pair.second().accept(this);
            indent_level--;
        }
        for (Map.Entry<String, Pair<Label,CodeBlock> > entry :n.error_handling_blocks.entrySet())
        {
            System.out.print("\n");
            pair = entry.getValue();
            System.out.println(pair.first().label + ":");
            pair.second().accept(this);
        }
        System.out.print("\n");
    }

    public void visit(GOTO n)
    {
        System.out.print(spaces);
        System.out.print("goto :");
        n.label.accept(this);
        System.out.print("\n");
    }

    public void visit(HeapAllocZ n)
    {
        n.id.accept(this);
        System.out.print(" = HeapAllocZ ( ");
        n.o.accept(this);
        System.out.print(" )\n");
    }

    public void visit(VaporIdentifier n)
    {
        System.out.print(n.id);
    }

    public void visit(IdEqualCall n)
    {
        n.id.accept(this);
        System.out.print(" = call ");
        n.o.accept(this);
        System.out.print(" ( ");
        for(Operand operand: n.arguments)
        {
            operand.accept(this);
            System.out.print(" ");
        }
        System.out.print(")\n");
    }

    public void visit(IdEqualMem n)
    {
        n.id.accept(this);
        System.out.print(" = ");
        n.m.accept(this);
        System.out.print("\n");
    }

    public void visit(IdEqualO n)
    {
        n.id.accept(this);
        System.out.print(" = ");
        n.o.accept(this);
        System.out.print("\n");
    }

    public void visit(IdEqualOperator n)
    {
        System.out.print(n.id.id + " = " );
        n.op.accept(this);
    }

    public void visit(If0_goto n)
    {
        System.out.print("if0 ");
        n.o.accept(this);
        System.out.print(" goto :");
        n.l.accept(this);
        System.out.print("\n");
    }

    public void visit(Instr n)
    {}

    public void visit(Jump n)
    {}

    public void visit(Label n)
    {
        System.out.print(n.label);
    }


    public void visit(LtS n)
    {
        System.out.print("LtS (");
        n.o1.accept(this);
        System.out.print(" ");
        n.o2.accept(this);
        System.out.print(")\n");
    }

    public void visit(MemEqualID n)
    {
        n.m.accept(this);
        System.out.print(" = ");
        n.id.accept(this);
        System.out.print("\n");
    }

    public void visit(MemEqualLabel n)
    {
        n.mem_ref.accept(this);
        System.out.print(" = :");
        n.label.accept(this);
        System.out.print("\n");
    }
    public void visit(MemRef n)
    {
        System.out.print("[ ");
        n.id.accept(this);
        System.out.print(" + ");
        n.c.accept(this);
        System.out.print(" ]");
    }

    public void visit(MulS n)
    {
        System.out.print("MulS (");
        n.lhs.accept(this);
        System.out.print(" ");
        n.rhs.accept(this);
        System.out.print(")\n");
    }

    public void visit(Operand n)
    {
        n.accept(this);
    }

    public void visit(Operator n)
    {}

    public void visit(Pair n)
    {}

    public void visit(PrintIntS n)
    {
        System.out.print("PrintIntS (");
        n.o.accept(this);
        System.out.print(")\n");
    }

    public void visit(Program n)
    {
        for (ConstDecl cd: n.constant_declarations)
        {
            cd.accept(this);
        }
        FunDecl fd;
        for (Map.Entry<String, FunDecl> entry: n.function_declarations.entrySet())
        {
            fd = entry.getValue();
            fd.accept(this);
        }
    }
    public void visit(Ret n)
    {
        System.out.println("ret");
    }
    public void visit(RetOperand n)
    {
        System.out.print("ret ");
        n.o.accept(this);
        System.out.print("\n");
    }
    public void visit(StringLiteral n)
    {
        System.out.print("\""+ n.content + "\"");
    }

    public void visit(Sub n)
    {
        System.out.print("Sub (");
        n.lhs.accept(this);
        System.out.print(" ");
        n.rhs.accept(this);
        System.out.print(")\n");
    }
    public void visit(VaporIntegerLiteral n)
    {
        System.out.print(n.int_string);
    }

}
