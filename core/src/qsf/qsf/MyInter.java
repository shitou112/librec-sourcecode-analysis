package qsf;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Qian Shaofeng
 *         created on 2017/9/27.
 */
public class MyInter {
    static class Point {
      int x;
      int y;
      Point() { x = 0; y = 0; }
      Point(int a, int b) { x = a; y = b; }
     }
    public int maxPoints(Point[] points) {

        int dx, dy, d, num=0;
        for (int i=0; i < points.length; ++i){
            Map<Map<Integer, Integer>, Integer> m = new HashMap();
            int dup = 1;
            for (int j=i+1; j < points.length; ++j){
                if (points[j].x==points[i].x && points[j].y==points[i].y){
                    ++dup;
                    continue;
                }
                dx = points[j].x - points[i].x;
                dy = points[j].y - points[i].y;
                d = gcd(dx, dy);
                HashMap<Integer, Integer> t = new HashMap<>();
                t.put(dx / d, dy / d);
                m.put(t, m.getOrDefault(t, 0)+1);
            }

            num = num < dup? dup: num;
            for (Map.Entry<Map<Integer, Integer>, Integer> e: m.entrySet()){
               num = Math.max(num, e.getValue()+dup);
            }
        }
        return num;
    }

    public int gcd(int a, int b){
        return (b==0)? a: gcd(b, a%b);
    }

    public static void main(String[] args) {
        Point p1 = new Point(1,1);
        Point p2 = new Point(2,1);
        Point p3 = new Point(2,3);
        Point p4 = new Point(4,4);

        Point[] p = new Point[4];
        p[0] = p1;
        p[1] = p2;
        p[2] = p3;
        p[3] = p4;
        System.out.println(new MyInter().maxPoints(p));
        System.out.println(0%3);
    }
}
