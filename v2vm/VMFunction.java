import cs132.util.SourcePos;
import cs132.vapor.ast.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Aaron on 11/20/16.
 */
public class VMFunction extends VFunction {
    ArrayList<VInstr> instructions = new ArrayList<VInstr>();
    HashMap<String, Integer> id_to_local_index;
    HashMap<Integer, ArrayList<String> > instr_index_to_label = new HashMap<Integer,ArrayList<String>>();
    HashMap<String, Integer> param_to_index = new HashMap<String, Integer>();
    public VMFunction(SourcePos sourcePos, String ident, int index, VVarRef.Local[] params, Stack stack)
    {
        super(sourcePos, ident, index, params, stack);
    }

}
