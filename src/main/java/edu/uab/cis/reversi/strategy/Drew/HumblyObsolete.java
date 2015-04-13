package edu.uab.cis.reversi.strategy.Drew;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.PMap;

import edu.uab.cis.reversi.Board;
import edu.uab.cis.reversi.Square;
import edu.uab.cis.reversi.Strategy;
import edu.uab.cis.reversi.Player;

public class HumblyObsolete implements Strategy {

	private final static int WINNING_POSITION = Integer.MAX_VALUE - 1;
	private final static int LOSING_POSITION = Integer.MIN_VALUE + 1;

	private final int LATE_GAME_CUTOFF = 50;

		@Override
  public Square chooseSquare(Board board) {

  	int plyDepth = 2;
   	int alpha = Integer.MIN_VALUE;
  	int beta = Integer.MAX_VALUE;

  	boolean isMaximizer = true;
  	boolean isWhite = (board.getCurrentPlayer() == Player.WHITE ? true : false);

  	Square bestSquare = null;

	if (board.getSquareOwners().size() == 4){
		System.out.println("in my special case");
		return chooseOne(board.getCurrentPossibleSquares());
	}

  	for (Square square : board.getCurrentPossibleSquares()){

		int score = goDeeper(board.play(square), plyDepth, alpha, beta, isMaximizer, isWhite);
		if (score > alpha){
			bestSquare = square;
			alpha = score;
		}
	}
	
	if (bestSquare == null) {
		System.out.println("best square is null, " + board.getSquareOwners().size() + " " + board.getCurrentPossibleSquares().size());
		//return chooseOne(board.getCurrentPossibleSquares());
	}
	System.out.println("about to return bestSquare: " + bestSquare);
  	return bestSquare;
  }	

private int goDeeper(Board board, int plyDepth, int alpha, int beta, boolean isMaximizer, boolean isWhite){

	//leaf node reached due to game being over
	if (board.isComplete()){
		return endGameHeuristic(board, isMaximizer, isWhite);
	}

	//leaf node due to max plys being reached
	if (plyDepth == 0){
		return applyHeuristic(board);
	}

	isMaximizer = !isMaximizer;
	isWhite = !isWhite;
	plyDepth--;

	while (board.getCurrentPossibleSquares().size() == 0){
		board = board.pass();
		isMaximizer = !isMaximizer;
		isWhite = !isWhite;	
	}

	//interior node
	for (Square square : board.getCurrentPossibleSquares()){

		int score = goDeeper(board.play(square), plyDepth, alpha, beta, isMaximizer, isWhite);

		if (isMaximizer){
			if (score >= beta){
				//prune
				return Integer.MAX_VALUE;
			}
			else if (score > alpha) {
				alpha = score;
			}
		}

		else{
			if (score <= alpha){
				//prune
				return Integer.MIN_VALUE;
			}
			else if (score < beta){
				beta = score;
			}
		}

  	}

  	if (isMaximizer){
  		return alpha;
  	}
  	else{
  		return beta;
  	}

}

private int applyHeuristic(Board board){
	if (board.getMoves().size() < LATE_GAME_CUTOFF) return earlyGameHeuristic(board);
	else return lateGameHeuristic(board);
}
//square values
private final static int CORNER = 99;
private final static int C = -8;
private final static int A = 8;
private final static int B = 6;
private final static int X = -24;
private final static int XA = -4;
private final static int XB = -3;
private final static int A2 = 7;
private final static int B2 = 4;
private final static int CENTER = 0;

static HashMap<Square, Integer> squareValues = new HashMap<Square, Integer>();
static{
squareValues.put(new Square(0, 0), CORNER);
squareValues.put(new Square(0, 1), C);
squareValues.put(new Square(0, 2), A);
squareValues.put(new Square(0, 3), B);
squareValues.put(new Square(0, 4), B);
squareValues.put(new Square(0, 5), A);
squareValues.put(new Square(0, 6), C);
squareValues.put(new Square(0, 7), CORNER);
squareValues.put(new Square(1, 0), C);
squareValues.put(new Square(1, 1), X);
squareValues.put(new Square(1, 2), XA);
squareValues.put(new Square(1, 3), XB);
squareValues.put(new Square(1, 4), XB);
squareValues.put(new Square(1, 5), XA);
squareValues.put(new Square(1, 6), X);
squareValues.put(new Square(1, 7), C);
squareValues.put(new Square(2, 0), A);
squareValues.put(new Square(2, 1), XA);
squareValues.put(new Square(2, 2), A2);
squareValues.put(new Square(2, 3), B2);
squareValues.put(new Square(2, 4), B2);
squareValues.put(new Square(2, 5), A2);
squareValues.put(new Square(2, 6), XA);
squareValues.put(new Square(2, 7), A);
squareValues.put(new Square(3, 0), B);
squareValues.put(new Square(3, 1), XB);
squareValues.put(new Square(3, 2), B2);
squareValues.put(new Square(3, 3), CENTER);
squareValues.put(new Square(3, 4), CENTER);
squareValues.put(new Square(3, 5), B2);
squareValues.put(new Square(3, 6), XB);
squareValues.put(new Square(3, 7), B);
squareValues.put(new Square(4, 0), B);
squareValues.put(new Square(4, 1), XB);
squareValues.put(new Square(4, 2), B2);
squareValues.put(new Square(4, 3), CENTER);
squareValues.put(new Square(4, 4), CENTER);
squareValues.put(new Square(4, 5), B2);
squareValues.put(new Square(4, 6), XB);
squareValues.put(new Square(4, 7), B);
squareValues.put(new Square(5, 0), A);
squareValues.put(new Square(5, 1), XA);
squareValues.put(new Square(5, 2), A2);
squareValues.put(new Square(5, 3), B2);
squareValues.put(new Square(5, 4), B2);
squareValues.put(new Square(5, 5), A2);
squareValues.put(new Square(5, 6), XA);
squareValues.put(new Square(5, 7), A);
squareValues.put(new Square(6, 0), C);
squareValues.put(new Square(6, 1), X);
squareValues.put(new Square(6, 2), XA);
squareValues.put(new Square(6, 3), XB);
squareValues.put(new Square(6, 4), XB);
squareValues.put(new Square(6, 5), XA);
squareValues.put(new Square(6, 6), X);
squareValues.put(new Square(6, 7), C);
squareValues.put(new Square(7, 0), CORNER);
squareValues.put(new Square(7, 1), C);
squareValues.put(new Square(7, 2), A);
squareValues.put(new Square(7, 3), B);
squareValues.put(new Square(7, 4), B);
squareValues.put(new Square(7, 5), A);
squareValues.put(new Square(7, 6), C);
squareValues.put(new Square(7, 7), CORNER);
};


private int earlyGameHeuristic(Board board){
	//positional
	Square square = board.getMoves().get(board.getMoves().size() - 1).getSquare();
	int positionalScore = squareValues.get(square);

	//mobility of opponent
	int opponentMoveNumber = board.getCurrentPossibleSquares().size();
	int opponentMoveNumberScore;
	if (opponentMoveNumber < 2)
		opponentMoveNumberScore = 12;
	else opponentMoveNumberScore = 8 - opponentMoveNumber;

	//frontiers
	HashMap<Square, Player> owners = new HashMap<Square, Player>(board.getSquareOwners());
	HashSet<Square> ownersSquares = new HashSet<Square>(owners.keySet());
	HashSet<Square> mySquares = new HashSet<Square>();
	HashSet<Square> opponentSquares = new HashSet<Square>();
	for (Square ownerSquare : ownersSquares){
		if (owners.get(ownerSquare) == board.getCurrentPlayer().opponent()){
			mySquares.add(ownerSquare);
		}
		else{
			opponentSquares.add(ownerSquare);
		}
	}
	int myFrontierCount = 0;
	int opponentFrontierCount = 0;
	for (Square mySquare : mySquares){
		int row = mySquare.getRow();
		int column = mySquare.getColumn();

		if (row != 0){
			if (!ownersSquares.contains(new Square(row - 1, column))) myFrontierCount++;
		}
		if (row != 0 && column != 0){
			if (!ownersSquares.contains(new Square(row - 1, column - 1))) myFrontierCount++;
		}
		if (column != 0){
			if (!ownersSquares.contains(new Square(row, column - 1))) myFrontierCount++;
		}
		if (column != 0 && row != 7){
			if (!ownersSquares.contains(new Square(row + 1, column - 1))) myFrontierCount++;
		}
		if (row != 7){
			if (!ownersSquares.contains(new Square(row + 1, column))) myFrontierCount++;
		}
		if (row != 7 && column != 7){
			if (!ownersSquares.contains(new Square(row + 1, column + 1))) myFrontierCount++;
		}
		if (column != 7){
			if (!ownersSquares.contains(new Square(row, column + 1))) myFrontierCount++;
		}
		if (column != 7 && row != 0){
			if (!ownersSquares.contains(new Square(row - 1, column + 1))) myFrontierCount++;
		}
	}
	for (Square opponentSquare : opponentSquares){
		int row = opponentSquare.getRow();
		int column = opponentSquare.getColumn();

		if (row != 0){
			if (!ownersSquares.contains(new Square(row - 1, column))) myFrontierCount++;
		}
		if (row != 0 && column != 0){
			if (!ownersSquares.contains(new Square(row - 1, column - 1))) myFrontierCount++;
		}
		if (column != 0){
			if (!ownersSquares.contains(new Square(row, column - 1))) myFrontierCount++;
		}
		if (column != 0 && row != 7){
			if (!ownersSquares.contains(new Square(row + 1, column - 1))) myFrontierCount++;
		}
		if (row != 7){
			if (!ownersSquares.contains(new Square(row + 1, column))) myFrontierCount++;
		}
		if (row != 7 && column != 7){
			if (!ownersSquares.contains(new Square(row + 1, column + 1))) myFrontierCount++;
		}
		if (column != 7){
			if (!ownersSquares.contains(new Square(row, column + 1))) myFrontierCount++;
		}
		if (column != 7 && row != 0){
			if (!ownersSquares.contains(new Square(row - 1, column + 1))) myFrontierCount++;
		}
	}

	int frontierBalanceScore = (myFrontierCount - opponentFrontierCount) / 3;

	return positionalScore + opponentMoveNumberScore + frontierBalanceScore;

	}

private int lateGameHeuristic(Board board){
	
	//positional score - weighted less
	Square square = board.getMoves().get(board.getMoves().size() - 1).getSquare();
	int positionalScore = squareValues.get(square) / 2;
	
	//mobility
	int opponentMoveNumberScore;
	int opponentMoveNumber = board.getCurrentPossibleSquares().size();
	if (opponentMoveNumber < 2)
		opponentMoveNumberScore = 12;
	else opponentMoveNumberScore = 8 - opponentMoveNumber;
	
	

	//parity
	Map<Square, Player> owner = board.getSquareOwners();
	int parityScore = 0, q1Parity = 0, q2Parity = 0, q3Parity = 0, q4Parity = 0;
    
    for(int i = 0; i < 8; i++){
        for(int j = 0; j < 8; j++){
            if(owner.containsKey(new Square(i, j))){
            }
            else{
                if(i<4){            // in top 1/2 of board
                    if(j<4)             // in Q1
                        q1Parity++;
                    else                // in Q2
                        q2Parity++;
                }
                else{               // in bot 1/2 of board
                    if(j<4)             // in Q3
                        q3Parity++;
                    else                // in Q4
                        q4Parity++;
                }
            }
        }
    }
	
    if(square.getRow()<4){                  // in top 1/2
        if(square.getColumn()<4){           // in Q1
            if(q1Parity%2 == 0)
                parityScore = 7;
            else
                parityScore = -7;
        }
        else{                               // in Q2
            if(q2Parity%2 == 0)
                parityScore = 7;
            else
                parityScore = -7;
        }
    }
    else{                                   // in bot 1/2
        if(square.getColumn()<4){           // in Q3
            if(q3Parity%2 == 0)
                parityScore = 7;
            else
                parityScore = -7;
        }
        else{                               // in Q4
            if(q4Parity%2 == 0)
                parityScore = 7;
            else
                parityScore = -7;
        }
    }

    
	//overall discs
	HashMap<Player, Integer> playerCounts = new HashMap<Player, Integer>(board.getPlayerSquareCounts());
	int squareBalanceScore = playerCounts.get(board.getCurrentPlayer().opponent()) - playerCounts.get(board.getCurrentPlayer());

	return positionalScore + opponentMoveNumberScore //+ parityScore
	 + squareBalanceScore;
}

    private int endGameHeuristic(Board board, boolean isMaximizer, boolean isWhite){

		//tie
		if (board.getWinner() == null) return 0;

		boolean isWhiteWinner = (board.getWinner() == Player.WHITE ? true : false);
		if (isWhiteWinner){
			if (isWhite && isMaximizer) return WINNING_POSITION;
			else if (isWhite && !isMaximizer) return LOSING_POSITION;
			else if (!isWhite && isMaximizer) return LOSING_POSITION;
			else return WINNING_POSITION;
		}
		else{
			if (!isWhite && isMaximizer) return WINNING_POSITION;
			else if (!isWhite && !isMaximizer) return LOSING_POSITION;
			else if (isWhite && isMaximizer) return LOSING_POSITION;
			else return WINNING_POSITION;
		}

	}

  public static <T> T chooseOne(Set<T> itemSet) {
    List<T> itemList = new ArrayList<>(itemSet);
    return itemList.get(new Random().nextInt(itemList.size()));
  }
}
