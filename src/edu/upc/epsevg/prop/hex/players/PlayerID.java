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
import java.util.ArrayList;
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
    
    private int MAX(MyStatus ms, int depth, int alpha, int beta) {
        nodos_explorados++;
        int valor = LOSS_SCORE;

        // base-case checks
        if (ms.isGameOver()) return valor;
        if (depth == 0 || timedOut) {
            return heuristica(ms);
        }

        Point millorMoviment = new Point(-1,-1);
        // If we have a cached move for this position, etc. (optional)
        if(hashMap.containsKey(ms)) {
            MyStatus status = new MyStatus(ms);
            valor = MAX(status, depth-1, alpha, beta);
            millorMoviment = hashMap.get(ms);
            //return valor;
        }

        // ---------------------------
        // 1) Generate all children
        // ---------------------------
        ArrayList<MoveCandidate> children = new ArrayList<>();
        for (int i = 0; i < ms.getSize(); i++) {
            for (int j = 0; j < ms.getSize(); j++) {
                if (ms.movPossible(i, j)) {
                    MyStatus child = new MyStatus(ms);
                    child.placeStone(new Point(i, j));

                    // Quick "predicted" score to help ordering.
                    // Typically just call your current 'heuristica(child)' 
                    // or retrieve from transposition table if stored.
                    int predictedScore = heuristica(child);

                    MoveCandidate mc = new MoveCandidate(i, j, predictedScore, child);
                    children.add(mc);
                }
            }
        }

        if (children.isEmpty()) {
            // no moves => treat as losing or just return heuristic
            return heuristica(ms);
        }

        // ---------------------------------
        // 2) Sort them by predictedScore
        //    descending => best child first
        // ---------------------------------
        children.sort((a, b) -> Integer.compare(b.predictedScore, a.predictedScore));

        // -------------------------------------
        // 3) Expand them in sorted order
        // -------------------------------------
        //Point millorMoviment = new Point(-1, -1);
        for (MoveCandidate c : children) {
            //if (timedOut) break;

            // Evaluate
            int candidat = MIN(c.childStatus, depth - 1, alpha, beta);
            if (candidat > valor) {
                valor = candidat;
                millorMoviment = new Point(c.x, c.y);
            }

            if (alfabeta) {
                alpha = Math.max(alpha, valor);
                if (beta <= alpha) {
                    break; // cutoff
                }
            }
        }

        // If we want to store the best move in our transposition table:
        hashMap.put(ms, millorMoviment);

        return valor;
    }

    private int MIN(MyStatus ms, int depth, int alpha, int beta) {
        nodos_explorados++;
        int valor = WIN_SCORE;

        // base-case checks
        if (ms.isGameOver()) return valor;
        if (depth == 0 || timedOut) {
            return heuristica(ms);
        }

        Point millorMoviment = new Point(-1,-1);
        // If we have a cached move for this position, etc. (optional)
        if(hashMap.containsKey(ms)) {
            MyStatus status = new MyStatus(ms);
            valor = MAX(status, depth-1, alpha, beta);
            millorMoviment = hashMap.get(ms);
            //return valor;
        }
        
        // Generate children
        ArrayList<MoveCandidate> children = new ArrayList<>();
        for (int i = 0; i < ms.getSize(); i++) {
            for (int j = 0; j < ms.getSize(); j++) {
                if (ms.movPossible(i, j)) {
                    MyStatus child = new MyStatus(ms);
                    child.placeStone(new Point(i, j));

                    int predictedScore = heuristica(child);
                    MoveCandidate mc = new MoveCandidate(i, j, predictedScore, child);
                    children.add(mc);
                }
            }
        }

        if (children.isEmpty()) {
            return heuristica(ms);
        }

        // Sort: again in descending order 
        // (though "true" MIN might want ascending, but we'll keep it consistent)
        children.sort((a, b) -> Integer.compare(b.predictedScore, a.predictedScore));

        //Point millorMoviment = new Point(-1, -1);
        for (MoveCandidate c : children) {
            //if (timedOut) break;

            int candidat = MAX(c.childStatus, depth - 1, alpha, beta);
            if (candidat < valor) {
                valor = candidat;
                millorMoviment = new Point(c.x, c.y);
            }

            if (alfabeta) {
                beta = Math.min(beta, valor);
                if (beta <= alpha) {
                    break; // cutoff
                }
            }
        }

        hashMap.put(ms, millorMoviment);

        return valor;
    }

    // Helper class to store each child's info (move + predicted score + resulting status).
    private static class MoveCandidate {
        public int x, y;
        public int predictedScore;
        public MyStatus childStatus;

        public MoveCandidate(int x, int y, int predictedScore, MyStatus childStatus) {
            this.x = x;
            this.y = y;
            this.predictedScore = predictedScore;
            this.childStatus = childStatus;
        }
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
        //int heuristica2 = heuristicaCentre(s, oppColor);
        
        //int blockValue = blockingHeuristic(s, color);
        //int alpha = 1;
        //if(oppDist < 4)
        //    alpha = 4;
        
        //int beta = 1;
        //if(myDist < 7)
          //  beta = 0;
        
        int zigzagValue = zigzagHeuristic(s);
        
        int opponentWin = 0;
        if (oppDist <= 3) {
            // Ejemplos: si oppDist=1 => penaliza -18000, etc.
            opponentWin = -6000 * (4 - oppDist);
        }
        
        int myWin = 0;
        if (myDist <= 3) {
            // Ejemplos: si oppDist=1 => penaliza -18000, etc.
            myWin = 6000 * (4 - myDist);
        }
        
        int myConnections = conectividadHeuristic(s, color);
        int oppConnections = conectividadHeuristic(s, oppColor);
        
        calculateCentralControl(s, color);
        
        return 30 * (oppDist - myDist) + 4 * (oppConnections - myConnections) + 2 * calculateCentralControl(s, color) + opponentWin + myWin; //+ alpha * blockValue; //+ beta * zigzagValue; //+ alpha * blockValue; //+ 3 * heuristica2;
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
            //{ 1, -2}, {-1, -1},
            //{-2, 1}, {-1, 2},
            //{1, 1}, {2, -1},
            { 1,-1},  {-1, 1},   // NE, SW
            { 1, 0}, { -1, 0},  // E, W
            { 0, 1},  { 0, -1} // S, N
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
    
    private int heuristicaCentre(HexGameStatus ms, int oppColor) {
    
        int[][] weightingTable = {
            { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
            { 1,  1,  2,  2,  2,  2,  2,  2,  2,  1,  1},
            { 1,  2,  3,  3,  3,  3,  3,  3,  3,  2,  1},
            { 1,  2,  3,  4,  4,  4,  4,  4,  3,  2,  1},
            { 1,  2,  3,  4,  5,  5,  5,  4,  3,  2,  1},
            { 1,  2,  3,  4,  5, 10,  5,  4,  3,  2,  1}, // center row
            { 1,  2,  3,  4,  5,  5,  5,  4,  3,  2,  1},
            { 1,  2,  3,  4,  4,  4,  4,  4,  3,  2,  1},
            { 1,  2,  3,  3,  3,  3,  3,  3,  3,  2,  1},
            { 1,  1,  2,  2,  2,  2,  2,  2,  2,  1,  1},
            { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1}
        };
        
        int heuristica = 0;
        for (int i = 0; i < ms.getSize(); i++){
            for (int j = 0; j < ms.getSize(); j++) {
                //if(ms.getPos(i, j) == oppColor)
                  //  heuristica -= weightingTable[i][j];
                if(ms.getPos(i, j) == -oppColor)
                    heuristica += weightingTable[i][j];
                
            }
        }
        return heuristica;
    }
    
        /**
     * Measures how well I'm "blocking" the opponent by counting,
     * for each opponent stone, how many neighbors are myColor.
     * The more I surround the opponent, the higher the blockScore.
     */
    private int blockingHeuristic(HexGameStatus s, int myColor) {
        int oppColor = -color;
        int n = s.getSize();

        // Standard hex adjacency for pointy-top
        int[][] dirs = {
            {+1,  0}, { -1,  0},
            { 0, +1}, {  0, -1},
            {+1, -1}, { -1, +1}
        };

        int blockScore = 0;
        // For each cell that belongs to the opponent...
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                if (s.getPos(x, y) == oppColor) {
                    // Count neighbors that are my stones.
                    for (int[] d: dirs) {
                        int nx = x + d[0];
                        int ny = y + d[1];
                        // Check valid bounds
                        if (nx >= 0 && nx < n && ny >= 0 && ny < n) {
                            if (s.getPos(nx, ny) == myColor) {
                                blockScore++;
                            }
                        }
                    }
                }
            }
        }
        return blockScore;
    }
    
    private int zigzagHeuristic(HexGameStatus s) {

        int n = s.getSize();

        // Standard hex adjacency for pointy-top
        int[][] dirs = {
            { 1, -2}, {-1, -1},
            {-2, 1}, 
            {-1, 2},
            {1, 1}, 
            {2, -1}
        };

        int zigzagScore = 0;
        // For each cell that belongs to the opponent...
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                if (s.getPos(x, y) == color) {
                    // Count neighbors that are my stones.
                    for (int[] d: dirs) {
                        int nx = x + d[0];
                        int ny = y + d[1];
                        // Check valid bounds
                        if (nx >= 0 && nx < n && ny >= 0 && ny < n) {
                            if (s.getPos(nx, ny) == color) {
                                zigzagScore++;
                            }
                        }
                    }
                }
            }
        }
        return zigzagScore;
    }


        /**
     * Calcula el número de conexiones entre piezas propias en el tablero.
     * (Un conteo rápido de aristas internas en el grafo de posiciones ocupadas).
     * 
     * @param s Estado actual del juego.
     * @param color Color del jugador (1 para PLAYER1, -1 para PLAYER2).
     * @return Número de conexiones.
     */
    private int conectividadHeuristic(HexGameStatus s, int playerColor) {
        int n = s.getSize();
        int connections = 0;
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                if (s.getPos(x, y) == playerColor) {
                    for (Point vecino : s.getNeigh(new Point(x, y))) {
                        if (s.getPos(vecino.x, vecino.y) == playerColor) {
                            connections++;
                        }
                    }
                }
            }
        }
        return connections / 2; // Cada arista contada 2 veces
    }
    
    private int calculateCentralControl(HexGameStatus s, int playerColor) {
        int n = s.getSize();
        int control = 0;
        double center = (n - 1) / 2;
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                if (s.getPos(x, y) == playerColor) {
                    double dist = Math.sqrt(Math.pow(x - center, 2) + Math.pow(y - center, 2));
                    control += (n / 2 - dist);
                }
            }
        }
        return control;
    }
    
}
