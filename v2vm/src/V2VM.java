/**
 * Created by Aaron on 11/19/16.
 */
import cs132.vapor.ast.*;
import cs132.util.ProblemException;
import cs132.vapor.parser.VaporParser;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.ast.VBuiltIn.Op;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;

public class V2VM {
    public static void main(String[] arg)
    {
        VaporProgram program;
        Op[] ops = {
                Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
                Op.PrintIntS, Op.HeapAllocZ, Op.Error,
        };
        boolean allowLocals = true;
        String[] registers = null;
        boolean allowStack = false;
        try
        {
            program = VaporParser.run(new InputStreamReader(System.in), 1, 1, java.util.Arrays.asList(ops),
                    allowLocals, registers, allowStack);
            VaporMTreeBuilder m_tree_builder = new VaporMTreeBuilder();
            m_tree_builder.visit(null, program);

            VaporMProgram vm_program = m_tree_builder.vapor_m_tree;
            new VaporMPrinter().visit(vm_program);

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
