package runTime;

/**
 * Created by mingzhangyong on  2020/6/5 0005 9:46
 */
public class Bird extends Animal {
    public String myName;
    public Bird(){
        this.myName = " my name is bird ";
    }
    public Bird(String myName) {
        this.myName = myName;
    }

    public String getMyName(){
        return myName;
    }
}
