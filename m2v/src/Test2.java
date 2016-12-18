/**
 * Created by Aaron on 11/6/16.
 */
class Test2 {
    public static void main(String[] arg)
    {
        E obj;
        obj = new E();
        System.out.println(obj.init());
    }

}

class D
{
    E e;
    int a;
    int b;
    int c;
    public int random()
    {
        boolean r;
        e=new E();
        r=e.a(99);
        return 9;
    }
}

class E extends D
{
    public boolean a(int p)
    {
        b=p;
        return true;
    }
    public int init()
    {
        a=1;
        b=2;
        c=2;
        while(a<40)
        {
            while(b<20)
            {
                if(c<50)
                {
                    c=c*c;
                    System.out.println(c);
                }
                else
                {
                    System.out.println(c*c);
                    c=c-10;
                    if(c<0)
                    {
                        c=2;
                        System.out.println(c);
                    }
                    else
                    {
                        System.out.println(this.random());
                    }
                }
                b=b*2;
            }
            a=a*3;
        }
        return 8;
    }
}

