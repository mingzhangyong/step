package proxyDemo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Created by mingzhangyong on  2020/6/4 0004 17:40
 */
public class MainClass {
    public static void main(String args[]){
        MyNameInterface myNameInterface = new MyNameInterfaceImpl();
        InvocationHandler myNameProxy = new MyNameProxy(myNameInterface);
        MyNameInterface proxyMyName = (MyNameInterface) Proxy.newProxyInstance(myNameProxy.getClass().getClassLoader(),myNameInterface.getClass().getInterfaces(),myNameProxy);
//        proxyMyName.getMyName("mao");
        System.out.println(" 打印结果 --- " + proxyMyName.getMyName("mao"));
    }
}
