package com.example.bipin.uiforselfdrivingcar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.LinkedList;

import static android.content.ContentValues.TAG;

/**
 * Created by bipin on 1/25/2018.
 */

public class GameView extends View {
    private static final int ROWS = 10, COLS = 7;
    private float cellSize, hMargin, vMargin, innerMargin;
    private Maze maze;
    private Paint paint;
    private static final String ERR = "ERR_VALUE";

    private int prevStartRow, prevStartCol, prevFinishRow, prevFinishCol;


    /**
     * Initializes paint object to paint the rectangles of the grid
     * Initializes maze with known ROWS and COLS
     * Constructor of GameView
     * @param context Context
     * @param attrs AttributeSet
     */
    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        maze = new Maze(ROWS, COLS);
        paint = new Paint();

        prevFinishCol = prevFinishRow = prevStartCol = prevStartRow = -1;

    }


    /**
     * OnDraw is called when the view is created, but we are continuously invalidating the view
     * so that onDraw will be called again and again
     * In this way it essentially acts as the main game loop
     * @param canvas canvas of the GameView where we created Rect to make the grid
     */
    @Override
    protected void onDraw(Canvas canvas) {

        // check if it is ready to be solved
        if (MainActivity.readyToFindPath) {
            MainActivity.readyToFindPath = false;
            maze.findStartAndFinish();
            String pathOfRobot =  maze.solve();
            if (pathOfRobot != ERR) {
                BluetoothMethods.bluetoothWriteMessage(pathOfRobot);
            }
        }

        //Log.e(TAG, "clearScrn: " + MainActivity.clearScreen);

        canvas.drawColor(Color.argb(255, 255, 229, 180));


        int width = getWidth();
        int height = getHeight();

        if (width/height < COLS/ROWS)
            cellSize = width/(COLS + 1);
        else
            cellSize = height/(ROWS + 1);

        hMargin = (width - COLS*cellSize)/2;
        vMargin = (height - ROWS*cellSize)/2;
        innerMargin = 2;

        // offset with margin
        canvas.translate(hMargin, vMargin);

        for (int i=0; i<ROWS; i++)
            for (int j=0; j<COLS; j++) {
                paint.setColor(Color.RED);

                int leftTopX  = (int) (j*cellSize + innerMargin);
                int leftTopY = (int)(i*cellSize + innerMargin);
                int bottomRightX = (int)((j+1)*cellSize);
                int bottomRightY =  (int)((i+1)*cellSize);

                maze.grid[i][j].leftTopX = leftTopX;
                maze.grid[i][j].leftTopY = leftTopY;
                maze.grid[i][j].rightBottomX = bottomRightX;
                maze.grid[i][j].rightBottomY = bottomRightY;

                Rect r = new Rect(leftTopX, leftTopY, bottomRightX, bottomRightY);
                int type = maze.grid[i][j].type;

                if (MainActivity.clearScreen) {
                    type = maze.grid[i][j].type = Cell.OPEN;
                }

                switch (type) {
                    case Cell.OPEN:
                        paint.setColor(Color.WHITE);
                        break;
                    case Cell.PATH:
                        paint.setColor(Color.YELLOW);
                        break;
                    case Cell.BLOCK:
                        paint.setColor(Color.BLACK);
                        break;
                    case Cell.START:
                        paint.setColor(Color.RED);
                        break;
                    case Cell.FINISH:
                        paint.setColor(Color.BLUE);
                        break;
                }

                canvas.drawRect(r, paint);
            }

            MainActivity.clearScreen = false;

            // invalidating so that it has to redraw again
            invalidate();
    }


    /**
     * It has been implemented as to change the nature of the cells in grid to make the cell
     * block or start cell or end cell
     * @param  event MotionEvent event
     * @return boolean
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        if (MainActivity.activeButton != null) {

            // find the cell corresponding to the touch
            float x = event.getX();
            float y = event.getY();


            int ri = -1, rj = -1;
            for (int i=0; i<ROWS; i++)
                for (int j=0; j<COLS; j++) {
                    if (x < maze.grid[i][j].rightBottomX &&
                            x > maze.grid[i][j].leftTopX &&
                            y < maze.grid[i][j].rightBottomY &&
                            y > maze.grid[i][j].leftTopY)
                    {
                        ri = i;
                        rj = j;
                        break;
                    }
                }

            Log.e(TAG, "onTouchEvent: ri: " + ri);
            Log.e(TAG, "onTouchEvent: rj: " + rj);

            // set the color of cell
            int type = Cell.OPEN;
            switch (MainActivity.activeButton.getId()) {
                case R.id.block:
                    type = Cell.BLOCK;
                    break;
                case R.id.start:
                    type = Cell.START;
                    break;
                case R.id.finish:
                    type = Cell.FINISH;
                    break;

            }

            if (type == Cell.START && ri != -1 && rj != -1) {
                if (prevStartCol != -1 && prevStartRow != -1) {
                    maze.grid[prevStartRow][prevStartCol].type = Cell.OPEN;
                }
                prevStartCol = rj;
                prevStartRow = ri;
            }

            if (type == Cell.FINISH && ri != -1 && rj != -1) {
                if (prevFinishCol != -1 && prevFinishRow != -1) {
                    maze.grid[prevFinishRow][prevFinishCol].type = Cell.OPEN;
                }
                prevFinishCol = rj;
                prevFinishRow = ri;
            }

            Log.e(TAG, "onTouchEvent: type: " + type);
            if (ri != -1 && rj != -1) {
                Log.e(TAG, "type of the cell changed");
                maze.grid[ri][rj].type = type;
            }
        }

        return true;
    }


    /**
     * Class Cell
     * It specifies the role of each the Cell eg. Cell.BLOCK, Cell.FINISH
     * Row and Col at which the cell lies along with the type of the cell is defined here
     */
    private class Cell {
        public static final int BLOCK = 1, START = 2, FINISH = 3, OPEN = 0, PATH = 4;

        public int row, col, type;
        public int leftTopX, leftTopY, rightBottomX, rightBottomY;

        public Cell(int row, int col) {
            this.row = row;
            this.col = col;
            this.type = Cell.OPEN;
        }
    }


    /**
     * Maze class, all the logic from representing Maze to finding shortest path are within it
     */
    private class Maze {
        int rows, cols;
        public Cell grid[][];
        int startRow, startCol, finishRow, finishCol;


        /**
         * Constructor of Maze
         * @param rows number of rows in a maze
         * @param cols number of cols in a maze
         */
        public Maze(int rows, int cols) {
            this.rows = rows;
            this.cols = cols;

            grid = new Cell[rows][cols];

            for (int i=0; i<rows; i++)
                for (int j=0; j<cols; j++)
                    grid[i][j] = new Cell(i, j);
        }


        /**
         * Finds the start and finish cells by iterating through all the cells in the grid of maze
         */
        private void findStartAndFinish() {
            boolean startfound = false, finishfound = false;
            for (int i=0; i<ROWS; i++)
                for (int j=0; j<COLS; j++) {
                    if (grid[i][j].type == Cell.START) {
                        startRow = i;
                        startCol = j;
                        startfound = true;
                    }
                    if (grid[i][j].type == Cell.FINISH) {
                        finishCol = j;
                        finishRow = i;
                        finishfound = true;
                    }
                }
            if (!startfound) {
                startCol = startRow = -1;
            }
            if (!finishfound) {
                finishRow = finishCol = -1;
            }
        }


        /**
         * Finds the shortest path from startpoint to finishpoint by using BFS algorithm
         * @return String, which is the string of instrucions to how to move inorder to reach the destination
         * e.g "LRFFLRRL"  where L = turn left by 90 deg, R = turn right by 90 deg, F = move forward
         */
        private String solve() {

            // clear path if already exists
            for (int i=0; i<ROWS; i++)
                for (int j=0; j<COLS; j++)
                    if (maze.grid[i][j].type == Cell.PATH)
                        maze.grid[i][j].type = Cell.OPEN;


            if (startRow == -1 || startCol == -1 || finishRow == -1 || finishCol == -1)
                return ERR;


            boolean visited[][] = new boolean[ROWS][COLS];
            MyPair from[][] = new MyPair[ROWS][COLS];

            // Create a queue for BFS
            LinkedList<MyPair> queue = new LinkedList<MyPair>();

            // Mark the current node as visited and enqueue it
            MyPair pair = new MyPair(startRow, startCol);
            visited[startRow][startCol] = true;

            queue.add(pair);

            boolean pathFound = false;
            while (queue.size() != 0 && !pathFound)
            {
                // Dequeue a vertex from queue and print it
                pair = queue.poll();

                // Get all adjacent vertices of the dequeued vertex s
                // If a adjacent has not been visited, then mark it
                // visited and enqueue it
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {

                        if ((Math.abs(dr) + Math.abs(dc)) % 2 != 0 ) {
                            int nr = pair.first + dr;
                            int nc = pair.second + dc;

                            if (nr >= 0 && nr < ROWS &&
                                nc >= 0 && nc < COLS) {
                                if (!visited[nr][nc] &&
                                    (maze.grid[nr][nc].type == Cell.OPEN ||
                                     maze.grid[nr][nc].type == Cell.FINISH))
                                {
                                    visited[nr][nc] = true;
                                    from[nr][nc] = new MyPair(pair.first, pair.second);
                                    queue.add(new MyPair(nr, nc));
                                    if (nr == finishRow && nc == finishCol)
                                        pathFound = true;
                                }
                            }
                        }
                    }
                }
            }

            if (!pathFound)
                return ERR;


            // Color the path
            int posR = finishRow, posC = finishCol;
            while (from[posR][posC].first != -1 && from[posR][posC].second != -1) {
                int newposR = from[posR][posC].first;
                int newposC = from[posR][posC].second;
                posR = newposR; posC = newposC;
                if (posR == startRow && posC == startCol)
                    break;
                maze.grid[posR][posC].type = Cell.PATH;
            }

            // Send the path inorder to send via bluetooth to the robot
            // L and R for direction (i.e L for rotate left by 90 deg)
            // F says move forward int the set direction
            // Once the path has been found it is set as a string of L, R and F
            // which gives instruction as how to move
            // ASSUMPTION: Robot is always facing North i.e upwards in its initial position


            // code below is the duplicate code from that of coloring the path
            // but separating them like this makes it easy to understand

            String retval = "";

            posR = finishRow;
            posC = finishCol;

            while (from[posR][posC].first != -1 && from[posR][posC].second != -1) {
                int newPosR = from[posR][posC].first;
                int newPosC = from[posR][posC].second;

                retval = nextStep(newPosR, newPosC, posR, posC) + retval;

                posR = newPosR; posC = newPosC;

                if (posR == startRow && posC == startCol)
                    break;
            }

            retval = robotFriendlyString(retval);
            return retval;

        }

    }

    //-------------------------------------------------------------------------------------------

    /**
     * C++ STL equivalent of pair<int,int> used by BFS algorithm while finding the shortest path
     */
    private class MyPair {
        int first, second;
        public MyPair() {
            first = second = -1;
        }
        public MyPair(int first, int second) {
            this.first = first;
            this.second = second;
        }
    }


    /**
     * Generate next step Right R, Left L, Up U, Down D
     * @param r1 initial row
     * @param c1 initial column
     * @param r2 final row
     * @param c2 final column
     * @return char
     */
    private char nextStep(int r1, int c1, int r2, int c2) {
        if (r2 > r1)
            return 'D';
        if (r1 > r2)
            return 'U';
        if (c2 > c1)
            return 'R';
        if (c1 > c2)
            return 'L';
        return '?';
    }

    /**
     * Generate robot friendly string
     * given string like "RURRD" generate a string in the form "FLFRFFRF"
     * Now for robot
     * R  = turn 90 deg right
     * L = turn 90 deg left
     * F = move forward
     * @param s String consisting of R, L, U, D
     * @return String consisting of R, L, and F
     */
    private String robotFriendlyString(String s) {
        // As robot cannot end at the starting place there will be atleast one move Forward command
        String retval = "F";
        for (int i=1; i<s.length(); i++) {
            char c1 = s.charAt(i-1);
            char c2 = s.charAt(i);

            if (c1 == 'L' && c2 == 'U')
                retval += "RF";
            else if (c1 == 'L' && c2 == 'D')
                retval += "LF";
            else if (c1 == 'R' && c2 == 'U')
                retval += "LF";
            else if (c1 == 'R' && c2 == 'D')
                retval += "RF";
            else if (c1 == 'U' && c2 == 'L')
                retval += "LF";
            else if (c1 == 'U' && c2 == 'R')
                retval += "RF";
            else if (c1 == 'D' && c2 == 'L')
                retval += "RF";
            else if (c1 == 'D' && c2 == 'R')
                retval += "LF";

            else if (c1 == 'D' && c2 == 'D')
                retval += "F";
            else if (c1 == 'U' && c2 == 'U')
                retval += "F";
            else if (c1 == 'L' && c2 == 'L')
                retval += "F";
            else if (c1 == 'R' && c2 == 'R')
                retval += "F";

        }
        return retval;
    }
}
