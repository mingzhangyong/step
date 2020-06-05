package reflex;

import runTime.Animal;
import runTime.Bird;

import java.lang.reflect.Constructor;

/**
 * Created by mingzhangyong on  2020/6/5 0005 10:05
 *
 * 获取对象的class测试 ； 验证获取的class是运行时类型
 */
public class GetClassTest {
    public static void main(String args[]){
        Animal animal = new Bird("red bird");
        Class<?> clazz = animal.getClass();
        System.out.println(" clazz ===  " + clazz);   // clazz ===  class runTime.Bird

        System.out.println(" clazz.getSuperclass() ===  " + clazz.getSuperclass()); // clazz.getSuperclass() ===  class runTime.Animal

        System.out.println(" clazz.getName() ===  " + clazz.getName()); //  clazz.getName() ===  runTime.Bird

        try {
            Object cloneAnimal = clazz.newInstance();
            Animal clone = (Animal)cloneAnimal;
            System.out.println(" 反射得到的 对象 编译时类型 ==== " +  clone.myName);
            System.out.println(" 反射得到的 对象 运行时类型 ==== " +  clone.getMyName());

            /**
             结论反射得到的对象跟原对象编译时类型一致，运行时类型一致
             反射得到的 对象 编译时类型 ====  my name is animal
             反射得到的 对象 运行时类型 ====  my name is bird
            * */
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        Constructor<?> cons[] = clazz.getConstructors();
        for(Constructor<?> constructor:cons){
            System.out.println(" 反射获取构造方法：  " + constructor);
        }
    }
}
