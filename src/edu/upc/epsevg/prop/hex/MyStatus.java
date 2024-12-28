/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.hex;

import java.awt.Point;
import java.util.Random;

/**
 *
 * @author keyma
 */
public class MyStatus extends HexGameStatus{
    
    int hash;
    long[][][] zobrist = new long[11][11][3];
    public MyStatus(int i) {
        super(i);
    }
    
    public MyStatus(HexGameStatus status) { 
        super(status);
        hash = 0;
        for(int i = 0; i < getSize(); i++) {
            for(int j = 0; j < getSize(); j++) {
                for(int k = 0; k < 3; k++) 
                    zobrist[i][j][k] = new Random().nextLong();
                
                int valor = super.getPos(i, j);
                hash ^= zobrist[i][j][valor+1];
            }
        }
    }

    @Override
    public int hashCode() {
        return hash; 
    }
    
    public boolean movPossible(int i, int j) {
        if (i < 0 || j < 0) return false;
        if (i > getSize() - 1 || j > getSize() - 1) return false;
        return getPos(i, j) == 0;
    }

    @Override
    public void placeStone(Point point) {
        hash ^= zobrist[point.x][point.y][1];
        super.placeStone(point);
        int valor = super.getPos(point);
        hash ^= zobrist[point.x][point.y][valor+1];
    }
    
    public boolean checkAdjacency(Point p1, Point p2) {
        return checkAdjacency(p1.x, p1.y, p2.x, p2.y);
    }
    
    public boolean checkAdjacency(int x1, int y1, int x2, int y2) {
        int distX = Math.abs(x1 - x2);
        int distY = Math.abs(y1 - y2);
        //6 y 14
        if (distX - distY == 0) {
            return getPos(x1,y2) == 0 && getPos(x2,y1) == 0; 
        }
        //8 y 12
        if (distX == 1 && distY == 2) {
            return getPos(x1, (y1+y2)/2) == 0 && getPos(x2, (y1+y2)/2) == 0;
        }
        //2 y 10
        if (distX == 2 && distX == 1) {
            return getPos((x1+x2)/2, y1) == 0 && getPos((x1+x2)/2, y2) == 0;
        }
        return false; 
    }
    
    
    
}
