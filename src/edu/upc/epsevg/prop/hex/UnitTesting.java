/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.upc.epsevg.prop.hex;

import edu.upc.epsevg.prop.hex.HexGameStatus;
import edu.upc.epsevg.prop.hex.PlayerType;
import edu.upc.epsevg.prop.hex.players.PlayerID;
import edu.upc.epsevg.prop.hex.players.PlayerMinimax;
import edu.upc.epsevg.prop.hex.players.ProfeGameStatus2;
import edu.upc.epsevg.prop.hex.players.ProfeGameStatus3;
import edu.upc.epsevg.prop.hex.players.ProfeGameStatus3.Result;
import java.awt.Point;
/**
 *
 * @author bernat
 */
public class UnitTesting {



    public static void main(String[] args) {


        byte[][] board = {
        //X   0  1  2  3  4  5  6  7  8
            { -1, -1, -1, -1, 0, 0, 0, 0, 0},                     // 0   Y
              { -1, 0, 0, 0, 0, 0, 0, 0, 0},                    // 1
                { 0, 0, 0, 1, 0, 0, 0, 0, 0},                  // 2
                  { 0, 0, -1, -1, 0, 0, 0, 0, 0},                // 3
                    { 0, 0, 1, 0, 0, 0, 0, 0, 0},              // 4
                      { 0, 0, 0,0, 0, 0, 0, 0, 0},            // 5
                        { 0, 0, 0, 0, 0, 0, 0, 0, 0},          // 6
                          { 0, 0, 0, 0, 0, 0, 0, 0, 0},        // 7
                            { 0, 0, 0, 0, 0, 0, 0, 0, 0}       // 8    Y
        };


        HexGameStatus gs = new HexGameStatus(board, PlayerType.PLAYER1);
        MyStatus ms = new MyStatus(gs);

        //int h = PlayerID.dijkstraDistanceToWin(gs, -1);//PlayerMinimax.dijkstraDistanceToWin(gs, -1);
        boolean esAdjacente = ms.checkAdjacency(new Point(2, 3), new Point(2, 4));
        System.out.println(esAdjacente);
    }

}
