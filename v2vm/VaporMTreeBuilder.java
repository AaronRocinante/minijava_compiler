/**
 * Created by Aaron on 11/19/16.
 */
import cs132.util.SourcePos;
import cs132.vapor.ast.*;
import cs132.vapor.ast.VaporProgram;

import java.util.ArrayList;
import java.util.HashMap;

class Bad extends Throwable
{
}

public class VaporMTreeBuilder extends VInstr.VisitorP<VMFunction, Bad> {

    public boolean allowLocals = false;
    public String[] registers = {
            "v0", "v1",
            "a0", "a1", "a2", "a3",
            "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
            "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
            "t8",
    };
    public boolean allowStack = true;
    public ArrayList<VDataSegment> vm_data_segment = new ArrayList<VDataSegment>();
    public ArrayList<VMFunction> vm_functions = new ArrayList<VMFunction>();
    public VaporMProgram vapor_m_tree;

    // map vapor variable to the corresponding vapor m variable
    private VVarRef.Local transform_variable(VMFunction func, String id, SourcePos soursePos)
    {
        // search params first
        if(func.param_to_index!=null)
        {
            Integer index = func.param_to_index.get(id);
            if(index != null)
            {
                return new VVarRef.Local(soursePos, "in", index);
            }
        }
        Integer local_stack_index = func.id_to_local_index.get(id);
        return new VVarRef.Local(soursePos, "local", local_stack_index);
    }
    public void visit(VMFunction null_func, VaporProgram n) throws Bad
    {
        for (VDataSegment data_segment: n.dataSegments)
        {
            vm_data_segment.add(data_segment);
        }

        for (VFunction function: n.functions)
        {
            this.visit(null_func, function);
        }
        // convert the ArrayList instructions to an array for each function declaration
        for(VMFunction fun: vm_functions)
        {
            fun.body = fun.instructions.toArray(new VInstr[0]);
        }
       vapor_m_tree = new VaporMProgram(allowLocals,registers,allowStack,
               vm_data_segment.toArray(new VDataSegment[0]),vm_functions.toArray(new VMFunction[0]));
    }

    public void visit(VMFunction null_func, VFunction n) throws Bad
    {
        Integer num_vars = 0;
        if(n.vars != null)
        {
            num_vars = n.vars.length;
        }
        // in field in Stack
        Integer in = 0;
        if(n.params!=null)
        {
            in = n.params.length;
        }
        // local field in Stack
        Integer local = num_vars;
        CountMaxParamVisitor max_param_visitor = new CountMaxParamVisitor();
        max_param_visitor.visit(n);
        // out field in Stack
        Integer out = max_param_visitor.max_num_params;
        // now we have all three fields, we can create a Stack class
        VMFunction.Stack stack = new VMFunction.Stack(in, out, local);

        VMFunction function = new VMFunction(n.sourcePos, n.ident, n.index, null, stack);
        vm_functions.add(function);

        HashMap<String, Integer> id_to_local_index = new HashMap<String, Integer>();
        HashMap<Integer, ArrayList<String> > instr_label = new HashMap<Integer, ArrayList<String>>();
        // map params to their respective index
        for(int i=0; i<in; i++)
        {
            function.param_to_index.put(n.params[i].toString(), i);
        }
        for(int i=0;i<num_vars;i++)
        {
            id_to_local_index.put(n.vars[i],i);
        }
        function.id_to_local_index = id_to_local_index;
        for(VCodeLabel label: n.labels)
        {
            if(instr_label.get(label.instrIndex)==null)
            {
                ArrayList<String> label_array = new ArrayList<String>();
                label_array.add(label.ident);
                instr_label.put(label.instrIndex,label_array);
            }
            else
            {
                ArrayList<String> label_array = instr_label.get(label.instrIndex);
                label_array.add(label.ident);
            }
        }
        Integer num_instrs = n.body.length;
        for(int i=0;i<num_instrs;i++)
        {
            VInstr instr = n.body[i];
            ArrayList<String> labels = instr_label.get(i);
            if(labels!=null)
            {
                int index = function.instructions.size();
                function.instr_index_to_label.put(index,labels);
            }
            this.visit(function, instr);
        }
    }

    public void visit(VMFunction func, VInstr n) throws Bad
    {
        n.accept(func, this);
    }
    public void visit(VMFunction func, VCodeLabel n)
    {
    }

    public void visit(VMFunction func, VMemRef.Global n)
    {
    }

    public void visit(VMFunction func, VLabelRef n)
    {
    }

    public void visit(VMFunction func, VLitInt n)
    {
    }

