package edu.uab.cis.reversi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Function;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

/**
 * A game of Reversi, played by two strategies.
 */
public class Reversi {

  /**
   * The command-line options for the {@link Reversi#main(String...)} method.
   */
  public static interface Options {
    @Option(
        longName = "strategies",
        description = "Subclasses of edu.uab.cis.reversi.Strategy to play against each other")
    public List<String> getStrategies();

    @Option(
        longName = "games",
        defaultValue = "10",
        description = "Number of games to play against each strategy")
    public int getNumberOfGames();

    @Option(
        longName = "timeout",
        defaultValue = "100",
        description = "Maximum time allowed to a strategy for choosing a square")
    public long getTimeout();

    @Option(
        longName = "timeout-unit",
        defaultValue = "MILLISECONDS",
        description = "Unit of the timeout, e.g. MILLISECONDS")
    public TimeUnit getTimeoutUnit();
  }

  enum Result {
    WIN, LOSS, TIE, FAIL
  }

  /**
   * Runs a round-robin tournament over Reversi strategies.
   * 
   * @see Options for the available command-line options.
   */
  public static void main(String... args) throws Exception {
    Options options = CliFactory.parseArguments(Options.class, args);
    int nGames = options.getNumberOfGames();
    long timeout = options.getTimeout();
    TimeUnit timeoutUnit = options.getTimeoutUnit();

    // convert strategy names to class instances
    List<Strategy> strategies = Lists.newArrayList();
    for (String strategyName : options.getStrategies()) {
      Strategy strategy = Class.forName(strategyName).asSubclass(Strategy.class).newInstance();
      strategy.setChooseSquareTimeLimit(timeout, timeoutUnit);
      strategies.add(strategy);
    }

    // keep track of number of wins for each strategy
    final Map<Strategy, Multiset<Result>> results = Maps.newHashMap();
    for (Strategy strategy : strategies) {
      results.put(strategy, HashMultiset.<Result> create());
    }

    // Run N rounds, pairing each strategy with each other strategy. There will
    // actually be 2N games since each strategy gets to be both black and white
    Board board = new Board();
    for (int game = 0; game < nGames; ++game) {
      for (int i = 0; i < strategies.size(); ++i) {
        for (int j = i + 1; j < strategies.size(); ++j) {
          Strategy strategy1 = strategies.get(i);
          Strategy strategy2 = strategies.get(j);
          Reversi reversi;

          // first game: strategy1=BLACK, strategy2=WHITE
          reversi = new Reversi(strategy1, strategy2, timeout, timeoutUnit);
          updateResults(results, reversi, board);

          // second game: strategy2=BLACK, strategy1=WHITE
          reversi = new Reversi(strategy2, strategy1, timeout, timeoutUnit);
          updateResults(results, reversi, board);
        }
      }
    }

    // rank strategies by number of wins
    System.out.printf("%4s\t%4s\t%4s\t%4s\n", "win", "loss", "tie", "fail");
    Ordering<Strategy> byWins = Ordering.natural().onResultOf(new Function<Strategy, Integer>() {
      @Override
      public Integer apply(Strategy strategy) {
        return results.get(strategy).count(Result.WIN);
      }
    }).reverse();
    for (Strategy strategy : byWins.sortedCopy(strategies)) {
      Multiset<Result> strategyResults = results.get(strategy);
      System.out.printf(
          "%4d\t%4d\t%4d\t%4d\t%s\n",
          strategyResults.count(Result.WIN),
          strategyResults.count(Result.LOSS),
          strategyResults.count(Result.TIE),
          strategyResults.count(Result.FAIL),
          strategy.getClass().getSimpleName());
    }
  }

