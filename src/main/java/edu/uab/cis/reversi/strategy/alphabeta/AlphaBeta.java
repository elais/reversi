/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uab.cis.reversi.strategy.alphabeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import edu.uab.cis.reversi.Board;
import java.util.Comparator;
import edu.uab.cis.reversi.Move;
import edu.uab.cis.reversi.Player;
import java.util.PriorityQueue;
import edu.uab.cis.reversi.Square;
import edu.uab.cis.reversi.Strategy;
import java.util.Queue;
/**
 *
 * @author elais
 */
public class AlphaBeta implements Strategy{
    
    public void alphabeta(Node node, int depth, int alpha, int beta, Player max){
        
        
        
        if(depth==0){
            
        }
        
        
        
        if(node.getBoard().getCurrentPossibleSquares().isEmpty()){
            node.getBoard().pass();
        } else{
            Square best_move = node.getBoard().getCurrentPossibleSquares().iterator().
            
        }
    }

    @Override
    public Square chooseSquare(Board board) {
        Board curr = board;
        Player player = board.getCurrentPlayer();
        Queue<Node> open_list = new PriorityQueue<>(20, nodeComparator);
        return null;
        
    }
    

    
    public static Comparator<Node> nodeComparator = new Comparator<Node>(){
        @Override
        public int compare (Node n1, Node n2){
            if(n1.getF() == n2.getF()){
                return n1.getF();
            } else{
                return n1.getF() - n2.getF();
            }
        }
    };
    
}

final class Node{
    
    private final Board node;
    private int g;
    private int h;
    private int f;
    private Node parent;
    
    public Node(Board node, int playerMoves){
        this.node = node;
        this.f = playerMoves;
    }


    
    public Board getBoard(){
        return node;
    }
    
    public int getG(){
        return g;
    }
    
    public void setG(int g){
        this.g = g;
        this.f = g;
    }
    
    public int getH(){
        return h;
    }
    
    public int getF(){
        return f;
    }
    
    public Node getParent(){
        return parent;
    }
    
    public void setParent(Node parent){
        this.parent = parent;
    }

}