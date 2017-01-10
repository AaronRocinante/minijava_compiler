import cs132.vapor.ast.*;

/**
 * Created by Aaron on 11/21/16.
 */
public class VaporMProgram extends VaporProgram{
    public final VMFunction[] functions;
    public VaporMProgram(boolean allowLocals, String[] registers, boolean allowStack, VDataSegment[] dataSegments, VMFunction[] functions)
    {
        super(allowLocals,registers,allowStack,dataSegments,functions);
        this.functions = functions;
    }
}