  private static void updateResults(
      Map<Strategy, Multiset<Result>> results,
      Reversi reversi,
      Board board) {
    Strategy winner;
    try {
      winner = reversi.getWinner(reversi.play(board));
      if (winner != null) {
        results.get(winner).add(Result.WIN);
        for (Strategy strategy : reversi.strategies.values()) {
          if (strategy != winner) {
            results.get(strategy).add(Result.LOSS);
          }
        }
      } else {
        for (Strategy strategy : reversi.strategies.values()) {
          results.get(strategy).add(Result.TIE);
        }
      }
    } catch (StrategyTimedOutException e) {
      results.get(e.getOpponentStrategy()).add(Result.WIN);
      results.get(e.getTimedOutStrategy()).add(Result.FAIL);
    }
  }

  private Map<Player, Strategy> strategies;

  private long timeout;

  private TimeUnit timeoutUnit;

  /**
   * Creates a new Reversi game.
   * 
   * @param blackStrategy
   *          The strategy used to play the black pieces.
   * @param whiteStrategy
   *          The strategy used to play the white pieces.
   */
  public Reversi(Strategy blackStrategy, Strategy whiteStrategy) {
    this(blackStrategy, whiteStrategy, 100, TimeUnit.MILLISECONDS);
  }

  /**
   * Creates a new Reversi game.
   * 
   * @param blackStrategy
   *          The strategy used to play the black pieces.
   * @param whiteStrategy
   *          The strategy used to play the white pieces.
   * @param timeout
   *          The maximum time allowed to a strategy for choosing a square.
   * @param timeoutUnit
   *          The unit of the timeout, e.g. {@link TimeUnit#MILLISECONDS}
   */
  public Reversi(Strategy blackStrategy, Strategy whiteStrategy, long timeout, TimeUnit timeoutUnit) {
    this.strategies = new HashMap<>();
    this.strategies.put(Player.BLACK, blackStrategy);
    this.strategies.put(Player.WHITE, whiteStrategy);
    this.timeout = timeout;
    this.timeoutUnit = timeoutUnit;
  }

  /**
   * Plays the strategies on the given Reversi board.
   * 
   * @param board
   *          The board in its initial state.
   * @return The board after play is complete.
   * @throws StrategyTimedOutException
   *           If a strategy exceeds the alloted time to choose a square.
   */
  public Board play(Board board) throws StrategyTimedOutException {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Board curr = board;
    while (!curr.isComplete()) {
      if (curr.getCurrentPossibleSquares().isEmpty()) {
        curr = curr.pass();
      } else {
        Player player = curr.getCurrentPlayer();
        final Strategy strategy = this.strategies.get(player);
        final Board boardForFuture = curr;
        Future<Square> future = executor.submit(() -> strategy.chooseSquare(boardForFuture));
        try {
          Square square = future.get(this.timeout, this.timeoutUnit);
          curr = curr.play(square);
        } catch (Exception e) {
          future.cancel(true);
          throw new StrategyTimedOutException(strategy, this.strategies.get(player.opponent()));
        }
      }
    }
    executor.shutdownNow();
    return curr;
  }

  /**
   * Gets the winning strategy from a board.
   * 
   * This maps from the {@link Player}s of the board to the {@link Strategy}s of
   * the Reversi game.
   * 
   * @param board
   *          A Reversi board, after the game is complete.
   * @return The winning strategy.
   */
  public Strategy getWinner(Board board) {
    return this.strategies.get(board.getWinner());
  }

  /**
   * Identifies a strategy that exceeded the allotted time to choose a square.
   */
  public static class StrategyTimedOutException extends Exception {
    private static final long serialVersionUID = 1L;

    public StrategyTimedOutException(Strategy timedOutStrategy, Strategy opponentStrategy) {
      this.timedOutStrategy = timedOutStrategy;
      this.opponentStrategy = opponentStrategy;
    }

    private Strategy timedOutStrategy;

    /**
     * @return The strategy that exceeded the allotted time.
     */
    public Strategy getTimedOutStrategy() {
      return this.timedOutStrategy;
    }

    /**
     * @return The strategy that was playing against the strategy that exceeded
     *         the allotted time.
     */
    public Strategy getOpponentStrategy() {
      return this.opponentStrategy;
    }

    private Strategy opponentStrategy;
  }
}
