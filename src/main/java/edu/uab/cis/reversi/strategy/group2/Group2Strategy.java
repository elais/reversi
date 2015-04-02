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
import java.util.function.UnaryOperator;
import edu.uab.cis.reversi.Board;
import java.util.Comparator;
import edu.uab.cis.reversi.Move;
import edu.uab.cis.reversi.Player;
import java.util.PriorityQueue;
import edu.uab.cis.reversi.Square;
import edu.uab.cis.reversi.Strategy;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
/**
 *
 * @author elais
 */
class FunctionalUtils {
	static final UnaryOperator<Node> coin_parity = (final Node node) -> {
          int coin_parity = 100 * ((node.getBoard().getPlayerSquareCounts().get(node.getPlayer()) -
              node.getBoard().getPlayerSquareCounts().get(node.getOpponent()))/
              (node.getBoard().getPlayerSquareCounts().get(node.getPlayer())+
              node.getBoard().getPlayerSquareCounts().get(node.getOpponent())));
              node.setF(coin_parity);		
          return node;
	};
}

class Evaluator {
       /** a variable referencing a lambda taking two Integer arguments and returning an Integer: */
	private final UnaryOperator<Node> strategy;
 
	public Evaluator(final UnaryOperator<Node> lambda) {
                strategy = lambda;
        }
 
        public Node executeStrategy(final Node a) {
                return strategy.apply(a);
        }
}


public class Group2Strategy implements Strategy{
    public static Node alphabeta(Node node, int depth, int alpha, int beta, Evaluator evaluate){
      Node parent = node;
      List<Node> child_list = new ArrayList();
      if(depth==0)
        return evaluate.executeStrategy(node);
      
      if(parent.getBoard().getCurrentPossibleSquares().isEmpty()){
        Node n = new Node(parent.getBoard().pass());
        child_list.add(n);
      } else{
        Iterator<Square> it = parent.getBoard().getCurrentPossibleSquares().iterator();
        while(it.hasNext()){
          Square s = it.next();
          Node n = new Node(parent.getBoard().play(s));
          n.setSquare(s);
          child_list.add(n);
        }
      }
      Iterator<Node> children = child_list.iterator();
      while(children.hasNext()){
        Node child = children.next();
        Node alpha_child = alphabeta(child, depth - 1, -beta, -alpha, evaluate);
        alpha = - alpha_child.getF();
        if(beta <= alpha)
          return alpha_child;
        parent.addChild(child);
        if (alpha > parent.getChildren().peek().getF()){
          child.setF(alpha);
        }
      }
      parent.setF(parent.getChildren().peek().getF());
      parent.setSquare(parent.getChildren().peek().getSquare());
      return parent;
    }

    @Override
    public Square chooseSquare(Board board) {
      Evaluator evaluate;
      evaluate = new Evaluator(FunctionalUtils.coin_parity);
      Square s = alphabeta(new Node(board), 3, Integer.MIN_VALUE, Integer.MAX_VALUE, evaluate).getSquare();
      //System.out.println(board.getPlayerSquareCounts());
      return s;
        
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
    private Player player;
    private Player opponent;
    private Queue<Node> children;
    
    public Node(Board node){
        this.node = node;
        this.children = new PriorityQueue(20, nodeComparator);
        this.player = node.getCurrentPlayer();
        this.opponent = node.getCurrentPlayer().opponent();
        this.f = Integer.MIN_VALUE;
    }
    
    public void play(Square a){
      node.play(a);
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
    
    public Player getPlayer(){
      return player;
    }
    
    public Player getOpponent(){
      return opponent;
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

final class Memoizer<T, U> {

  private final Map<T, U> cache = new ConcurrentHashMap<>();

  private Memoizer() {}

  private Function<T, U> doMemoize(final Function<T, U> function) {
    return input -> cache.computeIfAbsent(input, function::apply);
  }

  public static <T, U> Function<T, U> memoize(final Function<T, U> function) {
    return new Memoizer<T, U>().doMemoize(function);
  }
}

