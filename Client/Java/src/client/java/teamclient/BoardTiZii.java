package client.java.teamclient;

import common.board.Cell;
import common.player.Player;

import java.util.Arrays;

/**
 * Class name: BoardTiZii
 *
 * Date 12/2/2015
 */
public class BoardTiZii {
    public int rows, cols;                      // Dimensions of the grid
    public int[][] grid;                        // Contains info gathered from map

    public BoardTiZii(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new int[rows][cols];
        for (int i=0 ; i<rows ; i++) {
            Arrays.fill(grid[i], Consts.UNSEEN);
        }
    }

    public void setCell(Cell cell, int value){
        grid[cell.getRowNumber()][cell.getColumnNumber()] = value;
    }

    public void addPlayerSight(Player player){
        Cell cell = player.getCell();
        for(int i=cell.getRowNumber() - 1; i<=cell.getRowNumber()+1 ; i++){
            for(int j=cell.getColumnNumber() - 1; j<=cell.getColumnNumber()+1 ; j++){
                if(inRange(i, j) && grid[i][j]==Consts.UNSEEN) grid[i][j] = Consts.SEEN;
            }
        }
    }

    private boolean inRange(int i, int j) {
        return (i<rows && i>=0 && j<cols && j>=0);
    }

    public class Consts{
        public static final int UNSEEN = 0;
        public static final int SEEN   = 1;
        public static final int Gold   = 3;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        for(int i=0 ; i<rows ; i++){
            for(int j=0 ; j<cols; j++){
                sb.append(grid[i][j]);
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
