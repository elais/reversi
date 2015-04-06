/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uab.cis.reversi.strategy.group2;

import edu.uab.cis.reversi.Square;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import java.util.function.ToDoubleBiFunction;
import edu.uab.cis.reversi.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 * @author elais
 */
public class Heuristics {
  final static int pointTable[][] = new int [8][8];

  final static int CORNER = 20;
  final static int DIAGONAL = -7;
  final static int SECOND = -3;
  final static int THIRD = 11;
  final static int FOURTH = 8;
  final static int COMMON = -4;
  final static int STARTER = -3;

  // Weight values used to determine priorities when examining potential moves
  final static double POSITIONWEIGHT = 5;
  final static double MOBILITYWEIGHT = 15;
  final static double ENDWEIGHT = 300;

  //Set values for the top left quadrant
  static{
    pointTable[0][0] = CORNER;
    pointTable[1][1] = DIAGONAL;
    pointTable[0][1] = pointTable[1][2] = SECOND;
    pointTable[0][2] = pointTable[2][0] = THIRD;
    pointTable[0][3] = pointTable[1][0] = FOURTH;
    pointTable[0][2] = pointTable[1][2] = pointTable[2][1] = pointTable[2][2]
            = pointTable[2][3] = pointTable[3][1] = pointTable[3][2] = COMMON;
    pointTable[3][3] = STARTER;
    // Duplicate values for top right quadrant

    for (int i = 4; i < 8; i++) {
      for (int j = 0; j < 4; j++) {
        pointTable[j][i] = pointTable[(j)][7 - i];
      }
    }
    // Duplicate values for bottom two quadrants
    for (int i = 0; i < 8; i++) {
      for (int j = 4; j < 8; j++) {
        pointTable[j][i] = pointTable[7 - j][(i)];
      }
    }
  }
  
  static final ToDoubleFunction<Node> motherfucker = new ToDoubleFunction<Node>() {

    public double applyAsDouble(final Node a) {
      double score = 0;
      score = frontiers.applyAsDouble(a)
              + corner_closeness.applyAsDouble(a) 
              + corner_occupancy.applyAsDouble(a) 
              + mobility.applyAsDouble(a);
      return score;
    }
  };
  
  static final ToDoubleFunction<Node> frontiers = (final Node a) -> {
    int my_tiles = 0;
    int opp_tiles = 0;
    int my_front_tiles = 0;
    int opp_front_tiles = 0;
    int X1[] = {-1, -1, 0, 1, 1, 1, 0, -1};
    int Y1[] = {0, 1, 1, 1, 0, -1, -1, -1};
    double d = 0;
    double p = 0;
    double f = 0;
    for (int i = 0; i < 8; i++)
      for (int j = 0; j < 8; j++) {
        if (a.squareOwners.get(new Square(i,j)) == a.player) {
          d += pointTable[i][j];
          my_tiles++;
        } else if (a.squareOwners.get(new Square(i,j)) == a.opponent) {
          d -= pointTable[i][j];
          opp_tiles++;
        }
        if (a.squareOwners.get(new Square(i,j)) != null) {
          for (int k = 0; k < 8; k++) {
            int x = i + X1[k];
            int y = j + Y1[k];
            if (x >= 0 && x < 8 && y >= 0 && y < 8 && a.squareOwners.get(new Square(x,y)) == null) {
              if (a.squareOwners.get(new Square(i,j)) == a.player) {
                my_front_tiles++;
              } else {
                opp_front_tiles++;
              }
              break;
            }
          }
        }
      }
    if (my_tiles > opp_tiles) {
      p = (100.0 * my_tiles) / (my_tiles + opp_tiles);
    } else if (my_tiles < opp_tiles) {
      p = -(100.0 * opp_tiles) / (my_tiles + opp_tiles);
    } else {
      p = 0;
    }
    if (my_front_tiles > opp_front_tiles) {
      f = -(100.0 * my_front_tiles) / (my_front_tiles + opp_front_tiles);
    } else if (my_front_tiles < opp_front_tiles) {
      f = (100.0 * opp_front_tiles) / (my_front_tiles + opp_front_tiles);
    } else {
      f = 0;
    }
    return (10 * p) + (74.396 * f) + (10 * d);
  };
  
    
  
