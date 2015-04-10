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
  public Leaf alphaBeta(Leaf leaf, int depth, double alpha, double beta, Evaluator evaluate, boolean timeOut) throws TimeLapsedException {
    //these are checks put in to place to make sure we have not run out of time
    //or that the search has now reached its max depth
    //on reaching the time limit the search cuts off and calculates the nodes at
    //the given depth
    if (timeOut) {
      throw new TimeLapsedException("Time Lapsed");
    }
//    Leaf killer = null;
//    if(leaf.node.player == Player.BLACK){
//      if(killers.get(depth).get(0) != null)
//        killer = killers.get(depth).get(0);
//    } else if(leaf.node.player == Player.WHITE){
//      if(killers.get(depth).get(1) != null)
//        killer = killers.get(depth).get(1);
//    }
      
    //Transposition table
    double alphaOriginal = alpha;

    //transposition table look up, node is the lookup key
    //this speeds up search a little by eliminating the need to do needless
    //calculations on nodes already visited. 
    TranspositionTable ttEntry = transpositionTable.get(leaf.node.getBoard().hashCode());
    if (ttEntry != null && ttEntry.depth >= depth) {
      if (ttEntry.flag == TranspositionTable.Bound.EXACT) {
        //System.out.print("here");
        return new Leaf(leaf.node, ttEntry.value, ttEntry.list);
      } else if (ttEntry.flag == TranspositionTable.Bound.LOWERBOUND) {
        alpha = Math.max(alpha, ttEntry.value);
      } else if (ttEntry.flag == TranspositionTable.Bound.UPPERBOUND) {
        beta = Math.min(beta, ttEntry.value);
      }
      if (alpha >= beta) {
        return new Leaf(leaf.node, ttEntry.value, ttEntry.list);
      }
    }

    List<Leaf> child_list = new ArrayList();
    if (leaf.node.getBoard().isComplete()) {
      if (leaf.node.getBoard().getWinner() == leaf.node.getPlayer()) {
        return new Leaf(leaf.node, Double.POSITIVE_INFINITY, child_list);
      } else {
        return new Leaf(leaf.node, Double.NEGATIVE_INFINITY, child_list);
      }
    }

    if (depth == 0) {
      return new Leaf(leaf.node, evaluate.exec(leaf), child_list);
    }
    List<Leaf> best = new ArrayList();
    best.add(leaf);
    double bestValue = Double.NEGATIVE_INFINITY; //placeholder for highest score so far
    // returns value of node in final depth

    //here is where we define the node's children, first it checks to see if the node
    //has children, if not it creates them as needed (barring this is a game ending state etc
    //System.out.println("here");
    if (leaf.node.getBoard().getCurrentPossibleSquares().isEmpty()) {
      Node n = new Node(leaf.node.getBoard().pass());
      n.setSquare(Square.PASS);
      child_list.add(new Leaf(n, bestValue, child_list));
    } else {
      Iterator<Square> it = leaf.node.getBoard().getCurrentPossibleSquares().iterator();
      while (it.hasNext()) {
        Square s = it.next();
        Node n = new Node(leaf.node.play(s));
        n.setSquare(s);
        Leaf child = new Leaf(n, Heuristics.ex_wife.applyAsDouble(n), child_list);
        child_list.add(child);
      }
      child_list.sort(Comparator.comparing((Leaf e) -> e.score));
    }
    if (timeOut) {
      throw new TimeLapsedException("Time Lapsed");
    }
    //truthfully, this is the meat of the algorithm
    //importantly, after all of the child nodes are evaluated they
    //are sorted
    for (Leaf child : child_list) {
      child.score = -alphaBeta(child, depth - 1, -beta, -alpha, evaluate, timeOut).score;
      if (timeOut) {
        throw new TimeLapsedException("Time Lapsed");
      }
      bestValue = Math.max(bestValue, child.score);
      if (bestValue == child.score) {
        best.add(0, child);
      }
      alpha = Math.max(alpha, child.score);
      if (alpha >= beta) {
        break;
      }
    }

    Leaf winner = new Leaf(leaf.node, bestValue, best);
    //Transposition Table store; node is lookup key
    TranspositionTable newEntry = new TranspositionTable(depth, bestValue, best);
    if (bestValue <= alphaOriginal) {
      newEntry.flag = TranspositionTable.Bound.UPPERBOUND;
    } else if (bestValue >= beta) {
      newEntry.flag = TranspositionTable.Bound.LOWERBOUND;
    } else {
      newEntry.flag = TranspositionTable.Bound.EXACT;
    }
    transpositionTable.put(leaf.node.getBoard().hashCode(), newEntry);
    //System.out.println(table.get(leaf.node));
    //return best valued node.
    return winner;
  }

  private Map<Integer, TranspositionTable> transpositionTable;
  private Map<Integer, ArrayList<Leaf>> killers;
  private Leaf root;
  private volatile boolean timeOut;
  private final int maxDepth = 8;
  @Override
  public Square chooseSquare(Board board) {
    timeOut = false;
    root = new Leaf(new Node(board), Double.NEGATIVE_INFINITY, new ArrayList());
    transpositionTable = new ConcurrentHashMap();
    killers = new ConcurrentHashMap();
    ExecutorService service = Executors.newCachedThreadPool();
    IterativeDeepening iddfs = new IterativeDeepening();
    service.submit(iddfs);
    try {
      boolean isFinished = service.awaitTermination(time * 1/5, unit);
      if (!isFinished) {
        iddfs.done();
        root = iddfs.getResult();
      }
      service.shutdown();
    } catch (Exception e) {
      System.out.println("Thread Terminated");
    }
    if (root != null) {
      return root.children.get(0).node.getSquare();
    } else {
      System.out.println("root was null");
      return board.getCurrentPossibleSquares().iterator().next();
    }
  }

  class TimeLapsedException extends Exception {

    public TimeLapsedException(String message) {
      super(message);
    }

  }

  class IterativeDeepening implements Runnable {
    private Leaf result;
    private Evaluator evaluate = new Evaluator(Heuristics.ex_wife);
    @Override
    public synchronized void run() {
      Leaf pretender;
      int idDepth = 1;
      while (!timeOut && (idDepth <= maxDepth)) {
        try {
          if ((idDepth <= Integer.MAX_VALUE) && !timeOut) {
            pretender = alphaBeta(root, idDepth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, evaluate, timeOut);
            result = pretender;
            //System.out.println("Searched to depth: " + idDepth);       
            idDepth += 1;
          } else {
            break;
          }
        } catch (Exception e) {
          System.out.println("exception in run");
          e.printStackTrace();
          System.out.println("Depth was " + idDepth);
        }
      }
    }

    public synchronized Leaf getResult() {
      if (result != null) {
        return result;
      } else {
        return root;
      }
    }

    public void done() {
      timeOut = true;
    }


  }
  private TimeUnit unit;
  public long time;
  public long startTime = System.nanoTime();
  public long buffer;

  @Override
  public synchronized void setChooseSquareTimeLimit(long t, TimeUnit u) {
    unit = u;
    time = t;
  }

}
