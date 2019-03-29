

/**
 * Created by mingzhangyong on 2019/3/29/029.
 */
public class TestMath {
    public static void main(String[] args ){
        int a = 1;
        int b = 10 ;
        System.out.println(" 1/10  =  " + a/b); //          1/10  =  0
        System.out.println(" 1/10 取上整 " + Math.ceil(a/b));//     1/10 取上整 0.0
        System.out.println(" 1/10  转double类型  " + (double)a/b); //          1/10  =  0.1
        System.out.println(" 1/10  转double类型取上整 " + Math.ceil((double) a/b));    //       1/10 取上整 1.0

        int c = 6;

        System.out.println(" 6/10  =  " + c/b); //          6/10  =  0
        System.out.println(" 6/10 取上整 " + Math.ceil(c/b));//    6/10 取上整 0.0
        System.out.println(" 6/10  转double类型  " + (double)c/b); //          6/10  转double类型  0.6
        System.out.println(" 6/10  转double类型取上整 " + Math.ceil((double) c/b));    //       6/10  转double类型取上整 1.0

        int d = 17 ;

        System.out.println(" 12/10  =  " + d/b); //          12/10  =  1
        System.out.println(" 12/10 取上整 " + Math.ceil(d/b));//     12/10 取上整 1.0
        System.out.println(" 12/10  转double类型  " + (double)d/b); //          12/10  转double类型  1.2
        System.out.println(" 12/10  转double类型取上整 " + Math.ceil((double) d/b));    //        12/10  转double类型取上整 2.0
    }
}
