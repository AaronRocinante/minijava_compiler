/**
 * Created by Aaron on 10/5/16.
 */
import java.io.IOException;
import java.util.*;
import java.lang.System;

public class test {
    public  static void main(String[] arg) throws  IOException
    {
        int a;
        for (int i=0;i<5;++i) {
            System.out.println("i is " + i);
            a = System.in.read();
            System.out.println(a);
        }
    }
}
