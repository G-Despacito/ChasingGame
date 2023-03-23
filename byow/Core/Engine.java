package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.StdDraw;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;

    private boolean gameOver;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 16);
        drawMenu();

    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        String ch = input.substring(0, 1);
        World world = new World(WIDTH, HEIGHT, 12345);

        if (ch.equals("n") || ch.equals("N")) {
            world = new World(WIDTH, HEIGHT, extractSeed(input));

            playWorldWithInputString(world,
                    input.substring(Math.min(input.indexOf("s"), input.indexOf("S")) + 1));

        } else if (ch.equals("l") || ch.equals("L")) {
            world = World.readWorld();

            playWorldWithInputString(world, input.substring(1));

        }

        return world.getWorld();
    }


    public Long extractSeed(String input) {
        int p1 = input.indexOf("s");
        int p2 = input.indexOf("S");

        if ((!input.substring(0, 1).equals("n") && !input.substring(0, 1).equals("N"))
                || (p1 == -1 && p2 == -1)) {
            throw new IllegalArgumentException("Illegal input.");
        }

        if (p1 == -1 && p2 != -1) {
            return Long.parseLong(input.substring(1, p2));
        }

        if (p1 != -1 && p2 == -1) {
            return Long.parseLong(input.substring(1, p1));
        }

        return Long.parseLong(input.substring(1, Math.min(p1, p2)));
    }

    public void instruction() {

        drawFrame("In this game, your object is to find the door and escape the maze.");
        drawBottomText("(Press N to proceed.)");
        StdDraw.show();
        waitUtilPress("N");

        drawFrame("To move your avatar, press W (up), S (down), A (left), and D (right).");
        drawBottomText("(Press N to proceed.)");
        StdDraw.show();
        waitUtilPress("N");

        drawFrame("To light the room, press T.");
        drawBottomText("(Press N to proceed.)");
        StdDraw.show();
        waitUtilPress("N");

        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontSmall = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontSmall);
        StdDraw.text(WIDTH / 2, HEIGHT * 5 / 6, "Your avatar is represented with @");
        StdDraw.text(WIDTH / 2, HEIGHT * 4 / 6, "Monster is represented with ▲");
        StdDraw.text(WIDTH / 2, HEIGHT * 3 / 6,
                "Walls are represented with #, which are impenetrable.");
        StdDraw.text(WIDTH / 2, HEIGHT * 2 / 6, "Floors are represented with ·");
        StdDraw.text(WIDTH / 2, HEIGHT * 1 / 6, "Door is represented with █");
        drawBottomText("(Press N to proceed.)");
        StdDraw.show();
        waitUtilPress("N");

        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(fontSmall);
        StdDraw.text(WIDTH / 2, HEIGHT * 5 / 6,
                "You need to navigate your avatar through the rooms and ");
        StdDraw.text(WIDTH / 2, HEIGHT * 4 / 6,
                "hallways to avoid the monster and get to the door.");
        drawBottomText("(Press N to proceed.)");
        StdDraw.show();
        waitUtilPress("N");

        drawFrame("Good luck!");
        drawBottomText("(Press M to go back to the main menu.)");
        StdDraw.show();
        waitUtilPress("M");

        drawMenu();
    }

    public void waitUtilPress(String s) {
        while (!StdDraw.hasNextKeyTyped()) {
            StdDraw.pause(50);
        }
        while (true) {
            String ch = Character.toString(StdDraw.nextKeyTyped());
            if (ch.equals(s.toLowerCase()) || ch.equals(s.toUpperCase())) {
                break;
            }
        }
    }

    public void background() {
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontSmall = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontSmall);
        StdDraw.text(WIDTH / 2, HEIGHT * 5 / 6,
                "A prince is stuck in a maze where a monster is chasing him.");
        StdDraw.text(WIDTH / 2, HEIGHT * 4 / 6,
                "Once the monster got him, he will die.");
        StdDraw.text(WIDTH / 2, HEIGHT * 3 / 6,
                "In the desperate hours, he must do his best —— ");
        StdDraw.text(WIDTH / 2, HEIGHT * 2 / 6,
                "avoid the monster and");
        StdDraw.text(WIDTH / 2, HEIGHT * 1 / 6,
                "escape the maze via the only door...");
        drawBottomText("(Press M to go back to the main menu.)");
        StdDraw.show();
        waitUtilPress("M");

        drawMenu();
    }

    public void drawMenu() {
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);

        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontBig);
        StdDraw.text(WIDTH / 2, HEIGHT * 6 / 7, "Welcome to the Game!");
        Font fontSmall = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontSmall);
        StdDraw.text(WIDTH / 2, HEIGHT * 5 / 7, "Background(B)");
        StdDraw.text(WIDTH / 2, HEIGHT * 4 / 7, "Instruction(I)");
        StdDraw.text(WIDTH / 2, HEIGHT * 3 / 7, "New World(N)");
        StdDraw.text(WIDTH / 2, HEIGHT * 2 / 7, "Load(L)");
        StdDraw.text(WIDTH / 2, HEIGHT * 1 / 7, "Quit(Q)");

        while (!StdDraw.hasNextKeyTyped()) {
            StdDraw.pause(50);
        }
        String ch = Character.toString(StdDraw.nextKeyTyped());

        if (ch.equals("b") || ch.equals("B")) {
            background();

        } else if (ch.equals("i") || ch.equals("I")) {
            instruction();

        } else if (ch.equals("n") || ch.equals("N")) {
            String input = solicitSeedInput();
            World world = new World(WIDTH, HEIGHT, Long.parseLong(input));

            drawMode(world);

        } else if (ch.equals("l") || ch.equals("L")) {
            World world = World.readWorld();

            drawMode(world);

        } else if (ch.equals("q") || ch.equals("Q")) {
            System.exit(0);
        }
    }

    public void drawFrame(String s) {
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);

        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontBig);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, s);
    }

    public void drawMode(World world) {
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontSmall = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontSmall);
        StdDraw.text(WIDTH / 2, HEIGHT * 3 / 4, "Easy Mode(E)");
        StdDraw.text(WIDTH / 2, HEIGHT * 2 / 4, "Medium Mode(M)");
        StdDraw.text(WIDTH / 2, HEIGHT * 1 / 4, "Difficult Mode(D)");
        drawBottomText("(Press to proceed.)");
        StdDraw.show();

        int speed = 200;
        while (!StdDraw.hasNextKeyTyped()) {
            StdDraw.pause(50);
        }
        while (true) {
            String ch = Character.toString(StdDraw.nextKeyTyped());
            if (ch.equals("e") || ch.equals("E")) {
                speed = 200;
                break;
            }
            if (ch.equals("m") || ch.equals("M")) {
                speed = 100;
                break;
            }
            if (ch.equals("d") || ch.equals("D")) {
                speed = 50;
                break;
            }
        }
        playWorldWithKeyboard(world, speed);
    }

    public String solicitSeedInput() {
        String s = "";
        drawFrame(s);

        while (true) {
            while (!StdDraw.hasNextKeyTyped()) {
                StdDraw.pause(50);
            }
            String ch = Character.toString(StdDraw.nextKeyTyped());
            if (ch.equals("s") || ch.equals("S")) {
                drawFrame(s + ch);
                StdDraw.pause(500);
                return s;
            }
            StdDraw.clear(Color.BLACK);
            s += ch;
            drawFrame(s);
        }
    }

    private void drawUpLeftText(String s) {
        StdDraw.setPenColor(Color.WHITE);
        Font fontSmall = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontSmall);
        StdDraw.textLeft(0, this.HEIGHT - 1, s);
        StdDraw.show();
    }

    private void drawBottomText(String s) {
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);

        StdDraw.setPenColor(Color.WHITE);
        Font fontSmall = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontSmall);
        StdDraw.textLeft(WIDTH / 2, 2, s);
        StdDraw.show();
    }

    public void playWorldWithKeyboard(World world, int speed) {
        this.gameOver = false;

        ter.initialize(WIDTH, HEIGHT);
        ter.renderFrame(world.getWorld());
        drawUpLeftText(world.getCurrentTile().description());

        String preCh;
        String curCh = "";
        while (!gameOver) {
            List<World.Pos> path = new ArrayList<>();

            while (!StdDraw.hasNextKeyTyped()) {
                int x = (int) StdDraw.mouseX();
                int y = (int) StdDraw.mouseY();
                ter.renderFrame(world.getWorld());
                drawUpLeftText(world.getWorld()[x][y].description());
                StdDraw.show();

                path = world.updateChasePath();
                ter.renderFrame(world.getWorld());

                StdDraw.pause(speed);
                world.recoverChasePath(path);
                if (!world.monsterChase(path)) {
                    gameOver = true;

                    this.drawFrame("Monster chased! You failed!");
                    StdDraw.show();
                    StdDraw.pause(2000);
                    System.exit(0);
                }
                ter.renderFrame(world.getWorld());
            }

            preCh = curCh;
            curCh = Character.toString(StdDraw.nextKeyTyped());
            if (curCh.equals("w") || curCh.equals("W")) {
                world.moveUp();
                ter.renderFrame(world.getWorld());
            }

            if (curCh.equals("a") || curCh.equals("A")) {
                world.moveLeft();
                ter.renderFrame(world.getWorld());
            }

            if (curCh.equals("s") || curCh.equals("S")) {
                world.moveDown();
                ter.renderFrame(world.getWorld());
            }

            if (curCh.equals("d") || curCh.equals("D")) {
                world.moveRight();
                ter.renderFrame(world.getWorld());
            }

            if (curCh.equals("t") || curCh.equals("T")) {
                world.turnLight();
                ter.renderFrame(world.getWorld());
            }

            if (world.getCurrentTile().character() == Tileset.LOCKED_DOOR.character()) {
                gameOver = true;

                this.drawFrame("Well done! You win!");
                StdDraw.show();
                StdDraw.pause(2000);
                System.exit(0);
            }

            if (preCh.equals(":") && (curCh.equals("q") || curCh.equals("Q"))) {
                world.saveWorld();
                System.exit(0);

                return;
            }
        }
    }


    public void playWorldWithInputString(World world, String input) {
        this.gameOver = false;

//        ter.initialize(WIDTH, HEIGHT);
//        ter.renderFrame(world.getWorld());
//        drawUpLeftText(world.getCurrentTile().description());

        String preCh;
        String curCh = "";
        for (Character ch : input.toCharArray()) {
            preCh = curCh;
            curCh = ch.toString();
            if (curCh.equals("w") || curCh.equals("W")) {
                world.moveUp();
//                ter.renderFrame(world.getWorld());
            }

            if (curCh.equals("a") || curCh.equals("A")) {
                world.moveLeft();
//                ter.renderFrame(world.getWorld());
            }

            if (curCh.equals("s") || curCh.equals("S")) {
                world.moveDown();
//                ter.renderFrame(world.getWorld());
            }

            if (curCh.equals("d") || curCh.equals("D")) {
                world.moveRight();
//                ter.renderFrame(world.getWorld());
            }

            if (world.getCurrentTile().character() == Tileset.LOCKED_DOOR.character()) {
                gameOver = true;

//                this.drawFrame("Well done! You win!");
//                StdDraw.pause(2000);
//                System.exit(0);
            }

            if (preCh.equals(":") && (curCh.equals("q") || curCh.equals("Q"))) {
                world.saveWorld();
//                System.exit(0);

                return;
            }
        }
    }
}
