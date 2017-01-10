import cs132.vapor.ast.*;

/**
 * Created by Aaron on 11/20/16.
 */
public class CountMaxParamVisitor extends VInstr.Visitor< Bad> {
    Integer max_num_params = 0;
    public void visit(VFunction n) throws  Bad
    {
        for(VInstr instr: n.body)
        {
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

    public void visit(VGoto n)
    {}

    public void visit(VAssign n)
    {}

    public void visit(VCall n)
    {
        Integer num_params = n.args.length;
        if(num_params>max_num_params)
        {
            max_num_params = num_params;
        }
    }

    public void visit(VBranch n)
    {}

    public void visit(VBuiltIn n)
    {}

    public void visit(VMemRead n)
    {}

    public void visit(VReturn n)
    {}

    public void visit(VMemWrite n)
    {}
}