  static final ToDoubleFunction<Node> corner_closeness = (final Node a) -> {
    Map<Square, Player> squareOwners = a.squareOwners;
    // Corner closeness
    int my_tiles = 0;
    int opp_tiles = 0;
    if (squareOwners.get(new Square(0, 0)) == null) {
      if (squareOwners.get(new Square(0, 1)) == a.player) {
        my_tiles+=1;
      } else if (squareOwners.get(new Square(0, 1)) == a.opponent) {
        opp_tiles+=1;
      }
      if (squareOwners.get(new Square(1, 1)) == a.player) {
        my_tiles+=1;
      } else if (squareOwners.get(new Square(1, 1)) == a.opponent) {
        opp_tiles+=1;
      }
      if (squareOwners.get(new Square(1, 0)) == a.player) {
        my_tiles+=1;
      } else if (squareOwners.get(new Square(1, 0)) == a.opponent) {
        opp_tiles+=1;
      }
    }
    if (squareOwners.get(new Square(0, 7)) == null) {
      if (squareOwners.get(new Square(0, 6)) == a.player) {
        my_tiles+=1;
      } else if (squareOwners.get(new Square(0, 6)) == a.opponent) {
        opp_tiles+=1;
      }
      if (squareOwners.get(new Square(1, 6)) == a.player) {
        my_tiles+=1;
      } else if (squareOwners.get(new Square(1, 6)) == a.opponent) {
        opp_tiles+=1;
      }
      if (squareOwners.get(new Square(1, 7)) == a.player) {
        my_tiles+=1;
      } else if (squareOwners.get(new Square(1, 7)) == a.opponent) {
        opp_tiles+=1;
      }
    }
    if (squareOwners.get(new Square(7, 0)) == null) {
      if (squareOwners.get(new Square(7, 1)) == a.player) {
        my_tiles+=1;
      } else if (squareOwners.get(new Square(7, 1)) == a.opponent) {
        opp_tiles+=1;
      }
      if (squareOwners.get(new Square(6, 1)) == a.player) {
        my_tiles+=1;
      } else if (squareOwners.get(new Square(6, 1)) == a.opponent) {
        opp_tiles+=1;
      }
      if (squareOwners.get(new Square(6, 0)) == a.player) {
        my_tiles+=1;
      } else if (squareOwners.get(new Square(6, 0)) == a.opponent) {
        opp_tiles+=1;
      }
    }
    if (squareOwners.get(new Square(7, 7)) == null) {
      if (squareOwners.get(new Square(6, 7)) == a.player) {
        my_tiles+=1;
      } else if (squareOwners.get(new Square(6, 7)) == a.opponent) {
        opp_tiles+=1;
      }
      if (squareOwners.get(new Square(6, 6)) == a.player) {
        my_tiles+=1;
      } else if (squareOwners.get(new Square(6, 6)) == a.opponent) {
        opp_tiles+=1;
      }
      if (squareOwners.get(new Square(7, 6)) == a.player) {
        my_tiles+=1;
      } else if (squareOwners.get(new Square(7, 6)) == a.opponent) {
        opp_tiles+=1;
      }
    }
    return -12.5 * (my_tiles - opp_tiles);

  };
          
  
  // Credits for values go to: http://www.site-constructor.com/othello/Present/BoardLocationValue.html
  static final ToDoubleFunction<Node> position = (final Node a) -> {
    int row = a.row;
    int column = a.column;
    //System.out.print(row);
    return pointTable[row][column];
  };
  
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
  
