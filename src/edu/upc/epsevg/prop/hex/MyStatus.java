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
    
    public long getHash(long[][][] zobrist) {
        long hash = 0;
        for(int i = 0; i < getSize(); i++) {
            for(int j = 0; j < getSize(); j++) {
                int valor = super.getPos(i, j);
                hash ^= zobrist[i][j][valor+1];
            }
        }
    
        return hash;
    }

    public boolean movPossible(int i, int j) {
        if (i < 0 || j < 0) return false;
        if (i > getSize() - 1 || j > getSize() - 1) return false;
        return getPos(i, j) == 0;
    }
    
}
