import java.io.IOException;
import java.util.*;
import java.lang.System;

public class Parse{
    private boolean met_error = false, token_in_buffer = false;
    private int int_token_value;
    private enum Tokens{
        integer, plus, minus, left_paren, right_paren, dollar, incr, decr, eof, invalid_input
    }
    private Tokens token;
    private int token_buffer;
    private int line_num = 1;

    private static final int eof = -1;
    private static final String concat = " _", post_incr = " _++", post_decr = " _--",
                                pre_incr = " ++_", pre_decr = " --_";

    private static Queue<String> queue = new LinkedList<String>();

    private void eat (Tokens input) throws IOException{
        if (input == token){
            if (token != Tokens.eof){
                gettoken();
            }
        }
        else{
            error ();
        }
    }

    private void eat_int (int input) throws IOException{
        if (input == int_token_value){
            gettoken ();
        }
        else{
            error ();
        }
    }

    private void error (){
        if (!met_error) {
            met_error = true;
            System.out.format("Parse error in line %d\n",line_num);
            System.exit(1);
        }

    }

    private void gettoken () throws IOException{
        int a;
        if (token_in_buffer){
            token_in_buffer = false;
            a = token_buffer;
        }
        else{
            a = System.in.read();
        }
        if (a == eof){
            token = Tokens.eof;
            return;
        }
        while(a == ' ' || a == '\t' || a == '\n'){
            if (a == '\n'){
                int c;
                if (token_in_buffer){
                    c = token_buffer;
                    token_in_buffer = false;
                }
                else {
                    c = System.in.read();
                }
                if (c==-1)
                {
                    token = Tokens.eof;
                    return;
                }
                else
                {
                    token_in_buffer = true;
                    token_buffer = c;
                }
                ++line_num;
            }
            a = System.in.read();
            if (a == eof){
                token = Tokens.eof;
                return;
            }
        }
        if ('0'<= a && a<= '9'){
            token = Tokens.integer;
            int_token_value = a - '0';
            return;
        }
        switch (a){
            case '$':
                token =  Tokens.dollar;
                return;
            case '(':
                token =  Tokens.left_paren;
                return;
            case ')':
                token =  Tokens.right_paren;
                return;
            case '+':
            {
                int b = System.in.read();
                if (b == '+'){
                    token = Tokens.incr;
                    return;
                }
                token_buffer = b;
                token_in_buffer = true;
                token = Tokens.plus;
                return;
            }
            case '-':
            {
                int b = System.in.read();
                if (b == '-'){
                    token = Tokens.decr;
                    return;
                }
                token_buffer = b;
                token_in_buffer = true;
                token = Tokens.minus;
                return;
            }
            case '#':
            {
                int b = System.in.read();
                if (b == eof) {
                    token = Tokens.eof;
                    return;
                }
                while (b != '\n'){
                    b = System.in.read();
                    if (b == eof){
                        token = Tokens.eof;
                        return;
                    }
                }
                b = System.in.read();
                if (b==-1){
                    token = Tokens.eof;
                    return;
                }
                line_num++;
                token_buffer = b;
                token_in_buffer = true;
                gettoken();
                return;
            }
            default:
                token = Tokens.invalid_input;
        }
    }


    private boolean E_tilda () throws  IOException{
        gettoken();
        switch (token){
            case integer:
            case dollar:
            case left_paren:
            case incr:
            case decr:
                E_0 ();
                eat (Tokens.eof);
                break;
            default:
                error ();
        }
        return !met_error;
    }

    private void E_0 () throws IOException {
        switch (token){
            case integer:
            case dollar:
            case left_paren:
            case incr:
            case decr:
                E ();
                E_0_prime ();
                break;
            default:
                error ();
        }
    }

    private void E_0_prime () throws IOException{
        switch (token){
            case integer:
            case dollar:
            case left_paren:
                B_0 ();
                E_11 ();
                queue.add(concat);
                E_0_prime();
                break;

            case eof:
            case right_paren:
                break;

            default:
                error ();
        }
    }

    private void E_11 () throws IOException{
        switch (token){
            case integer:
            case dollar:
            case left_paren:
                E_12 ();
                E_prime ();
                break;

            default:
                error ();
        }
    }

