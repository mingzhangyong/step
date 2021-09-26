package algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: mingzhangyong
 * @create: 2021-09-24 18:32
 * 编写一个程序将数组扁平化去并除其中重复部分数据，最终得到一个升序且不重复的数组
 **/
public class DeleteRepeatElement {
    public int[] deleteRepeatElement(int[] repeat){
        List<Integer> repeatList = new ArrayList<Integer>();
        for (int r : repeat) {
            repeatList.add(r);
        }
        for (int i = repeatList.size() - 1; i >= 0; i--) {
            System.out.println("-----------------------"+i+"-----------------------");
            for (int j = 0; j <= i; j++) {
                System.out.println(j + "------- " +i+","+ repeatList.get(j+1) + " -- " + repeatList.size());
                if(repeatList.get(j) > repeatList.get(j+1)){
                    int jj = repeatList.get(j);
                    repeatList.set(j,repeatList.get(j + 1)) ;
                    repeatList.set(j+1,jj);
                }else if(repeatList.get(j) == repeatList.get(j+1)){
                    repeatList.remove(j+1);
                    i -- ;
                    j ++;
                }
            }
        }
        int[] result = new int[repeatList.size()];
        for (int i = 0; i < repeatList.size(); i++) {
            result[i] = repeatList.get(i);
        }
        return result;
    }
}
