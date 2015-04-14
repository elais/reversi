package edu.uab.cis.reversi.strategy.group2;

import java.util.List;

public class TranspositionTable{
  public int depth;
  public double value;
  public List<Leaf> children;
  public enum Bound{UPPERBOUND, LOWERBOUND, EXACT};
  public Bound flag;

  public TranspositionTable(int d, double s, List<Leaf> l){
    this.depth = d;
    this.value = s;
    this.children = l;
  }
}
