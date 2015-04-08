/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uab.cis.reversi.strategy.group2;

import java.util.ArrayList;
import java.util.List;
import edu.uab.cis.reversi.Board;
import edu.uab.cis.reversi.Player;
import edu.uab.cis.reversi.Square;
import edu.uab.cis.reversi.Strategy;
import edu.uab.cis.reversi.strategy.group2.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToDoubleBiFunction;
/**
 *
 * @author elais
 */


public class Group2Strategy implements Strategy{
  //private long startTime;
  
  //This is the alpha beta pruning algorithm
  public Leaf alphaBeta(Leaf leaf, int depth, double alpha, double beta, Evaluator evaluate) {
    //these are checks put in to place to make sure we have not run out of time
    //or that the search has now reached its max depth
    //on reaching the time limit the search cuts off and calculates the nodes at
    //the given depth
    if((System.nanoTime() - startTime) > TimeUnit.MILLISECONDS.toNanos(time - buffer)){
        //System.out.println("here");
        return null;
    }
    

    //Transposition table
    double alphaOriginal = alpha;
    //transposition table look up, node is the lookup key
    //this speeds up search a little by eliminating the need to do needless
    //calculations on nodes already visited. 
    TranspositionTable ttEntry = transpositionTable.get(leaf.node.getBoard().hashCode());
    if (ttEntry != null && ttEntry.depth >= depth){
      if(ttEntry.flag == TranspositionTable.Bound.EXACT){
        //System.out.print("here");
        return new Leaf(leaf.node, ttEntry.value, ttEntry.list);
      } else if(ttEntry.flag == TranspositionTable.Bound.LOWERBOUND)
        alpha = Math.max(alpha, ttEntry.value);
      else if(ttEntry.flag == TranspositionTable.Bound.UPPERBOUND)
        beta = Math.min(beta, ttEntry.value);
      if(alpha >= beta)
        return new Leaf(leaf.node, ttEntry.value, ttEntry.list);
    }
    List<Leaf> child_list = new ArrayList();
    
    if(leaf.node.getBoard().isComplete()){
      if(leaf.node.getBoard().getWinner() == leaf.node.getPlayer())
        return new Leaf(leaf.node, Double.POSITIVE_INFINITY, child_list);
      else
        return new Leaf(leaf.node, Double.NEGATIVE_INFINITY, child_list);
    }
    if (depth == 0) {
      return new Leaf(leaf.node, evaluate.exec(leaf), child_list);
    }
    List<Leaf> best = new ArrayList();
    best.add(leaf);
    double bestValue = Double.NEGATIVE_INFINITY; //placeholder for highest score so far
    // returns value of node in final depth
    if((System.nanoTime() - startTime) > TimeUnit.MILLISECONDS.toNanos(time - buffer)){
        //System.out.println("here");
        return null;
    }   
    //here is where we define the node's children, first it checks to see if the node
    //has children, if not it creates them as needed (barring this is a game ending state etc

    if (leaf.node.getBoard().getCurrentPossibleSquares().isEmpty()) {
      Node n = new Node(leaf.node.getBoard().pass());
      n.setSquare(Square.PASS);
      child_list.add(new Leaf(n, bestValue, new ArrayList()) );
    } else{
      Iterator<Square> it = leaf.node.getBoard().getCurrentPossibleSquares().iterator();
      while (it.hasNext()) {
        Square s = it.next();
        Node n = new Node(leaf.node.play(s));
        n.setSquare(s);
        child_list.add(new Leaf(n, Heuristics.corner_closeness.applyAsDouble(n), new ArrayList()));
      }
    }
    if((System.nanoTime() - startTime) > TimeUnit.MILLISECONDS.toNanos(time - buffer)){
        //System.out.println("here");
        return null;
    }
    //truthfully, this is the meat of the algorithm
    //importantly, after all of the child nodes are evaluated they
    //are sorted
    child_list.sort(Comparator.comparing((Leaf e) -> e.score).reversed());
    Iterator<Leaf> children = child_list.iterator();
    while (children.hasNext()) {
      Leaf child = children.next();
      child.score = -alphaBeta(child, depth - 1, -beta, -alpha, evaluate).score;
      bestValue = Math.max(bestValue, child.score);
      if(bestValue == child.score)
        best.add(0, child);
      alpha = Math.max(alpha, child.score);
      if(alpha >= beta){
        break;
      }
      if((System.nanoTime() - startTime) > TimeUnit.MILLISECONDS.toNanos(time - buffer)){
        //System.out.println("here");
        return null;
      }
    }
    Leaf winner = new Leaf(leaf.node, bestValue, best);
    //Transposition Table store; node is lookup key
    TranspositionTable newEntry = new TranspositionTable(depth, bestValue, best);
    if(bestValue <= alphaOriginal)
      newEntry.flag = TranspositionTable.Bound.UPPERBOUND;
    else if(bestValue >= beta)
      newEntry.flag = TranspositionTable.Bound.LOWERBOUND;
    else
      newEntry.flag = TranspositionTable.Bound.EXACT;
    transpositionTable.put(leaf.node.getBoard().hashCode(), newEntry);
    //System.out.println(table.get(leaf.node));
    if((System.nanoTime() - startTime) > TimeUnit.MILLISECONDS.toNanos(time - buffer)){
        //System.out.println("here");
        return null;
    }
    //return best valued node.
    return winner;
  }
  
//  private Leaf MTDf(Node node, int depth, double f, Evaluator evaluate){
//    double g = f;
//    double upperBound = Double.POSITIVE_INFINITY;
//    double lowerBound = Double.NEGATIVE_INFINITY;
//    double beta;
//    Leaf result = new Leaf(node, 0.0);
//    while(lowerBound < upperBound){
//      if(g == lowerBound)
//        beta = g + 1;
//      else
//        beta = g;
//      result = alphaBeta(node, depth, beta - 1, beta, evaluate);
//      g = result.score;`
//      if(g < beta)
//        upperBound = g;
//      else
//        lowerBound = g;
//    }
//    return result;
//  }
  private Map<Integer, TranspositionTable> transpositionTable;
  private final int infinity = Integer.MAX_VALUE;
  private int currentDepth = 0;
  //private int maxDepth;
  @Override
  public Square chooseSquare(Board board){
    
    Evaluator evaluate;
    evaluate = new Evaluator(Heuristics.ex_wife);
    transpositionTable = new HashMap<>(); // transposition table  
    startTime = System.nanoTime();
    buffer = unit.toMillis(time - (time * (1L/20L)));
    //maxDepth = 2;
    Leaf pretender;
    Leaf root = new Leaf(new Node(board), Double.NEGATIVE_INFINITY, new ArrayList());
    pretender = root;
    for(int maxDepth = 2; maxDepth < 5; maxDepth++) {
      pretender = alphaBeta(root, maxDepth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, evaluate);
      if ((System.nanoTime() - startTime) > TimeUnit.MILLISECONDS.toNanos(time - buffer)) {
        //System.out.println("here");
        return null;
      }
      root = pretender;
    }
    //System.out.println(root.children.get(0).score);
    //System.out.println(root.children.get(1).score);
    //System.out.println(root.children.get(2).score);
    return root.children.get(0).node.getSquare();
  }

  public long time;
  public TimeUnit unit;
  public long startTime;
  public long buffer;
  @Override 
  public void setChooseSquareTimeLimit(long t, TimeUnit u) {
// by default, do nothing
    this.time = t;
    this.unit = u;
  }
  
}