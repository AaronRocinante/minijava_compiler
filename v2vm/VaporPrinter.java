/**
 * Created by Aaron on 11/19/16.
 */
import cs132.vapor.ast.*;
import cs132.vapor.ast.VaporProgram;

import java.util.ArrayList;
import java.util.HashMap;

public class VaporPrinter extends VInstr.Visitor<Bad> {
    public void visit(VaporProgram n) throws Bad
    {
        for (VDataSegment data_segment: n.dataSegments)
        {
            this.visit(data_segment);
        }

        for (VFunction function: n.functions)
        {
            this.visit(function);
        }
    }
    public void visit(VDataSegment n) throws Bad
    {
        System.out.print(n.mutable?"var ":"const ");
        System.out.println(n.ident);
        for(VOperand.Static l: n.values)
        {
            System.out.println(l);
        }
    }

    public void visit(VFunction n) throws Bad
    {
        Integer num_vars = n.vars.length;
        HashMap<Integer, String> block_label = new HashMap<Integer, String>();
        for(VCodeLabel label: n.labels)
        {
            block_label.put(label.instrIndex,label.ident);
        }
        System.out.print("func "+n.ident);
        System.out.print("( ");
        for(VVarRef.Local param: n.params)
        {
            System.out.print(param + " ");
        }
        System.out.print(")\n");
        Integer num_blocks = n.body.length;
        for(int i=0;i<num_blocks;i++)
        {
            VInstr instr = n.body[i];
            if(block_label.get(i)!=null)
            {
                System.out.println(block_label.get(i)+":");
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
        System.out.print(n.base+" + "+n.byteOffset);
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

    public void visit(VGoto n) throws Bad
    {
        System.out.print("goto ");
        System.out.print(n.target);
        System.out.print("\n");
    }
    public void visit(VAssign n)
    {
        this.visit(n.dest);
        System.out.print(" = ");
        this.visit(n.source);
        System.out.print("\n");
    }

    public void visit(VCall n)
    {
        if(n.dest!=null)
        {
            this.visit(n.dest);
            System.out.print(" = ");
        }
        System.out.print("call ");
        System.out.print(n.addr.toString()+" ( ");
        for(VOperand arg:n.args)
        {
            this.visit(arg);
            System.out.print(" ");
        }
        System.out.print(")\n");
    }

    public void visit(VBranch n)
    {
        System.out.print(n.positive? "If ":"If0 ");
        this.visit(n.value);
        System.out.print(" goto ");
        this.visit(n.target);
        System.out.print("\n");
    }

    public void visit(VBuiltIn n)
    {
        if(n.dest != null) {
            this.visit(n.dest);
            System.out.print(" = ");
        }

        System.out.print(n.op.name + "( ");
        for (VOperand arg: n.args)
        {
            this.visit(arg);
            System.out.print(" ");
        }
        System.out.print(")\n");

    }


    public void visit(VMemRead n)
    {
        this.visit(n.dest);
        System.out.print(" = [");
        this.visit((VMemRef.Global) n.source);
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

        this.visit((VMemRef.Global) n.dest);
        System.out.print("] = ");

        this.visit(n.source);
        System.out.print("\n");
    }
}

