/**
 * Created by Aaron on 10/28/16.
 */
class Test {
    public static void main(String[] arg) {
        C c;
        c = new C();
        System.out.println(c.init());
        System.out.println(c.test(999));
    }
}

class C extends B{
    B b;
    int[] arr;
    int a;
    boolean result;
//    public int init()
//    {
//        System.out.println(b.get_b_a());
//        return 23;
//    }

    public int get_arr()
    {
        return arr[0];
    }
    public int test(int n)
    {
        int[] arr;
        a = 23445;
        System.out.println(this.set_b_a(8));
        System.out.println(this.get_b_a());
        arr= this.set_a(0,27);
        System.out.println(arr[0]);
        System.out.println(a);
        return a;
    }
}

class A {
    int[] a;
    public int init()
    {
        a  = new int[100];
        return 2;
    }
    public int[] get_a()
    {
        return a;
    }
    public int[] set_a(int pos, int value)
    {
        a[pos] = value;
        return a;
    }
    public int test(int n)
    {
        System.out.println(111);
        return 222;
    }

}


class B extends A{
    int a;
    public int set_b_a(int v)
    {
        a=v;
        return 123;
    }
    public int get_b_a()
    {
        return a;
    }

}


