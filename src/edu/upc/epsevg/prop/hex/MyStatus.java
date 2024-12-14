/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.hex;

/**
 *
 * @author keyma
 */
public class MyStatus extends HexGameStatus{
    
    int hash;
    
    public MyStatus(int i) {
        super(i);
    }
    
    public MyStatus(HexGameStatus status) {
        super(status);
    }
    
    public long getHash(long[][][] zobrist, int x, int y) {
        long hash = 0;
        
        int valor = super.getPos(x, y);
        hash ^= zobrist[x][y][valor+1];
    
        return valor;
    }

    public boolean movpossible(int i, int j) {
        if (i < 0 || j < 0) return false;
        if (i > getSize() - 1 || j > getSize() - 1) return false;
        return getPos(i, i) == 0;
    }
    
}