    public void visit(VMFunction func, VLitStr n)
    {
    }

    public void visit(VMFunction func, VTarget n)
    {
    }

    public void visit(VMFunction func, VOperand n)
    {
    }

    public void visit(VMFunction func, VGoto n) throws Bad
    {
        func.instructions.add(n);
    }
    public void visit(VMFunction func, VAssign n)
    {
        if(n.source instanceof VOperand.Static || n.source instanceof VLitStr)
        {
            func.instructions.add(new VAssign(
                    n.sourcePos,
                    transform_variable(func, n.dest.toString(), n.dest.sourcePos),
                    n.source)
            );
        }
        else
        {
            VVarRef.Local local_source_var = transform_variable(func, n.source.toString(), n.source.sourcePos);
            VVarRef.Local local_dest_var = transform_variable(func, n.dest.toString(), n.dest.sourcePos);
            VVarRef.Register register_source = new VVarRef.Register(n.source.sourcePos,"v",1);
            func.instructions.add(new VAssign(
                    n.dest.sourcePos,
                    register_source,
                    local_source_var)
            );
            func.instructions.add(new VAssign(
                    n.sourcePos,
                    local_dest_var,
                    register_source)
            );
        }
    }

    public void visit(VMFunction func, VCall n)
    {
        // fill in the out stack first
        int num_args = n.args.length;
        for(int i=0;i<num_args;i++)
        {
            VOperand arg = n.args[i];
            VVarRef.Local out_arg = new VVarRef.Local(arg.sourcePos,"out",i);
            if(arg instanceof VOperand.Static || arg instanceof VLitStr)
            {
                func.instructions.add(new VAssign(arg.sourcePos, out_arg, arg));
            }
            else
            {
                VVarRef.Local local_var = transform_variable(func, arg.toString(),arg.sourcePos);
                VVarRef.Register register_temp = new VVarRef.Register(arg.sourcePos,"v",1);
                func.instructions.add(new VAssign(arg.sourcePos, register_temp, local_var));
                func.instructions.add(new VAssign(arg.sourcePos, out_arg, register_temp));
            }
        }
        if(n.addr instanceof VAddr.Label)
        {
            func.instructions.add(new VCall(n.sourcePos,n.addr, null, null));
        }
        else
        {
            VVarRef.Local local_func_addr = transform_variable(func,n.addr.toString(),n.sourcePos);
            VVarRef.Register register_func_addr = new VVarRef.Register(n.sourcePos, "v",1);
            func.instructions.add(new VAssign(n.sourcePos, register_func_addr, local_func_addr));

            VAddr.Var<VFunction> addr = new VAddr.Var<VFunction>(register_func_addr);
            func.instructions.add(new VCall(n.sourcePos, addr, null, null));
        }

        VVarRef.Register v0 = new VVarRef.Register(n.sourcePos, "v",0);
        if(n.dest != null)
        {
            func.instructions.add(new VAssign(
                    n.sourcePos,
                    transform_variable(func,n.dest.toString(),n.dest.sourcePos),
                    v0));
        }
    }

    public void visit(VMFunction func, VBranch n)
    {
        if(n.value instanceof VOperand.Static || n.value instanceof VLitStr)
        {
            func.instructions.add(n);
        }
        else
        {
            VVarRef.Local local_var = transform_variable(func, n.value.toString(), n.sourcePos);
            VVarRef.Register register_cond = new VVarRef.Register(n.sourcePos, "v", 1);
            func.instructions.add(new VAssign(
                    n.sourcePos,
                    register_cond,
                    local_var)
            );
            VBranch new_branch = new VBranch(n.sourcePos, n.positive, register_cond, n.target);
            func.instructions.add(new_branch);
        }
    }

    public void visit(VMFunction func, VBuiltIn n)
    {
        VVarRef.Local dest = null;
        VVarRef.Register temp_dest = null;
        if(n.dest != null)
        {
            dest = transform_variable(func, n.dest.toString(),n.dest.sourcePos);
            temp_dest = new VVarRef.Register(n.dest.sourcePos, "v", 1);
        }
        if(n.args!=null)
        {
            ArrayList<VOperand> new_args = new ArrayList<VOperand>();
            int num_args = n.args.length;
            for (int i = 0; i < num_args; i++)
            {
                new_args.add(new VVarRef.Register(n.args[i].sourcePos,"a",i));
            }
            for(int i = 0; i < num_args; i++)
            {
                VOperand arg = n.args[i];
                if (arg instanceof VOperand.Static || arg instanceof VLitStr)
                {
                    new_args.set(i,arg);
                    continue;
                }
                func.instructions.add(new VAssign(
                        arg.sourcePos,
                        (VVarRef.Register) new_args.get(i),
                        transform_variable(func, arg.toString(), arg.sourcePos)));
            }
            VBuiltIn built_in = new VBuiltIn(n.sourcePos, n.op, new_args.toArray(new VOperand[0]), temp_dest);
            func.instructions.add(built_in);
        }
        else
        {
            VBuiltIn built_in = new VBuiltIn(n.sourcePos, n.op, null, temp_dest);
            func.instructions.add(built_in);
        }
        if(dest != null)
        {
            func.instructions.add(new VAssign(n.dest.sourcePos, dest, temp_dest));
        }
    }

