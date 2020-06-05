package runTime;

/**
 * Created by mingzhangyong on  2020/6/5 0005 9:50
 */
public class RunTimeTest {
    public static void main(String args[]){
        Animal a = new Bird("mao");
        System.out.println(" 编译时类型 -- " + a.myName);
        System.out.println(" 运行时类型 -- " + a.getMyName());
    }
}
