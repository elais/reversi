/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uab.cis.reversi.strategy.group2;

import edu.uab.cis.reversi.Board;
import edu.uab.cis.reversi.Player;
import edu.uab.cis.reversi.Square;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import edu.uab.cis.reversi.strategy.group2.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author elais
 */
final class Node {

  private final Board node;
  public Square s;
  public int row;
  public int column;
  public Map<Square,Player> squareOwners;
  public Player player;
  public Player opponent;
  public Set<Square> opponentSquares;
  public Node(Board node) {
    this.node = node;
    this.s = null;
    this.row = -1;
    this.column = -1;
    this.squareOwners = this.getBoard().getSquareOwners();
    this.player = this.getPlayer();
    this.opponent = this.getOpponent();
    
    if(this.getBoard().getCurrentPossibleSquares().isEmpty()){
      this.opponentSquares = this.getBoard().pass().getCurrentPossibleSquares();
    }
    else{
      this.opponentSquares = this.getBoard().play(this.getBoard()
            .getCurrentPossibleSquares().iterator().next())
            .getCurrentPossibleSquares();    
    }
      
  }

  public Board play(Square a) {
    Board play = node.play(a);
    return play;
  }
  
  public Board pass(){
    Board pass = this.getBoard().play(Square.PASS);
    return pass;
  }
  
  public Player getPlayer(){
    return node.getCurrentPlayer();
  }

  public Board getBoard() {
    Board board = node;
    return board;
  }

  public void setSquare(Square a){
    this.s = a;
    if(a != Square.PASS){
      this.row = a.getRow();
      this.column = a.getColumn();
    }
  }
  public Square getSquare() {
    return s;
  }

  public Player getOpponent() {
    return node.getCurrentPlayer().opponent();
  }
}

class Leaf{ 
  public Node node; 
  public int row;
  public int column;
  public double score;
  public List<Leaf> children;
  
  public Leaf(Node x, double y, List<Leaf> c) { 
    this.node = x; 
    this.score = y;
    this.children = c;
    
  }
  
  public double getScore(){
    return score;
  }
} 

class TranspositionTable{
  public int depth;
  public double value;
  public List<Leaf> children;
  public enum Bound{UPPERBOUND, LOWERBOUND, EXACT};
  public Bound flag;
  
  public TranspositionTable(int d, double s, List l){
    this.depth = d;
    this.value = s;
    this.children = l;
  }  
}