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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 *
 * @author elais
 */
class FunctionalUtils {

  static final ToDoubleFunction<Node> coin_parity = (final Node node) -> {
    double coin_parity;
    coin_parity = ((node.getBoard().getPlayerSquareCounts().get(node.getPlayer()).doubleValue()
            - node.getBoard().getPlayerSquareCounts().get(node.getOpponent()).doubleValue())
            / (node.getBoard().getPlayerSquareCounts().get(node.getPlayer()).doubleValue()
            + node.getBoard().getPlayerSquareCounts().get(node.getOpponent()).doubleValue()));
    //System.out.println(coin_parity);
    return coin_parity;
  };
  
  /**
   * Drew, make sure your evaluation function is encapsulated in a function just
   * like this one. All of the relevant data you need should be in the node.
   */
  static final ToDoubleFunction<Node> start_here = (final Node node) -> {
    double eval = 0;
    return eval;
  };
  
  static final ToDoubleFunction<Node> valid_moves = (final Node node) -> {
    double valid_moves1;
    valid_moves1 = node.getBoard().getCurrentPossibleSquares().size();
    return valid_moves1;
  };
  
  static final ToDoubleFunction<Node> valid_parity = (final Node node) -> {
    return coin_parity.applyAsDouble(node) + valid_moves.applyAsDouble(node);
  };
  
  static final ToDoubleFunction<Node> evaporation = (final Node node) -> {
    if (node.getBoard().getPlayerSquareCounts().size() < 20)
      return (double)node.getBoard().getPlayerSquareCounts().get(node.getOpponent())
              - node.getBoard().getPlayerSquareCounts().get(node.getPlayer());
    else 
      return (double)node.getBoard().getPlayerSquareCounts().get(node.getPlayer())
              - node.getBoard().getPlayerSquareCounts().get(node.getOpponent());
  };
}

class Evaluator {

  /**
   * a variable referencing a lambda taking two Integer arguments and returning
   * an Integer:
   */
  private final ToDoubleFunction<Node> strategy;

  public Evaluator(final ToDoubleFunction<Node> lambda) {
    strategy = lambda;
  }

  public Double exec(final Node a) {
    return strategy.applyAsDouble(a);
  }
}

public class Group2Strategy implements Strategy{
  //private long startTime;
  
  //This is the alpha beta pruning algorithm
  private Tuple alphaBeta(Node node, int depth, double alpha, double beta, Evaluator evaluate) {
    //Transposition talbe
    double alphaOriginal = alpha;
    
    //transposition table look up, node is the lookup key
    TranspositionTable ttEntry = table.getOrDefault(node, null);
    if (ttEntry != null && ttEntry.depth >= depth){
      if(ttEntry.flag == ttEntry.flag.EXACT)
        return new Tuple(ttEntry.node, ttEntry.value);
      else if(ttEntry.flag == ttEntry.flag.LOWERBOUND)
        alpha = Math.max(alpha, ttEntry.value);
      else if(ttEntry.flag == ttEntry.flag.UPPERBOUND)
        beta = Math.min(beta, ttEntry.value);
      if(alpha >= beta)
        return new Tuple(ttEntry.node, ttEntry.value);
    }
    
        
    
    
    if((System.currentTimeMillis() - startTime > TimeUnit.MILLISECONDS.toMillis(time - (time - 1L))) 
            || node.getBoard().isComplete()){
      depth = 0;
      if(node.getBoard().isComplete())
        return new Tuple(node, evaluate.exec(node) * 100);
    }
    // extract algorithm inputs from input tuple
    Node best = node;
    double bestValue = Double.NEGATIVE_INFINITY; //placeholder for highest score so far
    // returns value of node in final depth
    if (depth == 0) {
      Tuple t = new Tuple(node, evaluate.exec(node));
      return t;
    }
    
    List<Node> child_list = new ArrayList();

    if (node.getBoard().getCurrentPossibleSquares().isEmpty()) {
      Node n = new Node(node.getBoard().pass());
      n.setSquare(Square.PASS);
      child_list.add(n);
    } else{
      Iterator<Square> it = node.getBoard().getCurrentPossibleSquares().iterator();
      while (it.hasNext()) {
        Square s = it.next();
        Node n = new Node(node.getBoard().play(s));
        n.setSquare(s);
        child_list.add(n);
      }
    }

    Iterator<Node> children = child_list.iterator();
    while (children.hasNext()) {
      Node child = children.next();
      Tuple alphaT = alphaBeta(child, depth - 1, -beta, -alpha, evaluate);
      bestValue = Math.max(bestValue, -alphaT.score);
      if(bestValue == -alphaT.score)
        best = child;
      alpha = Math.max(alpha, -alphaT.score);
      if(alpha >= beta)
        break;
    }
    
    // Transposition Table store; node is lookup key
    TranspositionTable newEntry = new TranspositionTable(best, depth, bestValue);
    if(bestValue <= alphaOriginal)
      newEntry.flag = newEntry.flag.UPPERBOUND;
    else if(bestValue >= beta)
      newEntry.flag = newEntry.flag.LOWERBOUND;
    else
      newEntry.flag = newEntry.flag.EXACT;
    table.put(node, newEntry);
    //System.out.println(best.getSquare());
    Tuple t = new Tuple(best, bestValue);
    return t;
  }
  
  private Map<Node, TranspositionTable> table;
  @Override
  public Square chooseSquare(Board board) {
    
    //The reversi overlord calls this function
    Evaluator evaluate = new Evaluator(FunctionalUtils.valid_moves);
    table = new ConcurrentHashMap<>(); // transpoisition table
    startTime = System.currentTimeMillis();
    Tuple t; 
    t = alphaBeta(new Node(board), 19, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, evaluate);
    //System.out.println(table.size());
    return t.node.getSquare();
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