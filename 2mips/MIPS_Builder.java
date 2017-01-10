/**
 * Created by Aaron on 11/22/16.
 */
import cs132.vapor.ast.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

public class MIPS_Builder extends VInstr.Visitor<Bad>{
    final String stack_pointer = "$sp";
    final String return_register = "$ra";
    final String v0 = "$v0";
    final String zero_register = "$zero";
    final String t9 = "$t9";
    final String a0 = "$a0";
    private Integer stack_frame_size = 0;
    private Integer out_byte_size = 0;
    HashMap<String, String> string_to_label = new HashMap<String, String>();
    private void indent()
    {
        System.out.print("  ");
    }

    private void heap_allocz()
    {
        indent();
        System.out.println("li $v0 9");
        indent();
        System.out.println("syscall");
    }

    private void print()
    {
        indent();
        System.out.println("li $v0 1");
        indent();
        System.out.println("syscall");
        indent();
        System.out.println("la $a0 _newline");
        indent();
        System.out.println("li $v0 4");
        indent();
        System.out.println("syscall");
    }

    private void error(String label)
    {
        indent();
        System.out.format("la $a0 %s\n",label);
        indent();
        System.out.println("li $v0 4");
        indent();
        System.out.println("syscall");
    }

    private void move(String dest, String source)
    {
        indent();
        System.out.format("move %s %s\n",dest, source);
    }

    private void addu(String dest, String s, String t)
    {
        indent();
        System.out.format("addu %s %s %s\n",dest, s,t);
    }

    private void subu(String dest, String source, String value)
    {
        indent();
        System.out.format("subu %s %s %s\n", dest, source, value);
    }

    private void mul(String dest, String s, String t)
    {
        indent();
        System.out.format("mul %s %s %s\n", dest, s, t);
    }

    private void sltu(String dest, String s, String t)
    {
        indent();
        System.out.format("sltu %s %s %s\n",dest, s, t);
    }

    private void slt(String dest, String s, String t)
    {
        indent();
        System.out.format("slt %s %s %s\n",dest,s,t);
    }

    private void slti(String dest, String s, String t)
    {
        indent();
        System.out.format("slti %s %s %s\n",dest, s, t);
    }

    private void beqz(String rs, String label)
    {
        indent();
        System.out.format("beqz %s %s\n", rs, label);
    }

    private void bnez(String rs, String label)
    {
        indent();
        System.out.format("bnez %s %s\n",rs,label);
    }
    private void xor(String dest, String s, String t)
    {
        indent();
        System.out.format("xor %s %s %s\n", dest, s, t);
    }

    private void xori(String dest, String s, String t)
    {
        indent();
        System.out.format("xori %s %s %s\n",dest, s,t);
    }

    private void jal(String label)
    {
        indent();
        System.out.format("jal %s\n",label);
    }

    private void jalr(String register)
    {
        indent();
        System.out.format("jalr %s\n",register);
    }

    private void lw(String dest,String offset, String base)
    {
        indent();
        System.out.format("lw %s %s(%s)\n",dest,offset,base);
    }

    private void sw(String source, String offset, String dest_base)
    {
        indent();
        System.out.format("sw %s %s(%s)\n", source, offset, dest_base);
    }

    private void la(String dest, String label_addr)
    {
        indent();
        System.out.format("la %s %s\n", dest, label_addr);
    }

    private void li(String dest, String immediate)
    {
        indent();
        System.out.format("li %s %s\n", dest, immediate);
    }

    private void jump(String label)
    {
        indent();
        System.out.format("j %s\n",label);
    }

    private void jr(String register)
    {
        indent();
        System.out.format("jr %s\n", register);
    }