    private void E_12 () throws IOException{
        switch (token){
            case integer:
            case dollar:
            case left_paren:
                E_2 ();
                break;

            default:
                error ();
        }
    }

    private void E () throws IOException{
        switch (token){
            case integer:
            case dollar:
            case left_paren:
            case incr:
            case decr:
                E_1 ();
                E_prime ();
                break;

            default:
                error ();
        }
    }

    private void E_prime () throws IOException{
        switch (token){
            case integer:
            case dollar:
            case left_paren:
            case right_paren:
            case eof:
                break;

            case plus:
            case minus:
                String operator = B ();
                E_1 ();
                queue.add (operator);
                E_prime ();
                break;

            default:
                error ();

        }
    }

    private void E_1 () throws IOException{
        switch (token){
            case integer:
            case dollar:
            case left_paren:
                E_2 ();
                break;

            case incr:
            case decr:
                Tokens operator = I ();
                E_1 ();
                if (operator == Tokens.incr){
                    queue.add (pre_incr);
                }
                if (operator == Tokens.decr){
                    queue.add (pre_decr);
                }
                break;

            default:
                error();
        }
    }

    private void E_2 () throws IOException{
        switch (token){
            case integer:
            case dollar:
            case left_paren:
                E_3 ();
                E_2_prime ();
                break;

            default:
                error();
        }
    }

    private void E_2_prime () throws IOException{
        switch (token){
            case integer:
            case dollar:
            case left_paren:
            case right_paren:
            case plus:
            case minus:
            case eof:
                break;

            case incr:
            case decr:
                Tokens operator = I ();
                if (operator == Tokens.incr) {
                    queue.add (post_incr);
                    E_2_prime ();
                }
                if (operator == Tokens.decr){
                    queue.add (post_decr);
                    E_2_prime ();
                }
                break;

            default:
                error ();
        }
    }

    private void E_3 () throws IOException{
        switch (token){
            case integer:
            case left_paren:
                E_4 ();
                break;

            case dollar:
                eat (Tokens.dollar);
                E_3 ();
                queue.add (" $");
                break;

            default:
                error ();
        }
    }

    private void E_4 () throws IOException{
        switch (token){
            case integer:
                N ();
                break;

            case left_paren:
                eat (Tokens.left_paren);
                E_0 ();
                eat (Tokens.right_paren);
                break;

            default:
                error ();
        }
    }

    private Tokens I () throws IOException{
        switch (token){
            case incr:
                eat (Tokens.incr);
                return Tokens.incr;

            case decr:
                eat (Tokens.decr);
                return Tokens.decr;

            default:
                error ();
                return Tokens.invalid_input;
        }
    }

    private String B () throws IOException{
        switch (token){
            case plus:
                eat (Tokens.plus);
                return " +";

            case minus:
                eat (Tokens.minus);
                return " -";

            default:
                error ();
                return "";
        }
    }

    private void N () throws IOException{
        switch (int_token_value){
            case 0:
                queue.add (" 0");
                eat_int (0);
                break;
            case 1:
                queue.add (" 1");
                eat_int (1);
                break;
            case 2:
                queue.add (" 2");
                eat_int (2);
                break;
            case 3:
                queue.add (" 3");
                eat_int (3);
                break;
            case 4:
                queue.add (" 4");
                eat_int (4);
                break;
            case 5:
                queue.add (" 5");
                eat_int (5);
                break;
            case 6:
                queue.add (" 6");
                eat_int (6);
                break;
            case 7:
                queue.add (" 7");
                eat_int (7);
                break;
            case 8:
                queue.add (" 8");
                eat_int (8);
                break;
            case 9:
                queue.add (" 9");
                eat_int (9);
                break;

            default:
                error ();
        }
    }

    private void B_0 () throws IOException{
        switch (token){
            case integer:
            case dollar:
            case left_paren:
                break;

            default:
                error ();
        }
    }

    public static void main(String[] args) throws Exception{
        Parse parser = new Parse();
        boolean success = parser.E_tilda();
        if (success){
            String out_string = queue.poll();
            while (out_string != null) {
                System.out.print (out_string);
                out_string = queue.poll();
            }
            System.out.print ("\nExpression parsed successfully\n");
        }
    }
}
