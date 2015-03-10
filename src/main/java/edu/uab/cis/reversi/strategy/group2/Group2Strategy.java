/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uab.cis.reversi.strategy.group2;

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
import java.util.HashSet;
import java.util.Queue;
/**
 *
 * @author elais
 */
public class Group2Strategy implements Strategy{
    
    public Node alphabeta(Node node, int depth, int alpha, int beta){


        if(depth==0){
            System.out.print("here");
            return evaluate(node);
        }

        
        if(node.getBoard().getCurrentPossibleSquares().isEmpty()){
            node.getBoard().pass();
        } else{
            for(Square s: node.getBoard().getCurrentPossibleSquares()){
            Node n = new Node(node.getBoard().play(s));
            n.setSquare(s);
            node.addChild(n);
            }       
        }
        
        Node fav_child = node.getChildren().get(0);
        for(Node n : node.getChildren()){
            if(alpha >= beta){
                break;
            }
            
            Node pretender = alphabeta(n, depth - 1, -beta, -alpha);
            if(-pretender.getF() > alpha){
                alpha = -pretender.getF();
                fav_child = pretender;
            }
            
            
        }

        return fav_child;
    }

    @Override
    public Square chooseSquare(Board board) {
        Node n = new Node(board);
        return alphabeta(n, 3, Integer.MIN_VALUE, Integer.MAX_VALUE).getSquare();
        
    }
    
    public Node evaluate(Node node){
        node.setF(node.getBoard().getCurrentPossibleSquares().size());
        return node;   
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
    private Square s;
    private ArrayList<Node> children;
    
    public Node(Board node){
        this.node = node;
    }


    
    public Board getBoard(){
        return node;
    }
    
    public int getF(){
        return f;
    }
    
    public void setF(int f){
        this.f = f;
    }
    
    public int getH(){
        return h;
    }
    
    public void setSquare(Square s){
        this.s = s;
    }
    
    public Square getSquare(){
        return s;
    }
    
    public ArrayList<Node> getChildren(){
        return children;
    }
    
    public void addChild(Node child){
        children.add(child);
    }

}