    public void visit(VaporProgram n) throws Bad
    {
        System.out.println(".data\n");
        for (VDataSegment data_segment: n.dataSegments)
        {
            this.visit(data_segment);
        }
        System.out.println("\n.text\n");
        jal("Main");
        li(v0, String.valueOf(10));
        indent();
        System.out.println("syscall");
        System.out.print("\n");

        for (VFunction function: n.functions)
        {
            this.visit(function);
            System.out.print("\n");
        }

        System.out.println(".data");
        System.out.println(".align 0");
        System.out.println("_newline: .asciiz \"\\n\"");
        for(Map.Entry<String,String> pair: string_to_label.entrySet())
        {
            String original_string = pair.getKey();
            String string = original_string.substring(0,original_string.length()-1)+"\\n\"";
            System.out.format("%s: .asciiz %s\n",pair.getValue(),string);
        }
        System.out.print("\n");
    }

    public void visit(VDataSegment n) throws Bad
    {
        System.out.println(n.ident + ":");
        for(VOperand.Static l: n.values)
        {
            String mips_label = l.toString().substring(1);
            indent();
            System.out.println(mips_label);
        }
    }

    public void visit(VFunction n) throws Bad
    {
        HashMap<Integer, ArrayList<String> > block_label = new HashMap<Integer, ArrayList<String>>();
        for(VCodeLabel label: n.labels)
        {
            if(block_label.get(label.instrIndex)==null)
            {
                ArrayList<String> new_list = new ArrayList<String>();
                new_list.add(label.ident);
                block_label.put(label.instrIndex, new_list);
            }
            else
            {
                ArrayList<String> label_list = block_label.get(label.instrIndex);
                label_list.add(label.ident);
            }
        }

        // 4 more byte for the return address
        Integer local_byte_size = (n.stack.local + 1) * 4;
        Integer out_byte_size = n.stack.out * 4;
        Integer stack_frame_size = local_byte_size + out_byte_size;
        this.stack_frame_size = stack_frame_size;
        this.out_byte_size = out_byte_size;

        System.out.println(n.ident + ":");
        subu(stack_pointer, stack_pointer, stack_frame_size.toString());
        sw(return_register, String.valueOf(out_byte_size), stack_pointer);

        Integer num_instrs = n.body.length;
        for(int i=0;i<num_instrs;i++)
        {
            VInstr instr = n.body[i];
            ArrayList<String> label_list = block_label.get(i);
            if(label_list!=null)
            {
                for(String label: label_list)
                {
                    System.out.println(label + ":");
                }
            }
            this.visit(instr);
        }
    }

    public void visit(VInstr n) throws Bad
    {
        n.accept(this);
    }

    public void visit(VCodeLabel n)
    {}
    public void visit(VMemRef.Global n)
    {}
    public void visit(VMemRef.Stack n)
    {}
    public void visit(VLabelRef n)
    {}
    public void visit(VLitInt n)
    {}
    public void visit(VLitStr n)
    {}
    public void visit(VTarget n)
    {}
    public void visit(VOperand n)
    {}
    public void visit(VVarRef n)
    {}
    public void visit(VVarRef.Local n)
    {}
    public void visit(VVarRef.Register n)
    {}

    public void visit(VGoto n)
    {
        if(n.target instanceof VAddr.Label)
        {
            jump(((VAddr.Label)n.target).label.ident);
        }
        else    // VAddr.Var i.e. register
        {
            jr(((VVarRef.Register)((VAddr.Var)n.target).var).ident);
        }
    }

    public void visit(VAssign n)
    {
        // destination register
        String dest = ((VVarRef.Register)n.dest).toString();
        String source;
        if(n.source instanceof VVarRef.Register)
        {
            source = ((VVarRef.Register) n.source).toString();
            move(dest, source);
        }
        else if(n.source instanceof VLabelRef)
        {
            source = ((VLabelRef) n.source).ident;
            la(dest, source);
        }
        else if(n.source instanceof VLitInt)
        {
            source = String.valueOf(((VLitInt)n.source).value);
            li(dest, source);
        }
        else if(n.source instanceof VLitStr)
        {
            String label = string_to_label.get(((VLitStr)n.source).value);
            if(label == null)
            {
                Integer index = string_to_label.size();
                String new_label = "str"+index.toString();
                string_to_label.put(((VLitStr)n.source).value, new_label);
                la(dest, new_label);
            }
            else
            {
                la(dest, label);
            }
        }
    }

