/*
 * Code to handle the creation of the cluedo board.
 *
 * Authors Team11:  Jack Geraghty - 16384181
 *                  Conor Beenham - 16350851
 *                  Alen Thomas   - 16333003
 */

package com.team11.cluedo.board;


import com.team11.cluedo.assets.Assets;
import com.team11.cluedo.pathfinder.Mover;
import com.team11.cluedo.pathfinder.TileBasedMap;

import com.team11.cluedo.board.room.*;

import com.team11.cluedo.ui.Resolution;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

public class Board extends JComponent implements TileBasedMap {

    /*
     * Constants for the board width and height and the size of each tile
     */

    private final int BOARD_WIDTH = 27;
    private final int BOARD_HEIGHT = 26;
    public static final int TILE_SIZE = 30;

    private int tileSize;
    private double resolutionScalar;
    private Assets gameAssets;

    private BoardPos[][] board;

    //Used for pathfinding
    private boolean[][] visited = new boolean[BOARD_WIDTH][BOARD_HEIGHT];

    private ArrayList<Room> rooms = new ArrayList<>();
    private Room kitchen = new Room();
    private Room ballroom = new Room();
    private Room conservatory = new Room();
    private Room diningRoom = new Room();
    private Room billiardRoom = new Room();
    private Room library = new Room();
    private Room lounge = new Room();
    private Room hall = new Room();
    private Room study = new Room();
    private Room cellar = new Room();

    private WeaponPoints weaponPoints = new WeaponPoints();
    private SuspectPoints suspectPoints = new SuspectPoints();

    private DoorData doorData = new DoorData();

    public Board(Resolution resolution, Assets gameAssets) throws IOException{
        try{
            this.gameAssets = gameAssets;
            this.tileSize = (int)(TILE_SIZE * resolution.getScalePercentage());
            this.resolutionScalar = resolution.getScalePercentage();
            this.setLayout(new GridBagLayout());
            board = parseBoardFile();
            addRooms();
            addRoomSecretPassages();
            addDoorPoints();
            addAllSpawns();
        }
        catch(IOException ex){
            ex.printStackTrace(System.out);
        }
    }

    private void addRooms(){
        rooms.add(kitchen);
        rooms.add(ballroom);
        rooms.add(conservatory);
        rooms.add(diningRoom);
        rooms.add(billiardRoom);
        rooms.add(library);
        rooms.add(lounge);
        rooms.add(hall);
        rooms.add(study);
        rooms.add(cellar);
    }

    public Room getRoom(int i){
        return rooms.get(i);
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }

