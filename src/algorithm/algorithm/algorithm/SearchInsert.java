package algorithm;

/**
 * @author: mingzhangyong
 * @create: 2021-09-23 10:54
 * <p>
 * 搜索插入位置
 * 给定一个排序数组和一个目标值，在数组中找到目标值，并返回其索引。如果目标值不存在于数组中，返回它将会被按顺序插入的位置。
 * <p>
 * 请必须使用时间复杂度为 O(log n) 的算法。
 **/
public class SearchInsert {
    public int searchInsert(int[] nums, int target) {
        int index = nums.length;
        int i = 0;
        int j = nums.length - 1;
        if (nums.length == 1) {
            if (nums[0] >= target) {
                index = 0;
            } else if (nums[0] < target) {
                index = 1;
            }
        }
        while (i < j) {
            if (target == nums[i]) {
                index = i;
                break;
            }
            if (target == nums[j]) {
                index = j;
                break;
            }
            if (i == j - 1) {
                if (target > nums[j]) {
                    index = j + 1;
                } else if (target < nums[i]) {
                    index = i;
                } else {
                    index = i + 1;
                }
                break;
            }
            int m = (j - i) / 2 + i;
            System.out.println(m + "," + i + "," + j);
            if (nums[m] > target) {
                j = m;
            } else if (nums[m] < target) {
                i = m;
            } else {
                index = m;
                break;
            }
        }
        return index;
    }
}
