package edu.upc.epsevg.prop.hex.players;


import edu.upc.epsevg.prop.hex.HexGameStatus;
import edu.upc.epsevg.prop.hex.IAuto;
import edu.upc.epsevg.prop.hex.IPlayer;
import edu.upc.epsevg.prop.hex.MoveNode;
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
public class SHexMachine implements IPlayer, IAuto {

    private String name;
    

    public SHexMachine(String name) {
        this.name = name;
    }

    @Override
    public void timeout() {
        // Nothing to do! I'm so fast, I never timeout 8-)
    }

    /**
     * Decideix el moviment del jugador donat un tauler i un color de peça que
     * ha de posar.
     *
     * @param s Tauler i estat actual de joc.
     * @return el moviment que fa el jugador.
     */
    @Override
    public PlayerMove move(HexGameStatus s) {

        int millorMoviment = 0;
        nombredejugades = 0;
        double valor = LOSS_SCORE;
        for (int i = 0; i < tauler.getMida(); i++){
            Tauler t = new Tauler(tauler);
            if(t.movpossible(i)){
                t.afegeix(i, color);
                double candidat = MIN(t, i, -color, profunditat_maxima-1, LOSS_SCORE, WIN_SCORE);

                if(valor < candidat){
                    valor = candidat;
                    m.addFirst(i);
                    millorMoviment = i;
                }
                
            }
            
        }  
        return new PlayerMove( new Point(i,k), 0L, 0, SearchType.RANDOM);
    }
    
    public double MIN(Tauler tauler, int columnaadversari, int color, int depth, double alfa, double beta){               
        // base case1
        //o es refereix a comprovar si es solucio o a comprovar si ja no es pot jugar mes
        if (depth == 0){
            nombredejugades++;
            return heuristica(tauler, color);
        }
        
        double valor = WIN_SCORE;
        // base case2 - Ha guanyat algu
        if(tauler.solucio(columnaadversari, -color)) return valor + depth;
        
        // general case
        for (int i = 0; i < tauler.getMida(); i++){
            Tauler t = new Tauler (tauler);
            
            if (t.movpossible(i)){
                t.afegeix(i, color);
                if(t.solucio(i, color)) return LOSS_SCORE;
                                
                valor = Math.min(valor, MAX(t, i, -color, depth-1, alfa, beta)); //he de canviar de color realment????????
                
                // poda alfa beta
                if(alfabeta) {
                    beta = Math.min(valor, beta);
                    if(beta <= alfa)
                        break;
                }
            }
        }
        return valor;
    }
    
    private double MAX(Tauler tauler, int columnaadversari, int color, int depth, double alfa, double beta) {
        // base case1
        //o es refereix a comprovar si es solucio o a comprovar si ja no es pot jugar mes
        if (depth == 0){
            nombredejugades++;
            return heuristica(tauler, color);
        }
        
        double valor = LOSS_SCORE;
        // base case2 - Ha guanyat algu
        if(tauler.solucio(columnaadversari, -color)) return valor - depth;
        
        // general case
        for (int i = 0; i < tauler.getMida(); i++){
            Tauler t = new Tauler(tauler);
            
            if (t.movpossible(i)){
                t.afegeix(i, color);
                if(t.solucio(i, color)) return WIN_SCORE;
                
                valor = Math.max(valor, MIN(t, i, -color, depth-1, alfa, beta));
                
                // poda alfa beta
                if(alfabeta) {
                    alfa = Math.max(alfa, valor);
                    if (beta <= alfa) 
                        break;
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

}
