package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.awt.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class World implements Serializable {
    
    public class Pos implements Serializable {
        int X;
        int Y;
        
        Pos(int x, int y) {
            this.X = x;
            this.Y = y;
        }

        public List<Pos> neighbors() {
            List<Pos> result = new ArrayList<>();
            if (this.X - 1 >= 0) {
                result.add(new Pos(this.X - 1, this.Y));
            }

            if (this.X + 1 < world.length) {
                result.add(new Pos(this.X + 1, this.Y));
            }

            if (this.Y - 1 >= 0) {
                result.add(new Pos(this.X, this.Y - 1));
            }

            if (this.Y + 1 < world[0].length) {
                result.add(new Pos(this.X, this.Y + 1));
            }

            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Pos that = (Pos) o;
            return this.X == that.X && this.Y == that.Y;
        }

        @Override
        public String toString() {
            return "(" + this.X + ", " + this.Y + ")";
        }

        @Override
        public int hashCode() {
            return this.X * 81 + this.Y * 31;
        }

    }

    private Random random;

    private TETile[][] world;
    
    private int MAX_CHAMBER_NUM;
    private int chamberCount;
    private Chamber[] chambers;

    private Pos avatarPos;
    private Pos monsterPos;
    private TETile currentTile;

    private class Chamber implements Serializable {
        private int x;
        private int y;
        private int height;
        private int width;
        
        /*
        set some ceiling of position (x, y) and width and height
        */
        Chamber() {
            x = RandomUtils.uniform(random, 2, world.length - 2);
            y = RandomUtils.uniform(random, 2, world[0].length - 2);
            width = RandomUtils.uniform(random, 2,
                    3 + Math.min(world.length / 2, 2 * Math.min(x, world.length - x)));
            height = RandomUtils.uniform(random, 2,
                    3 + Math.min(world[0].length / 2, 2 * Math.min(y, world[0].length - y)));
        }

        public int getX() {
            return x;
        }
        
        public int getY() {
            return y;
        }
        
        public int getHeight() {
            return height;
        }
        
        public int getWidth() {
            return width;
        }
    }

    World(int width, int height, long seed) {
        this.random = new Random(seed);
        this.MAX_CHAMBER_NUM = RandomUtils.uniform(random, 5, 15);
        this.chambers = new Chamber[MAX_CHAMBER_NUM];
        this.chamberCount = 0;
        this.world = new TETile[width][height];

        generateWorld();
    }

    World(Random random, TETile[][] world, int chamberCount, int maxChamberNum,
          Chamber[] chambers, Pos avatarPos, Pos monsterPos, TETile currentTile) {
        this.random = random;
        this.world = world;
        this.chamberCount = chamberCount;
        this.MAX_CHAMBER_NUM = maxChamberNum;
        this.chambers = chambers;
        this.avatarPos = avatarPos;
        this.monsterPos = monsterPos;
        this.currentTile = currentTile;
    }

    public void generateWorld() {
        generateChambers();

        fillChambersIntoWorld();

        buildHallway();

        createAvatar();
        createMonster();

        updateChasePath();

        createDoor();

        turnLight();

    }

    public void createAvatar() {
        int up = chambers[0].getY() + (chambers[0].getHeight() + 1) / 2;
        int bottom = chambers[0].getY() - chambers[0].getHeight() / 2;
        int left = chambers[0].getX() - chambers[0].getWidth() / 2;
        int right = chambers[0].getX() + (chambers[0].getWidth() + 1) / 2;

        int avatarX = RandomUtils.uniform(random, left + 1, right);
        int avatarY = RandomUtils.uniform(random, bottom + 1, up);
        this.avatarPos = new Pos(avatarX, avatarY);

        while (!world[this.avatarPos.X][this.avatarPos.Y].equals(Tileset.FLOOR)) {
            this.avatarPos.X = RandomUtils.uniform(random, left + 1, right);
            this.avatarPos.Y = RandomUtils.uniform(random, bottom + 1, up);
        }
        this.currentTile = world[this.avatarPos.X][this.avatarPos.Y];
        world[this.avatarPos.X][this.avatarPos.Y] = Tileset.AVATAR;
    }
    
    public void createMonster() {
        Chamber monsterChamber = chambers[chambers.length - 1];
        int up = monsterChamber.getY() + (monsterChamber.getHeight() + 1) / 2;
        int bottom = monsterChamber.getY() - monsterChamber.getHeight() / 2;
        int left = monsterChamber.getX() - monsterChamber.getWidth() / 2;
        int right = monsterChamber.getX() + (monsterChamber.getWidth() + 1) / 2;

        int monsterX = RandomUtils.uniform(random, left + 1, right);
        int monsterY = RandomUtils.uniform(random, bottom + 1, up);
        this.monsterPos = new Pos(monsterX, monsterY);

        while (!world[this.monsterPos.X][this.monsterPos.Y].equals(Tileset.FLOOR)) {
            this.monsterPos.X = RandomUtils.uniform(random, left + 1, right);
            this.monsterPos.Y = RandomUtils.uniform(random, bottom + 1, up);
        }
        this.currentTile = world[this.monsterPos.X][this.monsterPos.Y];
        world[this.monsterPos.X][this.monsterPos.Y] = Tileset.MOUNTAIN;
    }

    public List<Pos> updateChasePath() {
        List<Pos> path = shortestPath(avatarPos, monsterPos);
        for (Pos pos : path) {
            if (pos.equals(avatarPos) || pos.equals(monsterPos)) {
                continue;
            }
            world[pos.X][pos.Y] = new TETile(Tileset.TREE.character(),
                    Tileset.TREE.textColor(),
                    world[pos.X][pos.Y].backgroundColor(),
                    Tileset.TREE.description());
        }

        return path;
    }

    public void recoverChasePath(List<Pos> path) {
        for (Pos pos : path) {
            if (pos.equals(avatarPos) || pos.equals(monsterPos)) {
                continue;
            }
            world[pos.X][pos.Y] = new TETile(Tileset.FLOOR.character(),
                    Tileset.FLOOR.textColor(),
                    world[pos.X][pos.Y].backgroundColor(),
                    Tileset.FLOOR.description());
        }
    }

    public boolean monsterChase(List<Pos> path) {
        if (path.size() <= 1) {
            return false;
        }

        world[monsterPos.X][monsterPos.Y] = new TETile(Tileset.FLOOR.character(),
                Tileset.FLOOR.textColor(),
                world[monsterPos.X][monsterPos.Y].backgroundColor(),
                Tileset.FLOOR.description());
        monsterPos = path.get(path.size() - 2);
        world[monsterPos.X][monsterPos.Y] = new TETile(Tileset.MOUNTAIN.character(),
                Tileset.MOUNTAIN.textColor(),
                world[monsterPos.X][monsterPos.Y].backgroundColor(),
                Tileset.MOUNTAIN.description());
        return true;
    }

    private class Pair {
        Pos vertex;
        int distance;

        Pair(Pos vertex, int distance) {
            this.vertex = vertex;
            this.distance = distance;
        }
    }

    class PairComparator implements Comparator<Pair> {
        public int compare(Pair p1, Pair p2) {
            return p1.distance - p2.distance;
        }
    }

    /* Dijkstra’s algorithm. Returns the shortest path from START to STOP. If no path
       exists, returns an empty List. If START == STOP, returns a List with START. */
    public List<Pos> shortestPath(Pos start, Pos stop) {
        if (start.equals(stop)) {
            List<Pos> result = new ArrayList<>();
            result.add(start);
            return result;
        }

        PriorityQueue<Pair> fringe = new PriorityQueue<Pair>(new PairComparator());
        HashSet<Pos> visited = new HashSet<>();
        HashMap<Pos, Integer> distTo = new HashMap<>();
        HashMap<Pos, Pos> edgeTo = new HashMap<>();

        fringe.add(new Pair(start, 0));
        distTo.put(start, 0);

        while (!fringe.isEmpty() && !visited.contains(stop)) {
            Pair curr = fringe.poll();
            visited.add(curr.vertex);
            for (Pos neighbor : curr.vertex.neighbors()) {
                if (world[neighbor.X][neighbor.Y].character() == Tileset.WALL.character()) {
                    continue;
                }

                if (visited.contains(neighbor)) {
                    continue;
                }

                if (!distTo.containsKey(neighbor) || distTo.get(neighbor) > curr.distance + 1) {
                    distTo.put(neighbor, curr.distance + 1);
                    edgeTo.put(neighbor, curr.vertex);
                    fringe.add(new Pair(neighbor, curr.distance + 1));
                }
            }
        }


        Pos postVertice = stop;
        List<Pos> result = new ArrayList<>();
        result.add(postVertice);
        while (!postVertice.equals(start)) {
            postVertice = edgeTo.get(postVertice);
            result.add(postVertice);
        }
        Collections.reverse(result);
        return result;
    }

    public void createDoor() {
        int up = chambers[chamberCount - 1].getY()
                + (chambers[chamberCount - 1].getHeight() + 1) / 2;
        int bottom = chambers[chamberCount - 1].getY()
                - chambers[chamberCount - 1].getHeight() / 2;
        int left = chambers[chamberCount - 1].getX()
                - chambers[chamberCount - 1].getWidth() / 2;
        int right = chambers[chamberCount - 1].getX()
                + (chambers[chamberCount - 1].getWidth() + 1) / 2;
        if (RandomUtils.uniform(random) < 0.5) {
            if (RandomUtils.uniform(random) < 0.5) {
                int rand = RandomUtils.uniform(random, bottom + 1, up);
//                while (world[left][rand].character() != Tileset.WALL.character()) {
//                    rand = RandomUtils.uniform(random, bottom + 1, up);
//                }
                world[left][rand] = Tileset.LOCKED_DOOR;
            } else {
                int rand = RandomUtils.uniform(random, bottom + 1, up);
//                while (world[right][rand].character() != Tileset.WALL.character()) {
//                    rand = RandomUtils.uniform(random, bottom + 1, up);
//                }
                world[right][rand] = Tileset.LOCKED_DOOR;
            }
        } else {
            if (RandomUtils.uniform(random) < 0.5) {
                int rand = RandomUtils.uniform(random, left + 1, right);
//                while (world[rand][up].character() != Tileset.WALL.character()) {
//                    rand = RandomUtils.uniform(random, left + 1, right);
//                }
                world[rand][up] = Tileset.LOCKED_DOOR;
            } else {
                int rand = RandomUtils.uniform(random, left + 1, right);
//                while (world[rand][bottom].character() != Tileset.WALL.character()) {
//                    rand = RandomUtils.uniform(random, left + 1, right);
//                }
                world[rand][bottom] = Tileset.LOCKED_DOOR;
            }
        }
    }

    public void generateChambers() {
        while (chamberCount < MAX_CHAMBER_NUM) {
            Chamber newChamber = new Chamber();
            if (isChamberValid(newChamber) && !isOverlapWithPreviousChambers(newChamber)) {
                chambers[chamberCount] = newChamber;
                chamberCount += 1;
            }
        }
    }

    public void fillChambersIntoWorld() {
        for (int x = 0; x < world.length; x++) {
            for (int y = 0; y < world[0].length; y++) {
                world[x][y] = Tileset.NOTHING;
            }
        }

        for (Chamber c : chambers) {
            int up = c.getY() + (c.getHeight() + 1) / 2;
            int bottom = c.getY() - c.getHeight() / 2;
            int left = c.getX() - c.getWidth() / 2;
            int right = c.getX() + (c.getWidth() + 1) / 2;

            for (int x = left; x <= right; x++) {
                world[x][up] = Tileset.WALL;
                world[x][bottom] = Tileset.WALL;
            }

            for (int y = bottom; y <= up; y++) {
                world[left][y] = Tileset.WALL;
                world[right][y] = Tileset.WALL;
            }

            for (int x = left + 1; x < right; x++) {
                for (int y = bottom + 1; y < up; y++) {
                    world[x][y] = Tileset.FLOOR;
                }
            }
        }
    }


    public TETile[][] getWorld() {
        return this.world;
    }

    /**
    * within the width and height
    * not overlap with the independent chambers
    */
    public boolean isChamberValid(Chamber chamber) {
        return chamber.getX() + (chamber.getWidth() + 1) / 2 < world.length
                && chamber.getX() - chamber.getWidth() / 2 >= 0
                && chamber.getY() + (chamber.getHeight() + 1) / 2 < world[0].length
                && chamber.getY() - chamber.getHeight() / 2 >= 0;
    }
    
    public boolean isChamberOverlap(Chamber a, Chamber b) {
        return isXOverlap(a, b) && isYOverlap(a, b);
    }

    public boolean isOverlapWithPreviousChambers(Chamber chamber) {
        for (int i = 0; i < chamberCount; i++) {
            if (isChamberOverlap(chambers[i], chamber)) {
                return true;
            }
        }
        return false;
    }

    private boolean isXOverlap(Chamber a, Chamber b) {
        if (a.getX() <= b.getX()) {
            return a.getX() + (a.getWidth() + 1) / 2 >= b.getX() - b.getWidth() / 2 - 1;
        }

        return b.getX() + (b.getWidth() + 1) / 2 >= a.getX() - a.getWidth() / 2 - 1;
    }

    private boolean isYOverlap(Chamber a, Chamber b) {
        if (a.getY() <= b.getY()) {
            return a.getY() + (a.getHeight() + 1) / 2 >= b.getY() - b.getHeight() / 2 - 1;
        }

        return b.getY() + (b.getHeight() + 1) / 2 >= a.getY() - a.getHeight() / 2 - 1;
    }

    public Graph makeGraph() {
        Graph graph = new Graph();

        for (int i = 0; i < chambers.length; i++) {
            graph.addVertex(i);
        }

        for (int i = 0; i < chambers.length; i++) {
            for (int j = i + 1; j < chambers.length; j++) {
                graph.addEdge(i, j, distanceOf(chambers[i], chambers[j]));
            }
        }

        return graph;
    }

    private int distanceOf(Chamber c1, Chamber c2) {
        return Math.abs(c1.getX() - c2.getX()) + Math.abs(c1.getY() - c2.getY());
    }

    public void buildHallway() {
        Graph graph = makeGraph();

        TreeSet<Edge> edges = graph.kruskals().getAllEdges();
        for (Edge edge : edges) {
            fillHallwayIntoWorld(edge.getSource(), edge.getDest());
        }
    }

    public void fillHallwayIntoWorld(int i, int j) {
        int startX = chambers[i].getX();
        int endX = chambers[j].getX();
        int startY = chambers[i].getY();
        int endY = chambers[j].getY();

        if (startX <= endX && startY <= endY) {

            helperStartXLTEndX(startX, endX, startY, endY);

            helperStartYLTEndY(startX, endX, startY, endY);

        } else if (startX > endX && startY <= endY) {

            helperStartXGTEndX(startX, endX, startY, endY);

            helperStartYLTEndY(startX, endX, startY, endY);

        } else if (startX <= endX && startY > endY) {

            helperStartXLTEndX(startX, endX, startY, endY);

            helperStartYGTEndY(startX, endX, startY, endY);

        } else if (startX > endX && startY > endY) {

            helperStartXGTEndX(startX, endX, startY, endY);

            helperStartYGTEndY(startX, endX, startY, endY);

        }

    }

    private void helperStartXLTEndX(int startX, int endX, int startY, int endY) {
        for (int k = startX; k <= endX; k++) {
            if (isInsideFloor(k, startY)) {
                continue;
            } else {
                world[k][startY] = Tileset.FLOOR;
                if (!world[k][startY - 1].equals(Tileset.FLOOR)) {
                    world[k][startY - 1] = Tileset.WALL;
                }
                if (!world[k][startY + 1].equals(Tileset.FLOOR)) {
                    world[k][startY + 1] = Tileset.WALL;
                }
            }
        }
    }

    private void helperStartXGTEndX(int startX, int endX, int startY, int endY) {
        for (int k = startX; k >= endX; k--) {
            if (isInsideFloor(k, startY)) {
                continue;
            } else {
                world[k][startY] = Tileset.FLOOR;
                if (!world[k][startY - 1].equals(Tileset.FLOOR)) {
                    world[k][startY - 1] = Tileset.WALL;
                }
                if (!world[k][startY + 1].equals(Tileset.FLOOR)) {
                    world[k][startY + 1] = Tileset.WALL;
                }
            }
        }
    }

    private void helperStartYLTEndY(int startX, int endX, int startY, int endY) {
        for (int k = startY; k <= endY; k++) {
            if (isInsideFloor(endX, k)) {
                continue;
            } else {
                world[endX][k] = Tileset.FLOOR;
                if (!world[endX - 1][k].equals(Tileset.FLOOR)) {
                    world[endX - 1][k] = Tileset.WALL;
                }
                if (!world[endX + 1][k].equals(Tileset.FLOOR)) {
                    world[endX + 1][k] = Tileset.WALL;
                }
            }
        }
    }

    private void helperStartYGTEndY(int startX, int endX, int startY, int endY) {
        for (int k = startY; k >= endY; k--) {
            if (isInsideFloor(endX, k)) {
                continue;
            } else {
                world[endX][k] = Tileset.FLOOR;
                if (!world[endX - 1][k].equals(Tileset.FLOOR)) {
                    world[endX - 1][k] = Tileset.WALL;
                }
                if (!world[endX + 1][k].equals(Tileset.FLOOR)) {
                    world[endX + 1][k] = Tileset.WALL;
                }
            }
        }
    }

    private boolean isInsideFloor(int x, int y) {

        int countTrue = 0;

        if (world[x][y - 1].equals(Tileset.FLOOR)) {
            countTrue += 1;
        }

        if (world[x][y + 1].equals(Tileset.FLOOR)) {
            countTrue += 1;
        }

        if (world[x - 1][y].equals(Tileset.FLOOR)) {
            countTrue += 1;
        }

        if (world[x + 1][y].equals(Tileset.FLOOR)) {
            countTrue += 1;
        }

        return world[x][y].equals(Tileset.FLOOR) && countTrue >= 2;
    }

    public TETile getCurrentTile() {
        return currentTile;
    }

    public void moveUp() {
        if (world[this.avatarPos.X][this.avatarPos.Y + 1].character() == Tileset.WALL.character()) {
            return;
        }
        world[this.avatarPos.X][this.avatarPos.Y] = this.currentTile;

        this.avatarPos.Y += 1;
        this.currentTile = world[this.avatarPos.X][this.avatarPos.Y];
        world[this.avatarPos.X][this.avatarPos.Y] = new TETile(Tileset.AVATAR.character(),
                Tileset.AVATAR.textColor(),
                world[this.avatarPos.X][this.avatarPos.Y].backgroundColor(),
                Tileset.AVATAR.description());
    }

    public void moveDown() {
        if (world[this.avatarPos.X][this.avatarPos.Y - 1].character() == Tileset.WALL.character()) {
            return;
        }
        world[this.avatarPos.X][this.avatarPos.Y] = this.currentTile;

        this.avatarPos.Y -= 1;
        this.currentTile = world[this.avatarPos.X][this.avatarPos.Y];
        world[this.avatarPos.X][this.avatarPos.Y] = new TETile(Tileset.AVATAR.character(),
                Tileset.AVATAR.textColor(),
                world[this.avatarPos.X][this.avatarPos.Y].backgroundColor(),
                Tileset.AVATAR.description());
    }

    public void moveLeft() {
        if (world[this.avatarPos.X - 1][this.avatarPos.Y].character() == Tileset.WALL.character()) {
            return;
        }
        world[this.avatarPos.X][this.avatarPos.Y] = this.currentTile;

        this.avatarPos.X -= 1;
        this.currentTile = world[this.avatarPos.X][this.avatarPos.Y];
        world[this.avatarPos.X][this.avatarPos.Y] = new TETile(Tileset.AVATAR.character(),
                Tileset.AVATAR.textColor(),
                world[this.avatarPos.X][this.avatarPos.Y].backgroundColor(),
                Tileset.AVATAR.description());
    }

    public void moveRight() {
        if (world[this.avatarPos.X + 1][this.avatarPos.Y].character() == Tileset.WALL.character()) {
            return;
        }
        world[this.avatarPos.X][this.avatarPos.Y] = this.currentTile;

        this.avatarPos.X += 1;
        this.currentTile = world[this.avatarPos.X][this.avatarPos.Y];
        world[this.avatarPos.X][this.avatarPos.Y] = new TETile(Tileset.AVATAR.character(),
                Tileset.AVATAR.textColor(),
                world[this.avatarPos.X][this.avatarPos.Y].backgroundColor(),
                Tileset.AVATAR.description());
    }

    public void turnLight() {
        Chamber lightedChamber = chambers[chambers.length / 2];
        if (world[lightedChamber.getX()][lightedChamber.getY()].backgroundColor() != Color.BLACK) {
            turnOffLight();
        } else {
            turnOnLight();
        }
    }


    public void turnOnLight() {
        Chamber lightedChamber = chambers[chambers.length / 2];

        int up = lightedChamber.getY() + (lightedChamber.getHeight() + 1) / 2;
        int bottom = lightedChamber.getY() - lightedChamber.getHeight() / 2;
        int left = lightedChamber.getX() - lightedChamber.getWidth() / 2;
        int right = lightedChamber.getX() + (lightedChamber.getWidth() + 1) / 2;

        int lightX = lightedChamber.getX();
        int lightY = lightedChamber.getY();

        for (int i = left + 1; i < right; i++) {
            for (int j = bottom + 1; j < up; j++) {
                int distance = Math.max(Math.abs(lightX - i), Math.abs(lightY - j));
                world[i][j] = TETile.darkenBackgroundColor(world[i][j], distance);
            }
        }
    }

    public void turnOffLight() {
        Chamber lightedChamber = chambers[chambers.length / 2];

        int up = lightedChamber.getY() + (lightedChamber.getHeight() + 1) / 2;
        int bottom = lightedChamber.getY() - lightedChamber.getHeight() / 2;
        int left = lightedChamber.getX() - lightedChamber.getWidth() / 2;
        int right = lightedChamber.getX() + (lightedChamber.getWidth() + 1) / 2;

        for (int i = left + 1; i < right; i++) {
            for (int j = bottom + 1; j < up; j++) {
                world[i][j] = new TETile(world[i][j].character(),
                        world[i][j].textColor(),
                        Color.BLACK,
                        world[i][j].description());
            }
        }
    }

    public void saveWorld() {
        World w = new World(random, world, chamberCount,
                MAX_CHAMBER_NUM, chambers, avatarPos, monsterPos, currentTile);
        File outFile = Paths.get(System.getProperty("user.dir"), "previousWorld.txt").toFile();
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(w);
            out.close();
        } catch (IOException excp) {
            System.out.println(excp.getMessage());
        }
    }


    /**
     * Reads in and deserializes a commit from a file with SHA-1 sha1 in COMMIT_FOLDER.
     *
     * @return Commit read from file
     */
    public static World readWorld() {
        File inFile = Paths.get(System.getProperty("user.dir"), "previousWorld.txt").toFile();
        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(inFile));
            World w = (World) inp.readObject();
            inp.close();
            return w;
        } catch (IOException | ClassNotFoundException excp) {
            System.out.println(excp.getMessage());
            World w = null;
            return w;
        }
    }

}

