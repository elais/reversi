/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uab.cis.reversi.strategy.group2;

import java.util.ArrayList;
import java.util.List;
import edu.uab.cis.reversi.Board;
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
    //Transposition table
    double alphaOriginal = alpha;
    List<Leaf> child_list = new ArrayList();
    

    //transposition table look up, node is the lookup key
    //this speeds up search a little by eliminating the need to do needless
    //calculations on nodes already visited. 
    TranspositionTable ttEntry = table.get(leaf.node);
    if (ttEntry != null && ttEntry.depth >= depth){
      if(ttEntry.flag == TranspositionTable.Bound.EXACT){
        //System.out.print("here");
        return new Leaf(ttEntry.node, ttEntry.value, leaf.children);
      } else if(ttEntry.flag == TranspositionTable.Bound.LOWERBOUND)
        alpha = Math.max(alpha, ttEntry.value);
      else if(ttEntry.flag == TranspositionTable.Bound.UPPERBOUND)
        beta = Math.min(beta, ttEntry.value);
      if(alpha >= beta)
        return new Leaf(ttEntry.node, ttEntry.value, leaf.children);
    }
    
        
    

    //these are checks put in to place to make sure we have not run out of time
    //or that the search has now reached its max depth
    //on reaching the time limit the search cuts off and calculates the nodes at
    //the given depth
    if(leaf.node.getBoard().isComplete() 
            || (System.nanoTime() - startTime) > TimeUnit.MILLISECONDS.toNanos(time - 100L)){ 
        return new Leaf(leaf.node, evaluate.exec(leaf.node), child_list);
      }
    
    if (depth == 0) {
      return new Leaf(leaf.node, evaluate.exec(leaf.node), child_list);
    }
    
    Leaf best = new Leaf(leaf.node, Double.NEGATIVE_INFINITY, child_list);
    double bestValue = Double.NEGATIVE_INFINITY; //placeholder for highest score so far
    // returns value of node in final depth

    
    //here is where we define the node's children, first it checks to see if the node
    //has children, if not it creates them as needed (barring this is a game ending state etc
    if(leaf.children.isEmpty()){
      if (leaf.node.getBoard().getCurrentPossibleSquares().isEmpty()) {
        Node n = new Node(leaf.node.getBoard().pass());
        n.setSquare(Square.PASS);
        child_list.add(new Leaf(n, bestValue, child_list) );
      } else{
        Iterator<Square> it = leaf.node.getBoard().getCurrentPossibleSquares().iterator();
        while (it.hasNext()) {
          Square s = it.next();
          Node n = new Node(leaf.node.play(s));
          n.setSquare(s);
          child_list.add(new Leaf(n, bestValue, child_list));
        }
      }
    } else {
      child_list = leaf.children;
    }
    
    
    //truthfully, this is the meat of the algorithm
    List<Leaf> tempChildList = new ArrayList(child_list.size());
    Iterator<Leaf> children = child_list.iterator();
    while (children.hasNext()) {
      Leaf child = children.next();
      Leaf alphaT = alphaBeta(child, depth - 1, -beta, -alpha, evaluate);
      bestValue = Math.max(bestValue, -alphaT.score);
      child.score = -alphaT.score;
      tempChildList.add(child);
      if(bestValue == child.score){
        best = new Leaf(child.node, bestValue, child_list);
      }
      alpha = Math.max(alpha, -alphaT.score);
      if(alpha >= beta)
        break;
    }
    if(!tempChildList.isEmpty()){
      tempChildList.sort(Comparator.comparing(e -> e.score));
      child_list = tempChildList;
    }

    
    //Transposition Table store; node is lookup key
    TranspositionTable newEntry = new TranspositionTable(depth, bestValue, best.node);
    if(bestValue <= alphaOriginal)
      newEntry.flag = TranspositionTable.Bound.UPPERBOUND;
    else if(bestValue >= beta)
      newEntry.flag = TranspositionTable.Bound.LOWERBOUND;
    else
      newEntry.flag = TranspositionTable.Bound.EXACT;
    table.put(leaf.node, newEntry);
    //System.out.println(table.get(leaf.node));
    
    //return best valued node.
    return best;
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
//      g = result.score;
//      if(g < beta)
//        upperBound = g;
//      else
//        lowerBound = g;
//    }
//    return result;
//  }
  private Map<Node, TranspositionTable> table;
  private final int infinity = Integer.MAX_VALUE;
  private int currentDepth = 0;
  @Override
  public Square chooseSquare(Board board){
    
    Evaluator evaluate;
    evaluate = new Evaluator(Heuristics.mobility);
    table = new HashMap<>(); // transposition table  
    startTime = System.nanoTime();
    //int count = 0;
    Leaf root = new Leaf(new Node(board), Double.NEGATIVE_INFINITY, new ArrayList());
    for(int i = 0; i <= 10; i++){
      root = alphaBeta(root, i, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, evaluate);
      if(((System.nanoTime() - startTime) > TimeUnit.MILLISECONDS.toNanos(time - 100L)))
        //System.out.println(i);
        break;
    }
    //System.out.println(table.size());
    //System.out.println(root.node.getSquare());
    //System.out.println(currentDepth);
    return root.node.getSquare();
  }

  public long time;
  public TimeUnit unit;
  public long startTime;
  @Override 
  public void setChooseSquareTimeLimit(long t, TimeUnit u) {
// by default, do nothing
    this.time = t;
    this.unit = u;
  }
  
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