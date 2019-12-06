package ab.demo.Solvability;

/**
 * Created by uilym on 1/03/2019.
 */
public class Force {
    // 0: U
    // 1: R
    // 2: D
    // 3: L
    // Direction is a vector with each entry corresponding to the directions above
    double[] direction;
    int distance;
    Force(double[] d) {
        direction = d;
    }
    public void Mult(double f) {
        for (int i = 0; i < 4; i++)
            direction[i] *= f;
    }
    public void Mult(double f, int p) {
        direction[p] *= f;
    }
    public void ForceSum(Force other) {
        for (int i = 0; i < 4; i++) {
            direction[i] += other.direction[i];
        }
    }
    public Force NewForceSum(Force other) {
        Force f = new Force(new double[]{0,0,0,0});
        f.ForceSum(this);
        f.ForceSum(other);
        return f;
    }
    @Override
    public String toString() {
        String ret = "";
        for (int i = 0; i < 4; i++)
            ret = ret + direction[i] + ",";
        return ret;
    }
}