    private BoardPos[][] parseBoardFile() throws IOException {
        GridBagConstraints gbc = new GridBagConstraints();
        BoardPos[][] board = new BoardPos[BOARD_WIDTH][BOARD_HEIGHT];

        InputStream in = getClass().getResourceAsStream("BoardInfo.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String cLine;
        for (int i = 0; (cLine = br.readLine()) != null; i++) {
            String line[] = cLine.split(" ");
            gbc.gridy = i;

            for (int j = 0; j < line.length; j++) {
                gbc.gridx = j;
                BoardPos boardPos = null;

                if (line[j].matches("#")) {
                    boardPos = createNonTraversal(new Point(i,j), TileType.BLANK, TileType.BLANK);
                }
                else if (line[j].matches("1")) {
                    boardPos = createTraversal(new Point(i,j), TileType.SPAWN, TileType.BLANK);
                }
                else if (line[j].matches("2")) {
                    boardPos = new BoardPos(new Point(i,j), false, false, TileType.SECRETPASSAGE, TileType.BLANK, tileSize);
                }
                else if (line[j].matches("K")) {
                    boardPos = createNonTraversal(new Point(i,j), TileType.ROOM, TileType.KITCHEN);
                    kitchen.setRoomType(TileType.KITCHEN);
                    kitchen.getRoomPoints().add(new Point(i,j));
                }
                else if (line[j].matches("B")) {
                    boardPos = createNonTraversal(new Point(i,j), TileType.ROOM, TileType.BALLROOM);
                    ballroom.setRoomType(TileType.BALLROOM);
                    ballroom.getRoomPoints().add(new Point(i,j));
                }
                else if (line[j].matches("C")) {
                    boardPos = createNonTraversal(new Point(i,j), TileType.ROOM, TileType.CONSERVATORY);
                    conservatory.setRoomType(TileType.CONSERVATORY);
                    conservatory.getRoomPoints().add(new Point(i,j));
                }
                else if (line[j].matches("I")) {
                    boardPos = createNonTraversal(new Point(i,j), TileType.ROOM, TileType.DININGROOM);
                    diningRoom.setRoomType(TileType.DININGROOM);
                    diningRoom.getRoomPoints().add(new Point(i,j));
                }
                else if (line[j].matches("T")) {
                    boardPos = createNonTraversal(new Point(i,j), TileType.ROOM, TileType.CELLAR);
                    cellar.setRoomType(TileType.CELLAR);
                    cellar.getRoomPoints().add(new Point(i,j));
                }
                else if (line[j].matches("R")) {
                    boardPos = createNonTraversal(new Point(i,j), TileType.ROOM, TileType.BILLIARDROOM);
                    billiardRoom.setRoomType(TileType.BILLIARDROOM);
                    billiardRoom.getRoomPoints().add(new Point(i,j));
                }
                else if (line[j].matches("U")) {
                    boardPos = createNonTraversal(new Point(i,j), TileType.ROOM, TileType.LOUNGE);
                    lounge.setRoomType(TileType.LOUNGE);
                    lounge.getRoomPoints().add(new Point(i,j));
                }
                else if (line[j].matches("H")) {
                    boardPos = createNonTraversal(new Point(i,j), TileType.ROOM, TileType.HALL);
                    hall.setRoomType(TileType.HALL);
                    hall.getRoomPoints().add(new Point(i,j));
                }
                else if (line[j].matches("S")) {
                    boardPos = createNonTraversal(new Point(i,j), TileType.ROOM, TileType.STUDY);
                    study.setRoomType(TileType.STUDY);
                    study.getRoomPoints().add(new Point(i,j));
                }
                else if (line[j].matches("L")) {
                    boardPos = createNonTraversal(new Point(i,j), TileType.ROOM, TileType.LIBRARY);
                    library.setRoomType(TileType.LIBRARY);
                    library.getRoomPoints().add(new Point(i,j));
                }
                else if (line[j].matches("D")) {
                    boardPos = createTraversal(new Point(i,j), TileType.DOOR, TileType.BLANK);
                }
                else if (line[j].matches("-")) {
                    boardPos = createTraversal(new Point(i,j), TileType.HALLWAY, TileType.BLANK);
                }
                else if (line[j].matches("M")){
                    boardPos = createTraversal(new Point (i,j), TileType.DOORMAT, TileType.BLANK);
                }
                else if (line[j].matches("Q")){
                    boardPos = createTraversal(new Point (i,j), TileType.PREFER, TileType.BLANK);
                }
                else if (line[j].matches("A")){
                    boardPos = createTraversal(new Point(i,j), TileType.AVOID, TileType.BLANK);
                }

                board[i][j] = boardPos;
                this.add(board[i][j], gbc);
            }
        }
        return board;
    }

    private void addRoomSecretPassages(){
        //  0 - Kitchen
        rooms.get(0).setHasSecretPassage(true);
        board[(int)doorData.getKitchenPassagePoints().getX()][(int)doorData.getKitchenPassagePoints().getY()].setRoomType(TileType.KITCHEN);
        //  8 - Study
        rooms.get(8).setHasSecretPassage(true);
        board[(int)doorData.getStudyPassagePoints().getX()][(int)doorData.getStudyPassagePoints().getY()].setRoomType(TileType.STUDY);
        //  2 - Conservatory
        rooms.get(2).setHasSecretPassage(true);
        board[(int)doorData.getConservatoryPassagePoints().getX()][(int)doorData.getConservatoryPassagePoints().getY()].setRoomType(TileType.CONSERVATORY);
        //  6 - Lounge
        rooms.get(6).setHasSecretPassage(true);
        board[(int)doorData.getLoungePassagePoints().getX()][(int)doorData.getLoungePassagePoints().getY()].setRoomType(TileType.LOUNGE);
    }

    private void addDoorPoints(){
        for (int i = 0; i < rooms.size(); i++){
            rooms.get(i).setEntryPoints(doorData.getEntryData(i));
        }

        for (int i = 0; i < rooms.size(); i++){
            rooms.get(i).setExitPoints(doorData.getExitData(i));
        }

        for (Room room : rooms) {
            for (Point doorPoint : room.getEntryPoints()) {
                board[(int)doorPoint.getX()][(int)doorPoint.getY()].setRoomType(room.getRoomType());
            }
        }
    }

    private void addAllSpawns(){
        for (int i = 0; i < 9; i++){
            rooms.get(i).addPositions(rooms.get(i).getWeaponPositions(), weaponPoints.getWeaponSpawnList().get(i));
        }

        for (int i = 0; i < 10; i++){
            rooms.get(i).addPositions(rooms.get(i).getPlayerPositions(), suspectPoints.getPlayerSpawnList().get(i));
        }
    }

    public BoardPos getBoardPos(int x, int y){
        return this.board[x][y];
    }

    public BoardPos[][] getBoard(){
        return this.board;
    }

    private BoardPos createNonTraversal(Point p, TileType tileType, TileType roomType){
        return new BoardPos(p, false, false, tileType, roomType, tileSize);
    }

    private BoardPos createTraversal(Point p, TileType tileType, TileType roomType){
        return new BoardPos(p,true, false, tileType, roomType, tileSize);
    }

    public int getWidthInTiles(){
        return BOARD_WIDTH;
    }

    public int getHeightInTiles(){
        return BOARD_HEIGHT;
    }

    //Determine whether we can move on the tile or not
    public boolean blocked(Mover mover, int x, int y){
        return !board[x][y].isTraversable() || board[x][y].isOccupied();
    }

    //Get the cost of moving from one tile (sx,sy) to (tx,ty)
    public float getCost(Mover mover, int sx, int sy, int tx, int ty){
        if ( (board[sx][sy].getTileType() == TileType.AVOID && board[tx][ty].getTileType() == TileType.DOOR) ||
                (board[sx][sy].getTileType() == TileType.DOOR && board[tx][ty].getTileType() == TileType.AVOID)){
            return 10;
        }
        else {
            return 1;
        }
    }

    //Set a tile to have been visited
    public void pathFinderVisited(int x, int y){
        visited[x][y] = true;
    }

    //Clear that all of the tiles have been visited
    public void clearVisited() {
        for (int x = 0; x < getWidthInTiles(); x++) {
            for (int y = 0; y < getHeightInTiles(); y++) {
                visited[x][y] = false;
            }
        }
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponents(g);

        Image boardImage = gameAssets.getBoardImage();
        ImageIcon board = new ImageIcon(gameAssets.getBoardImage());
        g.drawImage(boardImage, 0, 0,(int)(board.getIconWidth()*resolutionScalar),
                (int)(board.getIconHeight()*resolutionScalar), this);
    }
}