    public void visit(VCall n)
    {
        if(n.addr instanceof VAddr.Label)
        {
            String label = ((VAddr.Label)n.addr).label.ident;
            jal(label);
        }
        else
        {
            String register = "$"+((VVarRef.Register)((VAddr.Var)n.addr).var).ident;
            jalr(register);
        }
    }

    public void visit(VBranch n)
    {
        if(n.positive) //if goto
        {
            bnez(n.value.toString(),n.target.ident);
        }
        else    // if0 goto
        {
            beqz(n.value.toString(),n.target.ident);
        }
    }

    public void visit(VBuiltIn n)
    {
        String dest = null;
        if(n.dest != null)
        {
            dest = "$"+((VVarRef.Register) n.dest).ident ;
        }
        String op_name = n.op.name;
        if(op_name.equals(VBuiltIn.Op.HeapAllocZ.name))
        {
            VOperand arg = n.args[0];
            if(arg instanceof VVarRef.Register)
            {
                move(a0, arg.toString());
            }
            else if(arg instanceof VLitInt)
            {
                li(a0, arg.toString());
            }
            heap_allocz();
            if(dest !=null)
            {
                move(dest, v0);
            }
        }
        else if(op_name.equals(VBuiltIn.Op.Add.name))
        {
            String s;
            String t;
            VOperand first_arg = n.args[0];
            VOperand second_arg = n.args[1];
            if(first_arg instanceof VLitInt)
            {
                li(t9,first_arg.toString());
                s = t9;
                if(second_arg instanceof VLitInt)
                {
                    li(a0, second_arg.toString());
                    t = a0;
                }
                else //if(second_arg instanceof VVarRef.Register)
                {
                    t = second_arg.toString();
                }
            }
            else // if (first_arg instanceof VVarRef.Register)
            {
                s = first_arg.toString();
                if(second_arg instanceof VLitInt)
                {
                    li(t9,second_arg.toString());
                    t = t9;
                }
                else    // if(second_arg instanceof VVarRef.Register)
                {
                    t = second_arg.toString();
                }
            }
            addu(dest, s, t);
        }
        else if(op_name.equals(VBuiltIn.Op.Sub.name))
        {
            String s;
            String t;
            VOperand first_arg = n.args[0];
            VOperand second_arg = n.args[1];
            if(first_arg instanceof VLitInt)
            {
                li(t9,first_arg.toString());
                s = t9;
                if(second_arg instanceof VLitInt)
                {
                    li(a0, second_arg.toString());
                    t = a0;
                }
                else //if(second_arg instanceof VVarRef.Register)
                {
                    t = second_arg.toString();
                }
            }
            else // if (first_arg instanceof VVarRef.Register)
            {
                s = first_arg.toString();
                if(second_arg instanceof VLitInt)
                {
                    li(t9,second_arg.toString());
                    t = t9;
                }
                else    // if(second_arg instanceof VVarRef.Register)
                {
                    t = second_arg.toString();
                }
            }
            subu(dest, s, t);
        }
        else if(op_name.equals(VBuiltIn.Op.MulS.name))
        {
            String s;
            String t;
            VOperand first_arg = n.args[0];
            VOperand second_arg = n.args[1];
            if(first_arg instanceof VLitInt)
            {
                li(t9,first_arg.toString());
                s = t9;
                if(second_arg instanceof VLitInt)
                {
                    li(a0, second_arg.toString());
                    t = a0;
                }
                else //if(second_arg instanceof VVarRef.Register)
                {
                    t = second_arg.toString();
                }
            }
            else // if (first_arg instanceof VVarRef.Register)
            {
                s = first_arg.toString();
                if(second_arg instanceof VLitInt)
                {
                    li(t9,second_arg.toString());
                    t = t9;
                }
                else    // if(second_arg instanceof VVarRef.Register)
                {
                    t = second_arg.toString();
                }
            }
            mul(dest, s, t);
        }
        else if(op_name.equals(VBuiltIn.Op.Eq.name))
        {
            VOperand first_arg = n.args[0];
            VOperand second_arg = n.args[1];
            if(second_arg instanceof VVarRef.Register)
            {
                if(first_arg instanceof VVarRef.Register)
                {
                    // take the difference
                    xor(dest,first_arg.toString(),second_arg.toString());
                    // see if it's non-zero
                    sltu(dest, zero_register, dest);
                    // take the complement
                    xori(dest, dest, "1");
                }
                else if(first_arg instanceof VLitInt)
                {
                    li(t9, first_arg.toString());
                    xor(dest,t9,second_arg.toString());
                    sltu(dest,zero_register,dest);
                    xori(dest,dest,"1");
                }
            }
            else if(second_arg instanceof VLitInt)
            {
                if(first_arg instanceof VVarRef.Register)
                {
                    xori(dest, first_arg.toString(), second_arg.toString());
                    sltu(dest, zero_register, dest);
                    xori(dest,dest,"1");
                }
                else if(first_arg instanceof VLitInt)
                {
                    li(t9, first_arg.toString());
                    xori(dest, t9, second_arg.toString());
                    sltu(dest, zero_register, dest);
                    xori(dest, dest, "1");
                }
            }
        }

        else if(op_name.equals(VBuiltIn.Op.Lt.name))
        {
            VOperand first_arg = n.args[0];
            VOperand second_arg = n.args[1];
            if(second_arg instanceof VVarRef.Register)
            {
                if(first_arg instanceof VVarRef.Register)
                {
                    sltu(dest, first_arg.toString(), second_arg.toString());
                }
                else if(first_arg instanceof VLitInt)
                {
                    li(t9,first_arg.toString());
                    sltu(dest, t9, second_arg.toString());
                }
            }
            else if(second_arg instanceof VLitInt)
            {
                if(first_arg instanceof VVarRef.Register)
                {
                    slti(dest, first_arg.toString(), second_arg.toString());
                }
                else if(first_arg instanceof VLitInt)
                {
                    li(t9,first_arg.toString());
                    slti(dest, t9, second_arg.toString());
                }
            }
        }
        else if(op_name.equals(VBuiltIn.Op.LtS.name))
        {
            VOperand first_arg = n.args[0];
            VOperand second_arg = n.args[1];
            if(second_arg instanceof VVarRef.Register)
            {
                if(first_arg instanceof VVarRef.Register)
                {
                    slt(dest, first_arg.toString(), second_arg.toString());
                }
                else if(first_arg instanceof VLitInt)
                {
                    li(t9,first_arg.toString());
                    slt(dest, t9, second_arg.toString());
                }
            }
            else if(second_arg instanceof VLitInt)
            {
                if(first_arg instanceof VVarRef.Register)
                {
                    slti(dest, first_arg.toString(), second_arg.toString());
                }
                else if(first_arg instanceof VLitInt)
                {
                    li(t9,first_arg.toString());
                    slt(dest, t9, second_arg.toString());
                }
            }
        }
        else if(op_name.equals(VBuiltIn.Op.PrintIntS.name))
        {
            VOperand arg = n.args[0];
            if(arg instanceof VVarRef.Register)
            {
                move(a0, ((VVarRef.Register)arg).toString());
            }
            else if(arg instanceof VLitInt)
            {
                li(a0, arg.toString());
            }
            print();
        }
        else if(op_name.equals(VBuiltIn.Op.Error.name))
        {
            String message = n.args[0].toString();
            String error_label = string_to_label.get(message);
            if(error_label != null)
            {
                error(error_label);
            }
            else
            {
                Integer string_index = string_to_label.size();
                String new_error_label = "str"+string_index.toString();
                string_to_label.put(message, new_error_label);
                error(new_error_label);
            }
        }
    }

