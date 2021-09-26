package algorithm;

/**
 * @author: mingzhangyong
 * @create: 2021-09-24 11:13
 *
 * 搜索旋转排序数组
 * 整数数组 nums 按升序排列，数组中的值 互不相同 。
 *
 * 在传递给函数之前，nums 在预先未知的某个下标 k（0 <= k < nums.length）上进行了 旋转，使数组变为 [nums[k], nums[k+1], ..., nums[n-1], nums[0], nums[1], ..., nums[k-1]]（下标 从 0 开始 计数）。例如， [0,1,2,4,5,6,7] 在下标 3 处经旋转后可能变为 [4,5,6,7,0,1,2] 。
 *
 * 给你 旋转后 的数组 nums 和一个整数 target ，如果 nums 中存在这个目标值 target ，则返回它的下标，否则返回 -1 。
 * 示例 1：
 *
 * 输入：nums = [4,5,6,7,0,1,2], target = 0
 * 输出：4
 * 示例 2：
 *
 * 输入：nums = [4,5,6,7,0,1,2], target = 3
 * 输出：-1
 * 示例 3：
 *
 * 输入：nums = [1], target = 0
 * 输出：-1
 **/
public class SearchMatrixTarget {
    public int searchMatrixTarget(int[] nums,int target){
        //有序用二分
        int l = 0;
        int r = nums.length - 1;
        int index = -1;
        if(nums.length == 1){
            if(nums[0] == target){
                return 0;
            }else{
                return -1;
            }
        }
        while (l < r){
            int m = (r - l) / 2 + l ;
            if(nums[m] == target){
                index = m;
                break;
            }
            if(nums[l] == target){
                index = l;
                break;
            }
            if(nums[r] == target){
                index = r;
                break;
            }
            if(m == l || m== r){
                break;
            }
            if(nums[m] > nums[l]){
                //左边是升序
                if(target > nums[l] && target < nums[m]){
                    r = m;
                }else{
                    l = m;
                }
            }else{
                //右边是升序
                if(target > nums[m] && target < nums[r]){
                    l = m;
                }else{
                    r = m;
                }
            }
        }
        return index;
    }
}
