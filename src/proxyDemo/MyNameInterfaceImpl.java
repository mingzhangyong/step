package proxyDemo;

/**
 * Created by mingzhangyong on  2020/6/4 0004 17:26
 */
public class MyNameInterfaceImpl implements  MyNameInterface {
    @Override
    public String getMyName(String name) {
        return " my real name is " + name;
    }
}
