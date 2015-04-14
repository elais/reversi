package edu.uab.cis.reversi.strategy.group2;

import java.util.List;

public class Leaf{
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
