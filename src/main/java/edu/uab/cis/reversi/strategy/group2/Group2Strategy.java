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
import java.util.concurrent.TimeUnit;
/**
 *
 * @author elais
 */
public class Group2Strategy implements Strategy{
    private Player maximizing_player;
    public Node alphabeta(Node node, int depth, int alpha, int beta){

        if(depth==0)
          return evaluate(node);
        
        if(node.getBoard().getCurrentPossibleSquares().isEmpty()){
          Node n = new Node(node.getBoard().pass());
          node.addChild(n);
        }
        else {
          for(Square s : node.getBoard().getCurrentPossibleSquares()) {
            Node n = new Node(node.getBoard().play(s));
            n.setSquare(s);
            node.addChild(n);
            alpha = - alphabeta(n, depth - 1, -beta, -alpha).getF();
            
            if(beta <= alpha)
              break;
            
            if (alpha > node.getChildren().peek().getF()){
             node.setSquare(n.getSquare());
             node.getChildren().peek().setF(alpha);
            }
          }
        }

        return node;
    }
    

    @Override
    public Square chooseSquare(Board board) {
        maximizing_player = board.getCurrentPlayer();
        Square s = alphabeta(new Node(board), 3, Integer.MIN_VALUE, Integer.MAX_VALUE).getSquare();
        return s;
        
    }
    
    public Node evaluate(Node node){
        node.setF(node.getBoard().getCurrentPossibleSquares().size());
        return node;   
    }
    
    @Override
    public void setChooseSquareTimeLimit(long time, TimeUnit unit) {
    // by default, do nothing
        time=1000;
        unit=TimeUnit.MILLISECONDS;
    }    

    

    
}

final class Node{
    
    private final Board node;
    private int g;
    private int h;
    private int f;
    private Square s;
    private Queue<Node> children;
    
    public Node(Board node){
        this.node = node;
        this.children = new PriorityQueue(20, nodeComparator);
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
    
    public Queue<Node> getChildren(){
        return children;
    }
    
    public void addChild(Node child){
        children.add(child);
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