package byow.Core;

import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.io.IOException;

/* A mutable and finite Graph object. Edge labels are stored via a HashMap
   where labels are mapped to a key calculated by the following. The graph is
   undirected (whenever an Edge is added, the dual Edge is also added). Vertices
   are numbered starting from 0. */
public class Graph {

    /* Maps vertices to a list of its neighboring vertices. */
    private HashMap<Integer, Set<Integer>> neighbors = new HashMap<>();
    /* Maps vertices to a list of its connected edges. */
    private HashMap<Integer, Set<Edge>> edges = new HashMap<>();
    /* A sorted set of all edges. */
    private TreeSet<Edge> allEdges = new TreeSet<>();

    /* Returns the vertices that neighbor V. */
    public TreeSet<Integer> getNeighbors(int v) {
        return new TreeSet<Integer>(neighbors.get(v));
    }

    /* Returns all edges adjacent to V. */
    public TreeSet<Edge> getEdges(int v) {
        return new TreeSet<Edge>(edges.get(v));
    }

    /* Returns a sorted list of all vertices. */
    public TreeSet<Integer> getAllVertices() {
        return new TreeSet<Integer>(neighbors.keySet());
    }

    /* Returns a sorted list of all edges. */
    public TreeSet<Edge> getAllEdges() {
        return new TreeSet<Edge>(allEdges);
    }

    /* Adds vertex V to the graph. */
    public void addVertex(Integer v) {
        if (neighbors.get(v) == null) {
            neighbors.put(v, new HashSet<Integer>());
            edges.put(v, new HashSet<Edge>());
        }
    }

    /* Adds Edge E to the graph. */
    public void addEdge(Edge e) {
        addEdgeHelper(e.getSource(), e.getDest(), e.getWeight());
    }

    /* Creates an Edge between V1 and V2 with no weight. */
    public void addEdge(int v1, int v2) {
        addEdgeHelper(v1, v2, 0);
    }

    /* Creates an Edge between V1 and V2 with weight WEIGHT. */
    public void addEdge(int v1, int v2, int weight) {
        addEdgeHelper(v1, v2, weight);
    }

    /* Returns true if V1 and V2 are connected by an edge. */
    public boolean isNeighbor(int v1, int v2) {
        return neighbors.get(v1).contains(v2) && neighbors.get(v2).contains(v1);
    }

    /* Returns true if the graph contains V as a vertex. */
    public boolean containsVertex(int v) {
        return neighbors.get(v) != null;
    }

    /* Returns true if the graph contains the edge E. */
    public boolean containsEdge(Edge e) {
        return allEdges.contains(e);
    }

    /* Returns if this graph spans G. */
    public boolean spans(Graph g) {
        TreeSet<Integer> all = getAllVertices();
        if (all.size() != g.getAllVertices().size()) {
            return false;
        }
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> vertices = new ArrayDeque<>();
        Integer curr;

        vertices.add(all.first());
        while ((curr = vertices.poll()) != null) {
            if (!visited.contains(curr)) {
                visited.add(curr);
                for (int n : getNeighbors(curr)) {
                    vertices.add(n);
                }
            }
        }
        return visited.size() == g.getAllVertices().size();
    }

    /* Overrides objects equals method. */
    public boolean equals(Object o) {
        if (!(o instanceof Graph)) {
            return false;
        }
        Graph other = (Graph) o;
        return neighbors.equals(other.neighbors) && edges.equals(other.edges);
    }

    /* A helper function that adds a new edge from V1 to V2 with WEIGHT as the
       label. */
    private void addEdgeHelper(int v1, int v2, int weight) {
        addVertex(v1);
        addVertex(v2);

        neighbors.get(v1).add(v2);
        neighbors.get(v2).add(v1);

        Edge e1 = new Edge(v1, v2, weight);
        Edge e2 = new Edge(v2, v1, weight);
        edges.get(v1).add(e1);
        edges.get(v2).add(e2);
        allEdges.add(e1);
    }

    private class Pair {
        int vertex;
        int distance;

        Pair(int vertex, int distance) {
            this.vertex = vertex;
            this.distance = distance;
        }
    }

    class PairComparator implements Comparator<Pair> {
        public int compare(Pair p1, Pair p2) {
            return p1.distance - p2.distance;
        }
    }

