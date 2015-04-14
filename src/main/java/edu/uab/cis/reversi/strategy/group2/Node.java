/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uab.cis.reversi.strategy.group2;

import edu.uab.cis.reversi.Board;
import edu.uab.cis.reversi.Player;
import edu.uab.cis.reversi.Square;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author elais
 */
public final class Node {

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
    return node.play(a);
  }
  
  public Board pass(){
    return this.getBoard().play(Square.PASS);
  }
  
  public Player getPlayer(){
    return node.getCurrentPlayer();
  }

  public Board getBoard() {
    return node;
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

