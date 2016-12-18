/**
 * Created by Aaron on 11/22/16.
 */
import cs132.util.ProblemException;
import cs132.vapor.parser.VaporParser;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.ast.VBuiltIn.Op;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;

class Bad extends Throwable
{
}

public class VM2M {
    public static void main(String[] arg)
    {
        Op[] ops = {
                Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
                Op.PrintIntS, Op.HeapAllocZ, Op.Error,
        };
        boolean allowLocals = false;
        String[] registers = {
                "v0", "v1",
                "a0", "a1", "a2", "a3",
                "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
                "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
                "t8",
        };
        boolean allowStack = true;
        VaporProgram program;
        try
        {
            program = VaporParser.run(new InputStreamReader(System.in), 1, 1, java.util.Arrays.asList(ops),
                    allowLocals, registers, allowStack);

            MIPS_Builder builder = new MIPS_Builder();
            builder.visit(program);

        } catch (ProblemException ex)
        {
            System.out.println("parse error");
            System.exit(1);
        } catch (IOException e)
        {
            System.out.println("IOException");
            System.exit(1);
        }
        catch (Bad b)
        {
            System.out.println("bad things happened");
            System.exit(1);
        }
    }


}
