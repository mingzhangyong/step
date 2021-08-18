package algorithm;

/**
 74. 搜索二维矩阵
 编写一个高效的算法来判断 m x n 矩阵中，是否存在一个目标值。该矩阵具有如下特性：

 每行中的整数从左到右按升序排列。
 每行的第一个整数大于前一行的最后一个整数。


 示例 1：


 输入：matrix = [[1,3,5,7],[10,11,16,20],[23,30,34,60]], target = 3
 输出：true
 示例 2：


 输入：matrix = [[1,3,5,7],[10,11,16,20],[23,30,34,60]], target = 13
 输出：false


 提示：

 m == matrix.length
 n == matrix[i].length
 1 <= m, n <= 100
 -104 <= matrix[i][j], target <= 104
 **/
public class SearchMatrix {
    public static void main(String[] args){
//        int[][] matrix = {{1,3,5,7},{10,11,16,20},{23,30,34,60}};
        int[][] matrix = {{1}};
        System.out.println(searchMatrix(matrix,1));
    }
    public static boolean searchMatrix(int[][] matrix,int target){
        int length = matrix.length;
        if(length <= 0 ){
            return false;
        }
        int clength = matrix[0].length;
        if(matrix[0][0] > target){
            return false;
        }
        if(matrix[length-1][clength-1] < target){
            return false;
        }
        int i = 0;
        int j = length - 1;
        while (i<=j){
            int[] m = matrix[i];
            int[] n = matrix[j];
            if(m[clength-1] >= target){
                //在这个数组中二分查找target
                // 停止遍历
                i = j ;
                return binarySearch(m,target);
            }else{
                i ++;
            }
            if(n[0] <= target){
                return binarySearch(n,target);
            }else{
                j --;
            }
        }
        return false;
    }

    public static boolean binarySearch(int[] m , int target){
        int l = 0;
        int r = m.length - 1;
        while (l<=r){
            int midle = (r-l)/2 + l;
            if(m[midle] > target){
                r = midle - 1;
            }else if(m[midle]< target){
                l = midle + 1;
            }else{
                return true;
            }
        }
        return false;
    }
}