    public void visit(VMemRead n)
    {
        String dest = ((VVarRef.Register)n.dest).toString();
        if(n.source instanceof VMemRef.Global) {
            String offset = String.valueOf(((VMemRef.Global) n.source).byteOffset);
            String base;
            if(((VMemRef.Global)n.source).base instanceof VAddr.Label)
            {
                base = ((VAddr.Label)((VMemRef.Global)n.source).base).label.ident;
            }
            else    // VAddr.Var
            {
                base = ((VVarRef.Register)((VAddr.Var)((VMemRef.Global)n.source).base).var).toString();
            }
            lw(dest, offset, base);
        }
        else    //VMemRef.Stack
        {
            String byte_offset;
            if(((VMemRef.Stack)n.source).region == VMemRef.Stack.Region.Local)
            {
                // 4 more bytes for the return addr
                byte_offset = String.valueOf(((VMemRef.Stack)n.source).index * 4 + out_byte_size + 4);
            }
            else if(((VMemRef.Stack)n.source).region == VMemRef.Stack.Region.In)
            {
                byte_offset = String.valueOf(((VMemRef.Stack)n.source).index * 4 + this.stack_frame_size);
            }
            else    // out
            {
                byte_offset = String.valueOf(((VMemRef.Stack)n.source).index * 4);
            }
            lw(dest, byte_offset, stack_pointer);
        }
    }

