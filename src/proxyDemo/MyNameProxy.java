package proxyDemo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by mingzhangyong on  2020/6/4 0004 17:27
 */
public class MyNameProxy implements InvocationHandler {
    private MyNameInterface myNameInterface;

    public MyNameProxy( MyNameInterface myNameInterface){
        this.myNameInterface = myNameInterface;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if(method.getName().equals("getMyName")){
            Object invoke = method.invoke(myNameInterface, args);
            String realResult = invoke.toString();

            System.out.println(" realResult ==  " + realResult);
            return  "  proxy result = hhhhhhhhh ";
        }else{
            return method.invoke(proxy, args);
        }
    }
}
