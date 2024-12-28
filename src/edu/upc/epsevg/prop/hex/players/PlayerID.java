/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.hex.players;

import edu.upc.epsevg.prop.hex.HexGameStatus;
import edu.upc.epsevg.prop.hex.IAuto;
import edu.upc.epsevg.prop.hex.IPlayer;
import edu.upc.epsevg.prop.hex.MyStatus;
import edu.upc.epsevg.prop.hex.PlayerMove;
import edu.upc.epsevg.prop.hex.SearchType;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

/**
 *
 * @author Asus
 */
public class PlayerID implements IPlayer, IAuto {
    
    final int WIN_SCORE = Integer.MAX_VALUE;
    final int LOSS_SCORE = Integer.MIN_VALUE;
    boolean alfabeta = true;
    private String name;
    
    int color;
    long nodos_explorados;
    boolean timedOut = false;
    Map<MyStatus, Point> hashMap = new HashMap<>();

    public PlayerID(String name) {
        this.name = name;
        color = 0;
    }

    @Override
    public void timeout() {
        // Nothing to do! I'm so fast, I never timeout 8-)
        timedOut = true;
    }

    /**
     * Decideix el moviment del jugador donat un s i un color de peça que
     * ha de posar.
     *
     * @param s HexGameStatus i estat actual de joc.
     * @return el moviment que fa el jugador.
     */
    @Override
    public PlayerMove move(HexGameStatus s) {
        timedOut = false;
        nodos_explorados = 0;
        int depth = 1;
        PlayerMove pm = new PlayerMove( new Point(0,0), nodos_explorados, depth, SearchType.RANDOM);
        while(!timedOut) {
            pm = doMiniMax(s, depth);
            depth++;
        }
        return pm;

    }
    
    public PlayerMove doMiniMax(HexGameStatus s, int profunditat_maxima) { 
        
        Point millorMoviment = new Point(0,0);
        int valor = LOSS_SCORE;
        
        MyStatus ms = new MyStatus(s);
                
        color = s.getCurrentPlayerColor();
            
        for(int i=0;i<ms.getSize();i++){
            for(int k=0;k<ms.getSize();k++){
                if(ms.movPossible(i, k)) {
                    MyStatus status = new MyStatus(ms);
                    status.placeStone(new Point(i, k));
                    int candidat = MIN(status, profunditat_maxima-1, LOSS_SCORE, WIN_SCORE);
                    if(valor < candidat){
                        valor = candidat;
                        millorMoviment = new Point(i, k);
                    }
                }
            }  
        }
        hashMap.put(ms, millorMoviment);
        return new PlayerMove( millorMoviment, nodos_explorados, profunditat_maxima, SearchType.RANDOM);
    }
    
    public int MIN(MyStatus ms, int depth, int alfa, int beta){   
        
        nodos_explorados++;
        int valor = WIN_SCORE;
        // base case1 - Ha guanyat algu
        if(ms.isGameOver()) return valor;
        
        // base case2
        //o es refereix a comprovar si es solucio o a comprovar si ja no es pot jugar mes
        if (depth == 0 || timedOut){
            return heuristica(ms);
        }
        Point millorMoviment = new Point(-1,-1);
        if(hashMap.containsKey(ms)) {
            MyStatus status = new MyStatus(ms);
            valor = MAX(status, depth-1, alfa, beta);
            millorMoviment = hashMap.get(ms);
        }
        // general case
        for (int i = 0; i < ms.getSize(); i++){
            for (int j = 0; j < ms.getSize(); j++) {
                MyStatus status = new MyStatus(ms);
                if (millorMoviment.x != i || millorMoviment.y != j) {
                    if (status.movPossible(i, j)){
                        status.placeStone(new Point(i, j));
                        //if(status.isGameOver()) return LOSS_SCORE;

                        int candidat = MAX(status, depth-1, alfa, beta);
                        if(valor > candidat){
                            valor = candidat;
                            millorMoviment = new Point(i, j);
                        }

                        // poda alfa beta
                        if(alfabeta) {
                            beta = Math.min(valor, beta);
                            if(beta <= alfa)
                                break;
                        }
                    }
                }
            }
        }
        hashMap.put(ms, millorMoviment);
        return valor;
    }
    
    private int MAX(MyStatus ms, int depth, int alfa, int beta) {
        
        nodos_explorados++;
        int valor = LOSS_SCORE;
        // base case1 - Ha guanyat algu
        if(ms.isGameOver()) return valor;
        
        // base case2
        //o es refereix a comprovar si es solucio o a comprovar si ja no es pot jugar mes
        if (depth == 0 || timedOut){
            return heuristica(ms);
        }
        Point millorMoviment = new Point(-1,-1);
        if(hashMap.containsKey(ms)) {
            MyStatus status = new MyStatus(ms);
            valor = MAX(status, depth-1, alfa, beta);
            millorMoviment = hashMap.get(ms);
        }
        // general case
        for (int i = 0; i < ms.getSize(); i++){
            for (int j = 0; j < ms.getSize(); j++) {
                MyStatus status = new MyStatus(ms);
                if (millorMoviment.x != i || millorMoviment.y != j) {
                    if (status.movPossible(i, j)){
                        status.placeStone(new Point(i, j));
                        //if(status.isGameOver()) return WIN_SCORE;

                        int candidat = MIN(status, depth-1, alfa, beta);
                        if(valor < candidat){
                            valor = candidat;
                            millorMoviment = new Point(i, j);
                        }

                        // poda alfa beta
                        if(alfabeta) {
                            alfa = Math.max(alfa, valor);
                            if (beta <= alfa) 
                                break;
                        }
                    }
                }
            }
        }
        hashMap.put(ms, millorMoviment);
        return valor;
    }
    
