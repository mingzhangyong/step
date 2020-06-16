package spring.aspectJAOP;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mingzhangyong
 * @Created: 2020/6/15 0015 11:29
 * @E-mail: mingzhangyong@egova.com.cn
 */
public class OOM {
    static class OONObject{
        public OONObject(int i){
            System.out.println(i);
        }
    }

    public static void main(String[] args){
       /* List<OONObject> list = new ArrayList<>();

        for (int i = 0; i <100000000 ; i++) {
            OONObject object = new OONObject(i);
            list.add(i,object);
            list.add(i,null);
        }*/
       String str1 = new String("maoapian");
       String str2 = new String("java");
       System.out.println(str1.intern() == str1);
       System.out.println(str2.intern() == str2);
    }
}
