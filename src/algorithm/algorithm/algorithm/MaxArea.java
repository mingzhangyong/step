package algorithm;

/**
 * @author: mingzhangyong
 * @create: 2021-10-11 10:51
 *
 *  盛最多水的容器
 * 给你 n 个非负整数 a1，a2，...，an，每个数代表坐标中的一个点 (i, ai) 。在坐标内画 n 条垂直线，垂直线 i 的两个端点分别为 (i, ai) 和 (i, 0) 。找出其中的两条线，使得它们与 x 轴共同构成的容器可以容纳最多的水
 **/
public class MaxArea {
    /**
     * 遍历了两遍 ， 时间复杂度高
     * @param height
     * @return
     */
    public int maxArea(int[] height) {
        int max = 0;
        for (int i = 0; i < height.length - 1; i++) {
            for (int j = i + 1; j < height.length; j++) {
                int min = Math.min(height[i], height[j]);
                int area = min * (j - i);
                max = Math.max(max,area);
            }
        }
        return max;
    }
    //双指针， 不是用两次for循环， 而是在一次循环中用两个指针
    public int maxArea2(int[] height) {
        int max = 0;
        int l = 0;
        int r = height.length - 1;
        while (l < r){
            max = Math.max(Math.min(height[l],height[r]) * (r - l),max);

            if(height[l] < height[r]){
                l ++;
            }else{
                r --;
            }
        }
        return max;
    }
}
