package algorithm;

import utils.GsonUtils;

/**
 * @author: mingzhangyong
 * @create: 2021-08-19 11:25
 **/
public class MainClass {
    public static void main(String[] args){
//        SearchMin searchMin = new SearchMin();
//        int[] a = new int[]{4,5,6,7,0,1,2};
//        System.out.println(searchMin.findMin(a)) ;

//        DeleteDuplicates.ListNode test1 = new DeleteDuplicates.ListNode(1);
//        DeleteDuplicates.ListNode test2 = new DeleteDuplicates.ListNode(1);
//        DeleteDuplicates.ListNode test3 = new DeleteDuplicates.ListNode(3);
//        DeleteDuplicates.ListNode test4 = new DeleteDuplicates.ListNode(3);
//        DeleteDuplicates.ListNode test5 = new DeleteDuplicates.ListNode(4);
//        DeleteDuplicates.ListNode test6= new DeleteDuplicates.ListNode(4);
//        DeleteDuplicates.ListNode test7 = new DeleteDuplicates.ListNode(5);
//        test1.next = test2;
//        test2.next = test3;
//        test3.next = test4;
//        test4.next = test5;
//        test5.next = test6;
//        test6.next = test7;
//        DeleteDuplicates.ListNode result =  DeleteDuplicates.deleteDuplicates(test1);
//        System.out.println(GsonUtils.toJsonString(result));


        SearchInsert searchInsert = new SearchInsert();
        int[] a = new int[]{0,1,2,4,5,6,7};
        System.out.println(searchInsert.searchInsert(a,9));
    }
}