    public void visit(VMFunction func, VMemRead n)
    {
        VVarRef.Register temp_dest = new VVarRef.Register(n.sourcePos, "v",1);
        VAddr<VDataSegment> base = ((VMemRef.Global)n.source).base;
        int byte_offset = ((VMemRef.Global)n.source).byteOffset;
        if(base instanceof VAddr.Label)
        {
            func.instructions.add(new VMemRead(n.sourcePos, temp_dest, (VMemRef.Global) n.source));
        }
        else
        {
            VVarRef.Local local_base = transform_variable(func,((VAddr.Var)base).var.toString(), n.sourcePos);
            VVarRef.Register register_base = new VVarRef.Register(n.sourcePos, "v", 1);
            func.instructions.add(new VAssign(
                    n.sourcePos, register_base, local_base)
            );

            VAddr<VDataSegment> new_base = new VAddr.Var<VDataSegment>(register_base);
            VMemRef new_mem_ref = new VMemRef.Global(n.sourcePos, new_base, byte_offset);
            func.instructions.add(new VMemRead(n.sourcePos, temp_dest, new_mem_ref));

        }
        VVarRef.Local local_dest = transform_variable(func, n.dest.toString(), n.dest.sourcePos);
        func.instructions.add(new VAssign(n.sourcePos, local_dest, temp_dest));
    }

    public void visit(VMFunction func, VReturn n) throws Bad
    {
        if(n.value == null)
        {
            func.instructions.add(n);
        }
        else
        {
            VVarRef.Register v0 = new VVarRef.Register(n.value.sourcePos, "v", 0);
            if(n.value instanceof VOperand.Static || n.value instanceof VLitStr)
            {
                func.instructions.add(new VAssign(n.value.sourcePos, v0, n.value));
                func.instructions.add(new VReturn(n.sourcePos,null));
            }
            else
            {
                VVarRef.Local local_var = transform_variable(func, n.value.toString(), n.value.sourcePos);
                func.instructions.add(new VAssign(n.value.sourcePos, v0, local_var));
                func.instructions.add(new VReturn(n.sourcePos,null));
            }
        }
    }

    public void visit(VMFunction func, VMemWrite n)
    {
        VAddr<VDataSegment> base = ((VMemRef.Global)n.dest).base;
        int byte_offset = ((VMemRef.Global)n.dest).byteOffset;
        if(base instanceof VAddr.Label)
        {
            if(n.source instanceof VOperand.Static || n.source instanceof VLitStr)
            {
                func.instructions.add(new VMemWrite(n.sourcePos, (VMemRef.Global)n.dest, n.source));
            }
            else
            {
                func.instructions.add(new VMemWrite(n.sourcePos,
                        (VMemRef.Global) n.dest, transform_variable(func, n.source.toString(), n.sourcePos)));
            }
        }
        else
        {
            VVarRef.Local local_base = transform_variable(func,((VAddr.Var)base).var.toString(), n.sourcePos);
            VVarRef.Register register_base = new VVarRef.Register(n.sourcePos, "v", 1);
            func.instructions.add(new VAssign(
                    n.sourcePos, register_base, local_base)
            );

            VAddr<VDataSegment> new_base = new VAddr.Var<VDataSegment>(register_base);
            VMemRef new_mem_ref = new VMemRef.Global(n.sourcePos, new_base, byte_offset);
            if(n.source instanceof VOperand.Static || n.source instanceof VLitStr)
            {
                func.instructions.add(new VMemWrite(n.sourcePos, new_mem_ref, n.source));
            }
            else
            {
                VVarRef.Register register_source = new VVarRef.Register(n.sourcePos, "v", 0);
                func.instructions.add(new VAssign(
                        n.sourcePos,
                        register_source,
                        transform_variable(func, n.source.toString(), n.sourcePos))
                );
                func.instructions.add(new VMemWrite(n.sourcePos,
                        new_mem_ref, register_source));
            }
        }
    }
}
