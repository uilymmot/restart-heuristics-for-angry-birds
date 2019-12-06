package ab.demo.Solvability;

/**
 * Created by uilym on 1/03/2019.
 */
public class GroundItem {
    int s;
    int e;
    double a;
    GroundItem(int start, int end, double angle) {
        s = start;
        e = end;
        a = angle;
    }
    public boolean in(int xPos) {
        return (xPos >= s && xPos <= e);
    }
    public boolean rightVicinity(int xPos, int thresHold) {
        return (xPos >= (s-thresHold) && xPos <= e);
    }
    @Override
    public String toString() {
        return "| " + s + "," + e + "," + a + "|";
    }
}