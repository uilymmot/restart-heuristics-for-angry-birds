package ab.demo.Solvability;

/**
 * Created by uilym on 1/03/2019.
 */
public class GraphGen {

    // Fills out all levels of the support graph, ie if a supports b and b supports c then a supports c with value 2
    public static int[][] fillStabilityGraphOut(int[][] graph) {
        for (int y = 0; y < graph[0].length; y++) {
            for (int x = 0; x < graph[0].length; x++)
                if (graph[x][y] == 1)
                    for (int z = 0; z < graph[0].length; z++)
                        if (graph[y][z] != 0 && graph[x][z] == 0)
                            graph[x][z] = graph[y][z] + 1;
        }
        return graph;
    }


}
