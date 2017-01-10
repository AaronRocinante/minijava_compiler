/**
 * Created by Aaron on 11/19/16.
 */
import cs132.vapor.ast.*;
import cs132.vapor.ast.VaporProgram;

import java.util.ArrayList;
import java.util.HashMap;

public class VaporMPrinter extends VInstr.Visitor<Bad> {
    public void visit(VaporMProgram n) throws Bad
    {
        for (VDataSegment data_segment: n.dataSegments)
        {
            this.visit(data_segment);
        }
        System.out.print("\n");

        for (VMFunction function: (VMFunction[])n.functions)
        {
            this.visit(function);
            System.out.print("\n");
        }
    }
    public void visit(VDataSegment n) throws Bad
    {
        System.out.print(n.mutable ? "var ":"const ");
        System.out.println(n.ident);
        for(VOperand.Static l: n.values)
        {
            System.out.println(l);
        }
    }

    public void visit(VMFunction n) throws Bad
    {
//        HashMap<Integer, String> block_label = new HashMap<Integer, String>();
//        for(VCodeLabel label: n.labels)
//        {
//            block_label.put(label.instrIndex,label.ident);
//        }
        System.out.print("func "+n.ident);
        System.out.print("[in "+n.stack.in+", out "+n.stack.out+", local "+n.stack.local+"]\n");

        Integer num_instrs = n.body.length;
        HashMap<Integer,ArrayList<String>> instr_index_to_label = n.instr_index_to_label;
        for(int i=0;i<num_instrs;i++)
        {
            VInstr instr = n.body[i];
//            if(block_label.get(i)!=null)
//            {
//                System.out.println(block_label.get(i)+":");
//            }
            ArrayList<String> labels = instr_index_to_label.get(i);
            if(labels!=null)
            {
                for(String label: labels)
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
    {
        System.out.println(n.ident);
    }

//    public void visit(VMemRef n)
//    {}

    public void visit(VMemRef.Global n)
    {
        if(n.base instanceof VAddr.Label)
        {
            System.out.print(n.base+" + "+n.byteOffset);
        }
        else
        {
            this.visit(((VAddr.Var)n.base).var);
            System.out.print(" + " + n.byteOffset);
        }
    }
    public void visit(VMemRef.Stack n)
    {
        if(n.region == VMemRef.Stack.Region.In)
        {
            System.out.print("in [");
        }
        else if(n.region == VMemRef.Stack.Region.Out)
        {
            System.out.print("out [");
        }
        else if(n.region == VMemRef.Stack.Region.Local)
        {
            System.out.print("local [");
        }
        System.out.print(n.index);
        System.out.print("]\n");
    }
    public void visit(VLabelRef n)
    {
        System.out.print(n.toString());
    }

    public void visit(VLitInt n)
    {
        System.out.print(n.toString());
    }

    public void visit(VLitStr n)
    {
        System.out.print(n.toString());
    }

    public void visit(VTarget n)
    {
        System.out.print(n.ident);
    }

    public void visit(VOperand n)
    {
        System.out.print(n.toString());
    }

    public void visit(VVarRef n)
    {
        if(n instanceof VVarRef.Local)
        {
            this.visit((VVarRef.Local)n);
        }
        else
        {
            this.visit((VVarRef.Register)n);
        }
    }

    public void visit(VVarRef.Local n)
    {
        System.out.print(n.ident + "["+n.index+"] ");
    }

    public void visit(VVarRef.Register n)
    {
        System.out.print(n.toString());
        System.out.print(n.index);
    }
    public void visit(VGoto n) throws Bad
    {
        System.out.print("goto ");
        System.out.print(n.target);
        System.out.print("\n");
    }
    public void visit(VAssign n)
    {
        if(n.dest instanceof VVarRef.Local)
        {
            this.visit((VVarRef.Local) n.dest);
        }
        else
        {
            this.visit((VVarRef.Register) n.dest);
        }
        System.out.print(" = ");
        if(n.source instanceof VVarRef)
        {
            if (n.source instanceof VVarRef.Local)
            {
                this.visit((VVarRef.Local) n.source);
            } else
            {
                this.visit((VVarRef.Register) n.source);
            }
        }
        else
        {
            this.visit(n.source);
        }
        System.out.print("\n");
    }

    public void visit(VCall n)
    {
        System.out.print("call ");
        if(n.addr instanceof VAddr.Label)
        {
            System.out.print(((VAddr.Label)n.addr).toString());
        }
        else
        {
            this.visit(((VAddr.Var)n.addr).var);
        }
        System.out.print("\n");
    }

    public void visit(VBranch n)
    {
        System.out.print(n.positive? "if ":"if0 ");
        if(n.value instanceof VOperand.Static || n.value instanceof VLitStr)
        {
            this.visit(n.value);
        }
        else
        {
            this.visit((VVarRef) n.value);
        }
        System.out.print(" goto ");
        this.visit(n.target);
        System.out.print("\n");
    }

    public void visit(VBuiltIn n)
    {
        if(n.dest != null)
        {
            if(n.dest instanceof VVarRef.Local)
            {
                this.visit((VVarRef.Local) n.dest);
            }
            else
            {
                this.visit((VVarRef.Register) n.dest);
            }
            System.out.print(" = ");
        }

        System.out.print(n.op.name + "( ");
        for (VOperand arg: n.args)
        {
            if(arg instanceof VVarRef)
            {
                if(arg instanceof VVarRef.Local)
                {
                    this.visit((VVarRef.Local) arg);
                }
                else
                {
                    this.visit((VVarRef.Register)arg);
                }
            }
            else
            {
                this.visit(arg);
            }
            System.out.print(" ");
        }
        System.out.print(")\n");
    }

    public void visit(VMemRead n)
    {
        if(n.dest instanceof VVarRef.Local)
        {
            this.visit((VVarRef.Local) n.dest);
        }
        else
        {
            this.visit((VVarRef.Register) n.dest);
        }
        System.out.print(" = [");
        if(n.source instanceof VMemRef.Global) {
            this.visit((VMemRef.Global) n.source);
        }
        else{
            this.visit((VMemRef.Stack)n.source);
        }
        System.out.print("]\n");
    }


    public void visit(VReturn n) throws Bad
    {
        System.out.print("ret ");
        if(n.value !=null) {
            this.visit(n.value);
        }
        System.out.print("\n");
    }

    public void visit(VMemWrite n)
    {
        System.out.print("[");

        if(n.dest instanceof VMemRef.Global) {
            this.visit((VMemRef.Global) n.dest);
        }
        else {
            this.visit((VMemRef.Stack) n.dest);
        }
        System.out.print("] = ");

        if(n.source instanceof VOperand.Static || n.source instanceof VLitStr)
        {
            this.visit(n.source);
        }
        else
        {
            if(n.source instanceof VVarRef.Local)
            {
                this.visit((VVarRef.Local) n.source);
            }
            else
            {
                this.visit((VVarRef.Register)n.source);
            }
        }
        System.out.print("\n");
    }
}

