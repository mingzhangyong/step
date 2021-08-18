package algorithm;

/**
 给定一个按照升序排列的整数数组 nums，和一个目标值 target。找出给定目标值在数组中的开始位置和结束位置。

 如果数组中不存在目标值 target，返回 [-1, -1]。

 进阶：

 你可以设计并实现时间复杂度为 O(log n) 的算法解决此问题吗？
  

 示例 1：

 输入：nums = [5,7,7,8,8,10], target = 8
 输出：[3,4]
 示例 2：

 输入：nums = [5,7,7,8,8,10], target = 6
 输出：[-1,-1]
 示例 3：

 输入：nums = [], target = 0
 输出：[-1,-1]
  

 提示：

 0 <= nums.length <= 105
 -109 <= nums[i] <= 109
 nums 是一个非递减数组
 -109 <= target <= 109

 来源：力扣（LeetCode）
 链接：https://leetcode-cn.com/problems/find-first-and-last-position-of-element-in-sorted-array
 著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。
 */
public class SearchRange {
    public static void main(String args[]){
        int[] nums = new int[6] ;
        nums[0] = 5;
        nums[1] = 7;
        nums[2] = 7;
        nums[3] = 8;
        nums[4] = 8;
        nums[5] = 10;
        System.out.println(searchRange(nums,8)[0]) ;
    }
    public static int[] searchRange(int[] nums, int target) {
        int length = nums.length;
        int s = -1;
        int e = -1;
        for (int i = 0; i < length; i++) {
            if(nums[i] == target){
                s = i;
                break;
            }
        }
        for (int i = length-1; i >= 0; i--) {
            if(nums[i] == target){
                e = i;
                break;
            }
        }
        int[] r = new int[2];
        r[0] = s;
        r[1] = e;
        return r;
    }
}
