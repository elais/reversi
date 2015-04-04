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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToDoubleBiFunction;

/**
 *
 * @author elais
 */
class Heuristics {
  
  // this heuristic measures the player's valid coins vs the opponents valid coins
  static final ToDoubleFunction<Node> coin_parity = (final Node node) -> {
    double coin_parity;
    coin_parity = 100.00 * ((node.getBoard().getPlayerSquareCounts().get(node.getPlayer()).doubleValue()
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
  
  // this heuristic measures the number of valid moves the player has
  static final ToDoubleFunction<Node> valid_moves = (final Node node) -> {
    double valid_moves;
    valid_moves = (double)node.getBoard().getCurrentPossibleSquares().size();
    return valid_moves;
  };
  
  // this heuristic is just dumb, don't use it
  static final ToDoubleFunction<Node> valid_parity = (final Node node) -> {
    return coin_parity.applyAsDouble(node) + valid_moves.applyAsDouble(node);
  };
  
  // this heuristic looks to diminish the number of coins in the beginning, then increase in the mid game
  static final ToDoubleFunction<Node> evaporation = (final Node node) -> {
    if (node.getBoard().getPlayerSquareCounts().size() < 10)
      return node.getBoard().getPlayerSquareCounts().get(node.getOpponent())
              - node.getBoard().getPlayerSquareCounts().get(node.getPlayer());
    else 
      return node.getBoard().getPlayerSquareCounts().get(node.getPlayer())
              - node.getBoard().getPlayerSquareCounts().get(node.getOpponent());
  };
  
  // this heuristic just spits out a random number
  static final ToDoubleFunction<Node> rando = (final Node node) -> {
    return new Random(1337).nextDouble();
  };
  
  //this heuristic measures the player's relative mobility
  static final ToDoubleBiFunction<Node, Node> mobility = (final Node node1, final Node node2) -> {
    int result;
    int playerMoves = node1.getBoard().getCurrentPossibleSquares().size();
    int opponentMoves = node2.getBoard().getCurrentPossibleSquares().size();
    if( (playerMoves + opponentMoves) != 0)
      result = 100 * (playerMoves - opponentMoves) / (playerMoves + opponentMoves);
    else
      result = 0;
    return (double)result;    
  };
}

class Evaluator {

  /**
   * a variable referencing a lambda taking two Integer arguments and returning
   * an Integer:
   */
  private final ToDoubleFunction<Node> strategy;
  private final ToDoubleBiFunction<Node, Node> mobility;

  public Evaluator(final ToDoubleFunction<Node> lambda) {
    strategy = lambda;
    mobility = null;
  }
  
  public Evaluator(final ToDoubleBiFunction<Node, Node> lambda){
    mobility = lambda;
    strategy = null;
  }

  public Double exec(final Node a) {
    return strategy.applyAsDouble(a);
  }
  
  public Double exec(final Node a, final Node b){
    return mobility.applyAsDouble(b, b);
  }
}

public class Group2Strategy implements Strategy{
  //private long startTime;
  
  //This is the alpha beta pruning algorithm
  private Tuple alphaBeta(Node node, int depth, double alpha, double beta, Evaluator evaluate) {
    //Transposition table
    double alphaOriginal = alpha;
    List<Tuple> child_list = new ArrayList();

    //transposition table look up, node is the lookup key
//    TranspositionTable ttEntry = table.get(node);
//    //System.out.println(ttEntry);
//    if (ttEntry != null && ttEntry.depth >= depth){
//      if(ttEntry.flag == TranspositionTable.Bound.EXACT)
//        return new Tuple(ttEntry.node, ttEntry.value);
//      else if(ttEntry.flag == TranspositionTable.Bound.LOWERBOUND)
//        alpha = Math.max(alpha, ttEntry.value);
//      else if(ttEntry.flag == TranspositionTable.Bound.UPPERBOUND)
//        beta = Math.min(beta, ttEntry.value);
//      if(alpha >= beta)
//        return new Tuple(ttEntry.node, ttEntry.value);
//    }
    
        
    
    
    //if((System.currentTimeMillis() - startTime > TimeUnit.MILLISECONDS.toMillis(time - 990L)) 
    if(node.getBoard().isComplete()){
      depth = 0;
      return new Tuple(node, evaluate.exec(node), child_list);
    }
    
    if (depth == 0) {
      Tuple t = new Tuple(node, evaluate.exec(node), child_list);
      return t;
    }
    
    Tuple best = new Tuple(node, Double.NEGATIVE_INFINITY, child_list);
    double bestValue = Double.NEGATIVE_INFINITY; //placeholder for highest score so far
    // returns value of node in final depth

    

    if (node.getBoard().getCurrentPossibleSquares().isEmpty()) {
      Node n = new Node(node.getBoard().pass());
      n.setSquare(Square.PASS);
      child_list.add(new Tuple(n, bestValue, child_list) );
    } else{
      Iterator<Square> it = node.getBoard().getCurrentPossibleSquares().iterator();
      while (it.hasNext()) {
        Square s = it.next();
        Node n = new Node(node.play(s));
        n.setSquare(s);
        child_list.add(new Tuple(n, bestValue, child_list));
      }
    }
    
    
    //truthfully, this is the meat of the algorithm
    List<Tuple> tempChildList = new ArrayList(child_list.size());
    Iterator<Tuple> children = child_list.iterator();
    while (children.hasNext()) {
      Tuple child = children.next();
      Tuple alphaT = alphaBeta(child.node, depth - 1, -beta, -alpha, evaluate);
      bestValue = Math.max(bestValue, -alphaT.score);
      child.score = -alphaT.score;
      tempChildList.add(child);
      if(bestValue == child.score){
        best = new Tuple(child.node, bestValue, child.children);
      }
      alpha = Math.max(alpha, -alphaT.score);
      if(alpha >= beta)
        break;
    }
    if(!tempChildList.isEmpty()){
      child_list = tempChildList;
    }
    
    // Transposition Table store; node is lookup key
//    TranspositionTable newEntry = new TranspositionTable(depth, bestValue, best.node);
//    if(bestValue <= alphaOriginal)
//      newEntry.flag = TranspositionTable.Bound.UPPERBOUND;
//    else if(bestValue >= beta)
//      newEntry.flag = TranspositionTable.Bound.LOWERBOUND;
//    else
//      newEntry.flag = TranspositionTable.Bound.EXACT;
//    table.put(node, newEntry);
    //System.out.println(table.get(node.hashCode()));
    
    //return best valued node.
    return best;
  }
  
//  private Tuple MTDf(Node node, int depth, double f, Evaluator evaluate){
//    double g = f;
//    double upperBound = Double.POSITIVE_INFINITY;
//    double lowerBound = Double.NEGATIVE_INFINITY;
//    double beta;
//    Tuple result = new Tuple(node, 0.0);
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
  @Override
  public Square chooseSquare(Board board) {
    
    //The reversi overlord calls this function
    Evaluator evaluate = new Evaluator(Heuristics.mobility);
    table = new ConcurrentHashMap<>(); // transpoisition table  
    startTime = System.currentTimeMillis();
    Tuple t = alphaBeta(new Node(board), 2, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, evaluate);    
    //System.out.println(t.node.getSquare());
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