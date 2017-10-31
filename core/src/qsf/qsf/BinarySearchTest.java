package qsf;

import java.util.*;

/**
 * @author Qian Shaofeng
 *         created on 2017/10/30.
 */
public class BinarySearchTest {
    public static void main(String[] args) {
        int[] arr = {1,2,4,5,6};
        int i = Arrays.binarySearch(arr, 0, 5, 3);
        System.out.println(i);

        PriorityQueue<Integer> queue = new PriorityQueue<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 > o2? -1 : o1 == o2? 0 : 1;
            }
        });
        queue.add(3);
        queue.add(2);
        queue.add(5);
        queue.add(1);
        System.out.println(queue);

        List<List<Integer>> arraylist = new ArrayList<>();
        List<Integer> a = new ArrayList<>();
        a.add(1);
        arraylist.set(0, a);
        System.out.println(arraylist);
    }
}
