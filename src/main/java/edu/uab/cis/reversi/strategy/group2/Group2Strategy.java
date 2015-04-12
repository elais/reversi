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

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 *
 * @author elais
 */
public class Group2Strategy implements Strategy {
  //private long startTime;

  //This is the alpha beta pruning algorithm
  private Leaf negaMax(Leaf leaf, int depth, double alpha, double beta, Evaluator evaluate) throws TimeLapsedException {
    //these are checks put in to place to make sure we have not run out of time
    //or that the search has now reached its max depth
    //on reaching the time limit the search cuts off and calculates the nodes at
    //the given depth
    if (timeOut) {
      throw new TimeLapsedException("Time Lapsed");
    }
    //Transposition table
    double alphaOriginal = alpha;

    //transposition table look up, node is the lookup key
    //this speeds up search a little by eliminating the need to do needless
    //calculations on nodes already visited. 
    TranspositionTable ttEntry = transpositionTable.get(leaf.node.getBoard().hashCode());
    if (ttEntry != null && ttEntry.depth >= depth) {
      if (ttEntry.flag == TranspositionTable.Bound.EXACT) {
        //System.out.print("here");
        return new Leaf(leaf.node, ttEntry.value, ttEntry.children);
      } else if (ttEntry.flag == TranspositionTable.Bound.LOWERBOUND) {
        alpha = Math.max(alpha, ttEntry.value);
      } else if (ttEntry.flag == TranspositionTable.Bound.UPPERBOUND) {
        beta = Math.min(beta, ttEntry.value);
      }
      if (alpha >= beta) {
        return new Leaf(leaf.node, ttEntry.value, ttEntry.children);
      }
    }
    if (timeOut) {
      throw new TimeLapsedException("Time Lapsed");
    }

    List<Leaf> child_list = new ArrayList<Leaf>();
    if (leaf.node.getBoard().isComplete()) {
      if (leaf.node.getBoard().getWinner() == leaf.node.getPlayer()) {
        return new Leaf(leaf.node, Double.POSITIVE_INFINITY, new ArrayList<Leaf>());
      } else {
        return new Leaf(leaf.node, Double.NEGATIVE_INFINITY, new ArrayList<Leaf>());
      }
    }

    if (depth == 0) {
      return new Leaf(leaf.node, evaluate.exec(leaf), new ArrayList<Leaf>());
    }
    Leaf killer;
    double bestValue = Double.NEGATIVE_INFINITY; //placeholder for highest score so far
    // returns value of node in final depth

    //here is where we define the node's children, first it checks to see if the node
    //has children, if not it creates them as needed (barring this is a game ending state etc
    if(!leaf.children.isEmpty()){
      child_list = leaf.children;
    } else {
      if (leaf.node.getBoard().getCurrentPossibleSquares().isEmpty()) {
        Node n = new Node(leaf.node.getBoard().pass());
        n.setSquare(Square.PASS);
        return new Leaf(n, bestValue, new ArrayList<Leaf>());
      } else {
        for (Square s : leaf.node.getBoard().getCurrentPossibleSquares()) {
          Node n = new Node(leaf.node.play(s));
          n.setSquare(s);
          Leaf child = new Leaf(n, Heuristics.ex_wife.applyAsDouble(n), new ArrayList<Leaf>());
          child_list.add(child);
        }
        child_list.sort(Comparator.comparing((Leaf e) -> e.score));
      }
    }

    if (timeOut) {
      throw new TimeLapsedException("Time Lapsed");
    }
    //truthfully, this is the meat of the algorithm
    for (Leaf child : child_list) {
      child = negaMax(child, depth - 1, -beta, -alpha, evaluate);
      child.score = -child.score;
      if (timeOut) {
        throw new TimeLapsedException("Time Lapsed");
      }
      bestValue = Math.max(bestValue, child.score);
      alpha = Math.max(alpha, child.score);
      if (alpha >= beta) {
        break;
      }
    }
    if (timeOut) {
      throw new TimeLapsedException("Time Lapsed");
    }
    child_list.sort(Comparator.comparing((Leaf e) -> e.score));
    Leaf winner = new Leaf(leaf.node, bestValue, child_list);
    //Transposition Table store; node is lookup key
    TranspositionTable newEntry = new TranspositionTable(depth, bestValue, child_list);
    if (bestValue <= alphaOriginal) {
      newEntry.flag = TranspositionTable.Bound.UPPERBOUND;
    } else if (bestValue >= beta) {
      newEntry.flag = TranspositionTable.Bound.LOWERBOUND;
    } else {
      newEntry.flag = TranspositionTable.Bound.EXACT;
    }
    transpositionTable.put(leaf.node.getBoard().hashCode(), newEntry);
    //return best valued node.
    return winner;
  }

  private Map<Integer, TranspositionTable> transpositionTable;
  private Map<Integer, Map<Player, Leaf>> killers;
  private volatile Leaf root;
  private volatile boolean timeOut;
  private final int maxDepth = 12;

  @Override
  public Square chooseSquare(Board board) {
    ExecutorService service;
    service = Executors.newSingleThreadExecutor();
    root = new Leaf(new Node(board), Double.NEGATIVE_INFINITY, new ArrayList<Leaf>());
    transpositionTable = new ConcurrentHashMap<Integer, TranspositionTable>();
    killers = new ConcurrentHashMap<Integer, Map<Player, Leaf>>();
    IterativeDeepening iddfs = new IterativeDeepening();
    try {
      service.submit(iddfs);
      boolean isFinished = service.awaitTermination(time * 4/5, unit);
      if(!isFinished){
        iddfs.done();
        root = iddfs.getResult();
        service.shutdown();
        //System.out.println(root.children.get(0).node.getSquare());
        return root.children.get(0).node.getSquare();
      }
    } catch (Exception ex) {
      System.out.println("Cought Exception");
      ex.printStackTrace();
    }
    root = iddfs.getResult();
    return root.children.get(0).node.getSquare();

  }

  class TimeLapsedException extends Exception {

    public TimeLapsedException(String message) {
      super(message);
    }

  }

  class IterativeDeepening implements Runnable {

    private Leaf result;
    private final Evaluator evaluate = new Evaluator(Heuristics.ex_wife);
    @Override
    public synchronized void run() {
      Leaf pretender;
      timeOut = false;
      int startDepth = 2;
      while (!timeOut && startDepth <= maxDepth) {
        try {
          pretender = negaMax(root, startDepth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, evaluate);
          if (pretender != null) {
            result = pretender;
          }
          startDepth += 1;
        } catch (Exception e) {
          System.out.println("Final Depth: " + startDepth);
          //e.printStackTrace();
        }
      }
    }

    public synchronized Leaf getResult() {
      return result;
    }

    public void done() {
      timeOut = true;
    }
  }
  
  private TimeUnit unit;
  public long time;

  @Override
  public void setChooseSquareTimeLimit(long t, TimeUnit u) {
    unit = u;
    time = t;
  }

}
