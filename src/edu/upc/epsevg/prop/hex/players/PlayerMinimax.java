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
import java.util.Random;

/**
 * Jugador aleatori
 * @author Jordi i Nima
 */
public class PlayerMinimax implements IPlayer, IAuto {
    final double WIN_SCORE = Double.MAX_VALUE;
    final double LOSS_SCORE = Double.MIN_VALUE;
    boolean alfabeta = true;
    private String name;
    int profunditat_maxima;
    long[][][] zobrist = new long[11][11][3];
    int color;

    public PlayerMinimax(String name, int profm) {
        this.name = name;
        this.profunditat_maxima = profm;
        color = 0;
        for(int i =0; i<11; i++){
            for(int j = 0; j < 11; j++) {
                for(int k = 0; k < 3; k++) {
                    zobrist[i][j][k] = new Random().nextLong();
                }
            }
        }
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
        double valor = Double.MAX_VALUE;
        
        MyStatus ms = new MyStatus(s);
                
        color = s.getCurrentPlayerColor();
        
        for(int i=0;i<ms.getSize();i++){
            for(int k=0;k<ms.getSize();k++){
                if(ms.movPossible(i, k)) {
                    MyStatus status = new MyStatus(ms);
                    status.placeStone(new Point(i, k));
                    double candidat = MIN(status, profunditat_maxima-1, LOSS_SCORE, WIN_SCORE);
                    
                    if(valor < candidat){
                        valor = candidat;
                        millorMoviment = new Point(i, k);
                    }
                }
            }  
        }
        
        return new PlayerMove( millorMoviment, 0L, 0, SearchType.RANDOM);
    }
    
    public double MIN(MyStatus ms, int depth, double alfa, double beta){               
        // base case1
        //o es refereix a comprovar si es solucio o a comprovar si ja no es pot jugar mes
        if (depth == 0){
            return heuristica(ms);
        }
        
        double valor = WIN_SCORE;
        // base case2 - Ha guanyat algu
        if(ms.isGameOver()) return valor + depth;
        
        // general case
        for (int i = 0; i < ms.getSize(); i++){
            for (int j = 0; j < ms.getSize(); j++) {
                MyStatus status = new MyStatus(ms);
            
                if (status.movPossible(i, j)){
                    status.placeStone(new Point(i, j));
                    if(status.isGameOver()) return LOSS_SCORE;

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
    
    private double MAX(MyStatus ms, int depth, double alfa, double beta) {
        // base case1
        //o es refereix a comprovar si es solucio o a comprovar si ja no es pot jugar mes
        if (depth == 0){
            return heuristica(ms);
        }
        
        double valor = LOSS_SCORE;
        // base case2 - Ha guanyat algu
        if(ms.isGameOver()) return valor - depth;
  
        // general case
        for (int i = 0; i < ms.getSize(); i++){
            for (int j = 0; j < ms.getSize(); j++) {
                MyStatus status = new MyStatus(ms);
                if (status.movPossible(i, j)){
                    status.placeStone(new Point(i, j));
                    if(status.isGameOver()) return WIN_SCORE;

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
        return "Random(" + name + ")";
    }

    private double heuristica(HexGameStatus s) {
        
        
        
        return 0.00;
    }
    
    private int[][] convertStatusToMatrix(HexGameStatus s) {
        
        int graph[][] = new int[s.getSize()^2][s.getSize()^2];
    
        for(int i=0;i<s.getSize(); i++) {
            for(int k=0;k<s.getSize();k++) {
                
            }
        }
        return graph;
    }


}