  static final ToDoubleFunction<Node> corner_occupancy = (final Node a) -> {
    Map<Square, Player> squareOwners = a.squareOwners;

    int my_tiles = 0; 
    int opp_tiles = 0;
    if(squareOwners.get(new Square(0,0)) == a.player) 
      my_tiles+=1;
    else if(squareOwners.get(new Square(0,0)) == a.opponent) 
      opp_tiles+=1;
    if(squareOwners.get(new Square(0,7)) == a.player) 
      my_tiles+=1;
    else if(squareOwners.get(new Square(0,7)) == a.opponent) 
      opp_tiles+=1;
    if(squareOwners.get(new Square(7,0)) == a.player) 
      my_tiles+=1;
    else if(squareOwners.get(new Square(7,0)) == a.opponent) 
      opp_tiles+=1;
    if(squareOwners.get(new Square(7,7)) == a.player) 
      my_tiles+=1;
    else if(squareOwners.get(new Square(7,7)) == a.opponent) 
      opp_tiles+=1;
    
    return 25 * (my_tiles - opp_tiles) * 801.274;
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
    return 100 * coin_parity.applyAsDouble(node) + valid_moves.applyAsDouble(node);
  };
  
  // this heuristic looks to diminish the number of coins in the beginning, then increase in the mid game
  static final ToDoubleFunction<Node> evaporation = (final Node node) -> {
    if (node.getBoard().getPlayerSquareCounts().size() < 40)
      return node.getBoard().getPlayerSquareCounts().get(node.getOpponent())
              - node.getBoard().getPlayerSquareCounts().get(node.getPlayer());
    else 
      return node.getBoard().getPlayerSquareCounts().get(node.getPlayer())
              - node.getBoard().getPlayerSquareCounts().get(node.getOpponent()) + position.applyAsDouble(node);
  };
  
  // this heuristic just spits out a random number
  static final ToDoubleFunction<Node> rando = (final Node node) -> {
    return new Random(1337).nextDouble();
  };

    
  //this heuristic measures the player's relative mobility, when using this
  //be sure to change the node calculated at depth zero to two nodes;
  static final ToDoubleFunction<Node> mobility = (final Node node) -> {
    int result;
    int playerMoves = node.getBoard().getCurrentPossibleSquares().size();
    int opponentMoves = node.opponentSquares.size();
    if(playerMoves > opponentMoves)
      result = ((100 * playerMoves) - opponentMoves) / (playerMoves + opponentMoves);
    else if(opponentMoves < playerMoves)
      result = -((100 * opponentMoves) - playerMoves) / (playerMoves + opponentMoves);
    else
      result = 0;
    return result * 78.922;    
  };
  

  
}

class Evaluator {

  /**
   * a variable referencing a lambda taking two Integer arguments and returning
   * an Integer:
   */
  private final ToDoubleFunction<Node> strategy;
  public Evaluator(ToDoubleFunction<Node> lambda) {
    strategy = lambda;

  }

  Function<Leaf, Double> f = this::exec;
  Function<Leaf, Double> g = Memoizer.memoize(f);  
  public Double exec(final Leaf a) {
      if(a.node.s != Square.PASS || a.node.s != null){
          return strategy.applyAsDouble(a.node);
      }
      else
        return 0.0;
    } 
  
  private Queue<Node> children;
  private Iterator<Square> it;
  private Node opp;
  public Node mobility(final Node a) {
    Queue<Node> children = new PriorityQueue<>(Comparator.comparing((Node e) -> 
            e.getBoard().getCurrentPossibleSquares().size()).reversed());
    if(a.getBoard().getCurrentPossibleSquares().isEmpty())
      children.add(new Node(a.getBoard().pass()));
    else {
      it = a.getBoard().getCurrentPossibleSquares().iterator();
      while(it.hasNext()){
        children.add(new Node(a.play(it.next())));
      }
    }
    return children.peek();   
  }
  
  public Square position(final Node a){

    return a.getSquare();
  }
  
}
class Memoizer<T, U> {
    private final Map<T, U> cache = new ConcurrentHashMap<>();

    private Memoizer() {}
    private Function<T, U> doMemoize(final Function<T, U> function) {
        return input -> cache.computeIfAbsent(input, function::apply);
    }

    public static <T, U> Function<T, U> memoize(final Function<T, U> function) {
        return new Memoizer<T, U>().doMemoize(function);
    }
}
