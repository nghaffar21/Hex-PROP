package edu.upc.epsevg.prop.hex.players;

import edu.upc.epsevg.prop.hex.HexGameStatus;
import edu.upc.epsevg.prop.hex.IAuto;
import edu.upc.epsevg.prop.hex.IPlayer;
import edu.upc.epsevg.prop.hex.MoveNode;
import edu.upc.epsevg.prop.hex.MyStatus;
import edu.upc.epsevg.prop.hex.PlayerMove;
import edu.upc.epsevg.prop.hex.SearchType;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Jugador aleatori
 * @author Jordi i Nima
 */
public class PlayerMinimax implements IPlayer, IAuto {
    final int WIN_SCORE = Integer.MAX_VALUE;
    final int LOSS_SCORE = Integer.MIN_VALUE;
    boolean alfabeta = true;
    private String name;
    int profunditat_maxima;
    int color;
    long nodos_explorados;
    public PlayerMinimax(String name, int profm) {
        this.name = name;
        this.profunditat_maxima = profm;
        color = 0;
        
    }

    @Override
    public void timeout() {
        // Nothing to do! I'm so fast, I never timeout 8-)
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
        Point millorMoviment = null;
        int valor = LOSS_SCORE;
        nodos_explorados = 0;
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
                
        return new PlayerMove( millorMoviment, nodos_explorados, profunditat_maxima, SearchType.RANDOM);
    }
    
    public int MIN(MyStatus ms, int depth, int alfa, int beta){   
        nodos_explorados++;
        int valor = WIN_SCORE;
        // base case1 - Ha guanyat algu
        if(ms.isGameOver()) return valor;
        
        // base case2
        //o es refereix a comprovar si es solucio o a comprovar si ja no es pot jugar mes
        if (depth == 0){
            return heuristica(ms);
        }
        
        // general case
        for (int i = 0; i < ms.getSize(); i++){
            for (int j = 0; j < ms.getSize(); j++) {
                MyStatus status = new MyStatus(ms);
            
                if (status.movPossible(i, j)){
                    status.placeStone(new Point(i, j));
                    //if(status.isGameOver()) return LOSS_SCORE;

                    valor = Math.min(valor, MAX(status, depth-1, alfa, beta)); //he de canviar de color realment????????

                    // poda alfa beta
                    if(alfabeta) {
                        beta = Math.min(valor, beta);
                        if(beta <= alfa)
                            break;
                    }
                }
            }
            
        }
        return valor;
    }
    
    private int MAX(MyStatus ms, int depth, int alfa, int beta) {
        nodos_explorados++;
        int valor = LOSS_SCORE;
        // base case1 - Ha guanyat algu
        if(ms.isGameOver()) return valor;
        
        // base case2
        //o es refereix a comprovar si es solucio o a comprovar si ja no es pot jugar mes
        if (depth == 0){
            return heuristica(ms);
        }
  
        // general case
        for (int i = 0; i < ms.getSize(); i++){
            for (int j = 0; j < ms.getSize(); j++) {
                MyStatus status = new MyStatus(ms);
                if (status.movPossible(i, j)){
                    status.placeStone(new Point(i, j));
                    //if(status.isGameOver()) return WIN_SCORE;

                    valor = Math.max(valor, MIN(status, depth-1, alfa, beta));

                    // poda alfa beta
                    if(alfabeta) {
                        alfa = Math.max(alfa, valor);
                        if (beta <= alfa) 
                            break;
                    }
                }
            }
            
        }
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

        return (oppDist - myDist);
    }

    /**
     * Compute the shortest path for a given player color from one side to the opposite side using Dijkstra.
     * @param s Current game state.
     * @param playerColor The color of the player (1 or 2).
     * @return The shortest path distance. -1 if no path found.
     */
    public static int dijkstraDistanceToWin(HexGameStatus s, int playerColor) {
        int n = s.getSize();
        

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
                int cell = s.getPos(x,0);
                if (cell == playerColor) {
                    dist[0][x] = 0; // same color stone: cost 0
                    pq.add(new Node(0,x,0));
                } else if (cell == 0) {
                    dist[0][x] = 1; // empty: cost 1
                    pq.add(new Node(1,x,0));
                }
            }
        } else {
            // PlayerColor=1: left column as sources
            for (int y=0; y<n; y++) {
                int cell = s.getPos(0,y);
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
            { 1,-1},  {-1, 1},   // NE, SW
            { 1, 0}, { -1, 0},  // E, W
            { 0, 1},  { 0, -1}, // S, N
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
                if (nx<0||nx>=n||ny<0||ny>=n) continue;
                int cell = s.getPos(nx,ny);
                if (cell == playerColor) {
                    // cost 0
                    int nd = cur.dist;
                    if (nd < dist[ny][nx]) {
                        dist[ny][nx] = nd;
                        pq.add(new Node(nd, nx, ny));
                    }
                } else if (cell == 0) {
                    // empty cost 1
                    int nd = cur.dist + 1;
                    if (nd < dist[ny][nx]) {
                        dist[ny][nx] = nd;
                        pq.add(new Node(nd, nx, ny));
                    }
                }
            }
//            System.out.println("x: " + cur.x + "y: " + cur.y);
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



}