    public void visit(VMemWrite n)
    {
        String base;
        String offset;
        if(n.dest instanceof VMemRef.Global)
        {
            offset = String.valueOf(((VMemRef.Global) n.dest).byteOffset);
            if(((VMemRef.Global)n.dest).base instanceof VAddr.Label)
            {
                base = ((VAddr.Label)((VMemRef.Global)n.dest).base).label.ident;
            }
            else
            {
                base = ((VVarRef.Register)((VAddr.Var)((VMemRef.Global)n.dest).base).var).toString();
            }
        }
        else
        {
            // VMemRef.Stack
            base = stack_pointer;
            if(((VMemRef.Stack)n.dest).region == VMemRef.Stack.Region.Local)
            {
                // 4 more bytes for the return addr
                offset = String.valueOf(((VMemRef.Stack)n.dest).index * 4 + out_byte_size + 4);
            }
            else if(((VMemRef.Stack)n.dest).region == VMemRef.Stack.Region.In)
            {
                offset = String.valueOf(((VMemRef.Stack)n.dest).index * 4 + this.stack_frame_size);
            }
            else
            {
                offset = String.valueOf(((VMemRef.Stack)n.dest).index * 4);
            }
        }
        String source;
        if(n.source instanceof VLabelRef)
        {
            String label_addr = ((VLabelRef)n.source).ident;
            // load the label to the temporary register $t9 first
            la(t9, label_addr);
            source = t9;
        }
        else if(n.source instanceof VLitInt)
        {
            li(t9, n.source.toString());
            source = t9;
        }
        else if(n.source instanceof VLitStr)
        {
            String label = string_to_label.get(n.source.toString());
            if(label == null)
            {
                Integer index = string_to_label.size();
                String new_label = "str"+index.toString();
                string_to_label.put(n.source.toString(),new_label);
                la(t9, new_label);
            }
            else
            {
                la(t9,label);
            }
            source = t9;
        }
        else
        {
            source = n.source.toString();
        }
        sw(source, offset, base);
    }

    public void visit(VReturn n) throws Bad
    {
        if(n.value !=null) {
            if(n.value instanceof VLitInt)
            {
                li(v0, String.valueOf((VLitInt)n.value));
            }
            else if(n.value instanceof VVarRef)
            {
                move(v0,((VVarRef.Register)n.value).ident);
            }
            else if(n.value instanceof VLitStr)
            {
                String label = n.value.toString();
                if(label==null)
                {
                    Integer index = string_to_label.size();
                    String new_label = "str"+index.toString();
                    string_to_label.put(n.value.toString(), new_label);
                    la(v0,new_label);
                }
                else
                {
                    la(v0, label);
                }
            }
            else if(n.value instanceof VLabelRef)
            {
                la(v0, ((VLabelRef)n.value).ident);
            }
        }
        String offset = String.valueOf(this.out_byte_size);
        lw(return_register, offset, stack_pointer);
        addu(stack_pointer, stack_pointer, String.valueOf(this.stack_frame_size));
        jr(return_register);
    }
}
