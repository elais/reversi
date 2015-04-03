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
    double valid_moves;
    valid_moves = node.getBoard().getCurrentPossibleSquares().size();
    return valid_moves;
  };
  
  static final ToDoubleFunction<Node> valid_parity = (final Node node) -> {
    double valid_parity;
    return coin_parity.applyAsDouble(node) + valid_moves.applyAsDouble(node);
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
  private Tuple alphaBeta(FiveTuple input) {
    
    
    // extract algorithm inputs from input tuple
    Node best = input.node;
    Node node = input.node;
    int depth = input.depth;
    double alpha = input.alpha;
    double beta = input.beta;
    Evaluator evaluate = input.evaluate;
    
    double bestResult = Double.NEGATIVE_INFINITY; //placeholder for highest score so far
    //if(System.nanoTime()  time - 100)
    
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
      FiveTuple newInput; 
      newInput = new FiveTuple(child, depth - 1, -beta, 
              -alpha, evaluate);
      Tuple alphaT = alphaBeta(newInput);
      alpha = -alphaT.score;
      if(beta <= alpha) {
        //System.out.println(beta);
        return alphaT;
      }
      if(alpha > bestResult){
        //System.out.println("here");
        bestResult = alpha;
        best = child;
      }
    }
    //System.out.println(best.getSquare());
    Tuple t = new Tuple(best, bestResult);
    return t;
  }
  

  @Override
  public Square chooseSquare(Board board) {
    
    Evaluator evaluate = new Evaluator(FunctionalUtils.valid_parity); //choose an evaluation function
    
    //input tuple for algorithm, in a tuple for later hashing; values are immutable.
    FiveTuple input = new FiveTuple(new Node(board), 4, Double.NEGATIVE_INFINITY, 
            Double.POSITIVE_INFINITY, evaluate);
    Square s = alphaBeta(input).node.getSquare(); //gets square from algorithm
    return s;
  }
  public long time;
  public TimeUnit unit;
  @Override
  public void setChooseSquareTimeLimit(long time, TimeUnit unit) {
// by default, do nothing
    this.time = 1000;
    this.unit = TimeUnit.MILLISECONDS;
  }
  
  private static final Map<FiveTuple,Tuple> memo = new HashMap<>();
    
  
  
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