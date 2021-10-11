package algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: mingzhangyong
 * @create: 2021-09-29 10:34
 * 三数之和
 * 给你一个包含 n 个整数的数组 nums，判断 nums 中是否存在三个元素 a，b，c ，使得 a + b + c = 0 ？请你找出所有和为 0 且不重复的三元组。
 * <p>
 * 注意：答案中不可以包含重复的三元组。
 **/
public class ThreeSum {
    public List<List<Integer>> threeSum(int[] nums) {
        if(nums.length == 0){
            return new ArrayList<List<Integer>>();
        }
//        排序
        for (int i = nums.length -1 ; i >= 0; i--) {
            for (int j = 0; j < i; j++) {
                if(nums[j] > nums[j+1]){
                    int jj = nums[j];
                    nums[j] = nums[j+1];
                    nums[j+1] = jj;
                }
            }
        }
        for (int i = 0; i < nums.length; i++) {
            System.out.println("排序后数组：" + i + " -- " + nums[i]);
        }

        List<List<Integer>> result = new ArrayList<List<Integer>>();
        int jj = nums[nums.length - 1];
        for (int j = nums.length - 1; j >= 0; j--) {
            if(j < nums.length - 1 && jj == nums[j]){
                continue;
            }
            jj = nums[j];
            int ii = nums[0];
            for (int i = 0; i < j; i++) {
                int a = nums[i] + nums[j];
                if(i > 0 && ii == nums[i]){
                    continue;
                }
                ii = nums[i];
                Integer kk = null;
                for (int k = i + 1; k < j; k++) {
                    if (a + nums[k] == 0) {
                        if(null != kk && kk == nums[k]){
                            continue;
                        }
                        kk = nums[k];
                        List<Integer> item = new ArrayList<Integer>();
                        item.add(nums[i]);
                        item.add(nums[j]);
                        item.add(nums[k]);
                        result.add(item);
                    }
                }
            }
        }

        return result;
    }
}