    /**
     * Ens avisa que hem de parar la cerca en curs perquè s'ha exhaurit el temps
     * de joc.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Heuristic function using Dijkstra's shortest path to estimate who is closer to winning.
     * The heuristic is: opponentDistance - myDistance.
     * A positive high value means we are much closer than our opponent, which is good for us.
     */
    private int heuristica(HexGameStatus s) {
        int myDist = dijkstraDistanceToWin(s, color);
        int oppColor = (color == 1) ? -1 : 1;
        int oppDist = dijkstraDistanceToWin(s, oppColor);

        // If no path for one of them, we treat that distance as very large.
        if (myDist < 0) myDist = LOSS_SCORE;
        if (oppDist < 0) oppDist = WIN_SCORE;

        // heuristica de centre
        int heuristica2 = heuristicaCentre(s, oppColor);
        
        return (oppDist - myDist) + heuristica2;
    }

    /**
     * Compute the shortest path for a given player color from one side to the opposite side using Dijkstra.
     * @param s Current game state.
     * @param playerColor The color of the player (1 or 2).
     * @return The shortest path distance. -1 if no path found.
     */
    private int dijkstraDistanceToWin(HexGameStatus s, int playerColor) {
        MyStatus ms = new MyStatus(s);
        int n = ms.getSize();

        // We'll create a graph implicitly. Each cell: node
        // Player 1: connect top to bottom
        // Player 2: connect left to right

        // Create a distance array
        int dist[][] = new int[n][n];
        for (int i=0; i<n; i++)
            for (int j=0; j<n; j++)
                dist[i][j] = Integer.MAX_VALUE;

        // Priority queue for Dijkstra
        // Each entry: (distance, x, y)
        PriorityQueue<Node> pq = new PriorityQueue<>();
        
        // Initialize sources:
        if (playerColor == -1) {
            // Top row as sources
            for (int x=0; x<n; x++) {
                int cell = ms.getPos(x,0);
                if (cell == playerColor) {
                    dist[0][x] = 0; // same color stone: cost 0
                    pq.add(new Node(0,x,0));
                } else if (cell == 0) {
                    dist[0][x] = 1; // empty: cost 1
                    pq.add(new Node(1,x,0));
                } else {
                    // Opponent stone: unreachable
                }
            }
        } else {
            // PlayerColor=2: left column as sources
            for (int y=0; y<n; y++) {
                int cell = ms.getPos(0,y);
                if (cell == playerColor) {
                    dist[y][0] = 0;
                    pq.add(new Node(0,0,y));
                } else if (cell == 0) {
                    dist[y][0] = 1;
                    pq.add(new Node(1,0,y));
                }
            }
        }
  
        // Directions for hex neighbors
        // Assuming (x,y) with x as column, y as row:
        int[][] dirs = {
            //{ 1, -2}, {-1, -1},
            //{-2, 1}, {-1, 2},
            //{1, 1}, {2, -1},
            { 1, 0}, { -1, 0},  // E, W
            { 0, 1},  { 0, -1}, // S, N
            { 1,-1},  {-1, 1}   // NE, SW
        };

        while(!pq.isEmpty()) {
            Node cur = pq.poll();
            if (cur.dist > dist[cur.y][cur.x]) continue; // outdated

            // Check if we reached the opposite side
            if (playerColor == -1) {
                // Check bottom row
                if (cur.y == n-1) {
                    return cur.dist;
                }
            } else {
                // Check right column
                if (cur.x == n-1) {
                    return cur.dist;
                }
            }
            
            // Explore neighbors
            for (int[] d : dirs) {
                int nx = cur.x + d[0];
                int ny = cur.y + d[1];
                if (!ms.movPossible(nx, ny)) continue;
                int cell = ms.getPos(nx,ny);
                if (cell == playerColor) {
                    // cost 0
                    int nd = cur.dist;
                    if (nd < dist[nx][ny]) {
                        dist[nx][ny] = nd;
                        pq.add(new Node(nd, nx, ny));
                    }
                } else if (cell == 0) {
                    // empty cost 1
                    int nd = cur.dist + 1;
                    if (nd < dist[nx][ny]) {
                        dist[nx][ny] = nd;
                        pq.add(new Node(nd, nx, ny));
                    }
                }
            }

        }

        // No path found
        return -1;
    }

    // Helper class for Dijkstra
    private static class Node implements Comparable<Node> {
        int dist, x, y;
        Node(int dist, int x, int y) {
            this.dist = dist;
            this.x = x;
            this.y = y;
        }

        @Override
        public int compareTo(Node o) {
            return Integer.compare(this.dist, o.dist);
        }
    }
    
    private int heuristicaCentre(HexGameStatus ms, int oppColor) {
    
        int[][] weightingTable = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // row 0
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // row 1
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0}, // row 2
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0}, // row 3
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0}, // row 4
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0}, // row 5 (center row)
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0}, // row 6
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0}, // row 7
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0}, // row 8
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // row 9
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}  // row 10
        };
        
        int heuristica = 0;
        for (int i = 0; i < ms.getSize(); i++){
            for (int j = 0; j < ms.getSize(); j++) {
                if(ms.getPos(i, j) == oppColor)
                    heuristica -= weightingTable[i][j];
                else if(ms.getPos(i, j) == -oppColor)
                    heuristica += weightingTable[i][j];
                
            }
        }
        return heuristica;
    }

    
}