    public Graph prims(int start) {
        PriorityQueue<Pair> fringe = new PriorityQueue<Pair>(new PairComparator());
        Set<Integer> visited = new HashSet<>();
        HashMap<Integer, Integer> distTo = new HashMap<>();
        HashMap<Integer, Integer> edgeTo = new HashMap<>();

        for (Edge e : getEdges(start)) {
            fringe.add(new Pair(e.getDest(), e.getWeight()));
        }

        while (!fringe.isEmpty()) {
            Pair curr = fringe.poll();
            visited.add(curr.vertex);
            for (Edge edge : getEdges(curr.vertex)) {
                if (visited.contains(edge.getDest())) {
                    continue;
                }

                if (!distTo.containsKey(edge.getDest())
                        || distTo.get(edge.getDest()) > edge.getWeight()) {
                    distTo.put(edge.getDest(), edge.getWeight());
                    edgeTo.put(edge.getDest(), edge.getSource());
                    fringe.add(new Pair(edge.getDest(), distTo.get(edge.getDest())));
                }
            }
        }

        List<Edge> mst = new ArrayList<Edge>();
        for (Integer sour : edgeTo.keySet()) {
            mst.add(new Edge(sour, edgeTo.get(sour), distTo.get(sour)));
        }

        Graph g = new Graph();
        for (int i : getAllVertices()) {
            g.addVertex(i);
        }
        for (Edge e : mst) {
            g.addEdge(e);
        }

        return g;
    }

//    public Graph prims(int start) {
//        // number of vertices
//        int n = getAllVertices().size();
//        // the lowest cost from v[i] to v_new
//        int[] lowestCost = new int[n];
//        Arrays.fill(lowestCost, Integer.MAX_VALUE);
//        // whether marked[i] is in current MST v_new, 0 no, 1 yes
//        int[] marked = new int[n];
//
//        // edgeTo[v2] = v1 for edge v1->v2
//        int[] edgeTo = new int[n];
//
//        Graph res = new Graph();
//
//        PriorityQueue<Tuple> pq = new PriorityQueue<>((t1, t2) -> t1.dist - t2.dist == 0
//                ? t1.v - t2.v : t1.dist - t2.dist);
//        pq.offer(new Tuple(0, start));
//        while (!pq.isEmpty()) {
//            Tuple curr = pq.poll();
//
//            if (marked[curr.v] == 1) {
//                continue;
//            }
//            marked[curr.v] = 1;
//            if (curr.v != start) {
//                res.addEdgeHelper(curr.v, edgeTo[curr.v], curr.dist);
//            }
//
//            for (Edge e: getEdges(curr.v)) {
//                int j = e.getDest();
//                int weight = e.getWeight();
//                if (marked[j] == 0 && weight < lowestCost[j]) {
//                    lowestCost[j] = weight;
//                    edgeTo[j] = curr.v;
//                    pq.offer(new Tuple(weight, j));
//                }
//            }
//        }
//        return res;
//    }
//
//    private class Tuple {
//        int dist;
//        int v;
//
//        Tuple(int dist, int v) {
//            this.v = v;
//            this.dist = dist;
//        }
//    }

    public Graph kruskals() {
        List<Edge> mst = new ArrayList<Edge>();

        PriorityQueue<Edge> pq = new PriorityQueue<>();
        for (Edge e : allEdges) {
            pq.add(e);
        }

        UnionFind uf = new UnionFind(getAllVertices().size());
        while (!pq.isEmpty() && mst.size() < getAllVertices().size() - 1) {
            Edge e = pq.poll();
            int v = e.getSource();
            int w = e.getDest();
            if (!uf.connected(v, w)) {
                uf.union(v, w);
                mst.add(e);
            }
        }

        Graph g = new Graph();
        for (int i : getAllVertices()) {
            g.addVertex(i);
        }
        for (Edge e : mst) {
            g.addEdge(e);
        }

        return g;
    }

    /* Returns a randomly generated graph with VERTICES number of vertices and
       EDGES number of edges with max weight WEIGHT. */
    public static Graph randomGraph(int vertices, int edges, int weight) {
        Graph g = new Graph();
        Random rng = new Random();
        for (int i = 0; i < vertices; i += 1) {
            g.addVertex(i);
        }
        for (int i = 0; i < edges; i += 1) {
            Edge e = new Edge(rng.nextInt(vertices), rng.nextInt(vertices), rng.nextInt(weight));
            g.addEdge(e);
        }
        return g;
    }

    /* Returns a Graph object with integer edge weights as parsed from
       FILENAME. Talk about the setup of this file. */
    public static Graph loadFromText(String filename) {
        Charset cs = Charset.forName("US-ASCII");
        try (BufferedReader r = Files.newBufferedReader(Paths.get(filename), cs)) {
            Graph g = new Graph();
            String line;
            while ((line = r.readLine()) != null) {
                String[] fields = line.split(", ");
                if (fields.length == 3) {
                    int from = Integer.parseInt(fields[0]);
                    int to = Integer.parseInt(fields[1]);
                    int weight = Integer.parseInt(fields[2]);
                    g.addEdge(from, to, weight);
                } else if (fields.length == 1) {
                    g.addVertex(Integer.parseInt(fields[0]));
                } else {
                    throw new IllegalArgumentException("Bad input file!");
                }
            }
            return g;
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
            System.exit(1);
            return null;
        }
    }
}
