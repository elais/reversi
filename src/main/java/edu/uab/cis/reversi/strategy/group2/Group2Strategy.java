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
import java.util.function.ToDoubleFunction;
import edu.uab.cis.reversi.Board;
import java.util.Comparator;
import edu.uab.cis.reversi.Move;
import edu.uab.cis.reversi.strategy.group2.*;
import edu.uab.cis.reversi.Player;
import java.util.PriorityQueue;
import edu.uab.cis.reversi.Square;
import edu.uab.cis.reversi.Strategy;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
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
    coin_parity = 100 * ((node.getBoard().getPlayerSquareCounts().get(node.getPlayer()).doubleValue()
            - node.getBoard().getPlayerSquareCounts().get(node.getOpponent()).doubleValue())
            / (node.getBoard().getPlayerSquareCounts().get(node.getPlayer()).doubleValue()
            + node.getBoard().getPlayerSquareCounts().get(node.getOpponent()).doubleValue()));
    //System.out.println(coin_parity);
    return coin_parity;
  };

  static final ToDoubleFunction<Node> start_here = (final Node node) -> {
    double eval = 0;
    return eval;
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

public class Group2Strategy implements Strategy {

  private Tuple alphabeta(Node node, int depth, double alpha, double beta, Evaluator evaluate) {
    
    Node best = node; // placeholder for highest scoring child node
    double bestResult = Double.NEGATIVE_INFINITY; //initialize alpha
    
    List<Node> child_list = new ArrayList();
    if (depth == 0) {
      
      Tuple t = new Tuple(node, evaluate.exec(node));
      return t;
    }

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
      Tuple alphaT = alphabeta(child, depth - 1, -beta, -alpha, evaluate);
      alpha = - alphaT.score;
      if(beta <= alpha) {
        Tuple alpha2 = new Tuple(alphaT.node, alpha);
        return alpha2;
      }
      if(alpha > bestResult){
        //System.out.println("here");
        bestResult = alpha;
        best = child;
      }
    }
    //System.out.println(best.getSquare());
    Tuple carson = new Tuple(best, bestResult);
    return carson;
  }
  
  @Override
  public Square chooseSquare(Board board) {
    Evaluator evaluate;
    evaluate = new Evaluator(FunctionalUtils.coin_parity);
    Square s = alphabeta(new Node(board), 4, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, evaluate).node.getSquare();
    return s;
  }

  @Override
  public void setChooseSquareTimeLimit(long time, TimeUnit unit) {
// by default, do nothing
    time = 1000;
    unit = TimeUnit.MILLISECONDS;
  }

}



final class Memoizer<T, U> {

  private final Map<T, U> cache = new ConcurrentHashMap<>();

  private Memoizer() {
  }

  private Function<T, U> doMemoize(final Function<T, U> function) {
    return input -> cache.computeIfAbsent(input, function::apply);
  }

  public static <T, U> Function<T, U> memoize(final Function<T, U> function) {
    return new Memoizer<T, U>().doMemoize(function);
  }
}