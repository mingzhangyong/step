package spring.aspectJAOP;

/**
 * @author mingzhangyong
 * @Created: 2020/6/9 0009 10:37
 * @E-mail: mingzhangyong@egova.com.cn
 */
public class HelloWorld {
    public void sayHello(){
        System.out.println("hello !");
    }
    public static void main(String args[]){
        HelloWorld helloWorld = new HelloWorld();
        helloWorld.sayHello();
    }
}
