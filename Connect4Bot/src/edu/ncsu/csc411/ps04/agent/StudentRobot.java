package edu.ncsu.csc411.ps04.agent;

import java.util.ArrayList;

import edu.ncsu.csc411.ps04.environment.Environment;
import edu.ncsu.csc411.ps04.environment.Position;
import edu.ncsu.csc411.ps04.environment.Status;

public class StudentRobot extends Robot {
    // How many moves to look ahead
    private static final int DEPTH_BOUND           = 2;
    // When evaluating a state, the points to award out for 2 or 3 in-a-row
    private static final int TWO_IN_A_ROW_POINTS   = 1;
    private static final int THREE_IN_A_ROW_POINTS = 3;

    public StudentRobot ( final Environment env ) {
        super( env );
    }

    /**
     * Problem Set 04 - For this Problem Set you will design an agent that can
     * play Connect Four. The goal of Connect Four is to "connect" four (4)
     * markers of the same color (role) horizontally, vertically, or diagonally.
     * In this exercise your getAction method should return an integer between 0
     * and 6 (inclusive), representing the column you would like to "drop" your
     * marker. Unlike previous Problem Sets, in this environment, you will be
     * alternating turns with another agent.
     *
     * There are multiple example agents found in the
     * edu.ncsu.csc411.ps04.examples package. Each example agent provides a
     * brief explanation on its decision process, as well as demonstrations on
     * how to use the various methods from Environment. In order to pass this
     * Problem Set, you must successfully beat RandomRobot, VerticalRobot, and
     * HorizontalRobot 70% of the time as both the YELLOW and RED player. This
     * is distributed across the first six (6) test cases. In addition, you have
     * the chance to earn EXTRA CREDIT by beating GreedyRobot (test cases 07 and
     * 08) 70% of the time (10% possible, 5% per test case). Finally, if you
     * successfully pass the test cases, you are welcome to test your
     * implementation against your classmates.
     *
     * While Simple Reflex or Model-based agent may be able to succeed, consider
     * exploring the Minimax search algorithm to maximize your chances of
     * winning. While the first two will be easier, you may want to place
     * priority on moves that prevent the adversary from winning.
     */

    /**
     * This agent uses a Min-Max tree with alpha-beta pruning to try and produce
     * the best possible move for their current role(Yellow or Red).
     *
     * If we ignore alpha-beta pruning, this agent finds all possible states of
     * the board a set number of moves in the future. This constant number is
     * the DEPTH_BOUND. The agent assigns all these states a value
     * representative of their "goodness". This is the staticEval() function,
     * which in short gives a player 1 point for 2-in-a-row and 5 points for
     * 3-in-a-row, or infinite points for winning.
     *
     * With alpha-beta pruning, The total number of states the agent has to
     * evaluate decreases. Thus, the efficiency of the agent is drastically
     * improved, allowing the use of a larger DEPTH_BOUND.
     *
     * A DEPTH_BOUND of 2 is good enough to pass all tests in about half a
     * second. It is also good enough to consistently beat me. I can rarely draw
     * it, and even more rarely beat it.
     *
     * A DEPTH_BOUND of 6 passes all tests in about a minute. I wasn't able to
     * beat it.
     *
     * A DEPTH_BOUND of 10 is about the upper limit. The tests take way too
     * long, but an individual simulation is doable. The robot can take about 5
     * seconds per move, especially near the beginning of a game.
     */
    @Override
    public int getAction () {
        final Position[][] currentState = super.env.clonePositions();
        final Status myRole = super.getRole();
        final ArrayList<Integer> options = getValidActionsForState( currentState );
        int bestOption = -1;
        Integer alpha = Integer.MIN_VALUE;
        Integer beta = Integer.MAX_VALUE;
        // If yellow, we want to minimize
        if ( myRole == Status.YELLOW ) {
            for ( final Integer option : options ) {
                final int maxValue = maxValue( super.perceive( option, clonePositions( currentState ), myRole ), 1,
                        myRole, alpha, beta );
                if ( maxValue < beta ) {
                    beta = maxValue;
                    bestOption = option;
                }
                if ( beta <= alpha ) {
                    return bestOption;
                }
            }
        }
        // Otherwise, if red, maximize
        else {
            for ( final Integer option : options ) {
                final int minValue = minValue( super.perceive( option, clonePositions( currentState ), myRole ), 1,
                        myRole, alpha, beta );
                if ( minValue > alpha ) {
                    alpha = minValue;
                    bestOption = option;
                }
                if ( alpha >= beta ) {
                    return bestOption;
                }
            }
        }
        return bestOption;
    }

    private int maxValue ( final Position[][] state, final int depth, final Status role, Integer alpha,
            final Integer beta ) {
        // Find what our opposition's role is
        final Status oppRole = role == Status.RED ? Status.YELLOW : Status.RED;
        // Evaluate the state of the board
        final int staticEval = staticEval( state );
        // If this is a leaf node, we return its eval
        if ( depth == DEPTH_BOUND || staticEval == Integer.MAX_VALUE - 1 || staticEval == Integer.MIN_VALUE + 1 ) {
            return staticEval( state );
        }
        // For every move that we can make...
        final ArrayList<Integer> options = getValidActionsForState( state );
        for ( final Integer option : options ) {
            // Find the maximum of all the minimums
            alpha = Math.max( alpha, minValue( super.perceive( option, clonePositions( state ), oppRole ), depth + 1,
                    oppRole, alpha, beta ) );
            // Alpha-beta pruning
            if ( alpha >= beta ) {
                return alpha;
            }
        }
        return alpha;
    }

    private int minValue ( final Position[][] state, final int depth, final Status role, final Integer alpha,
            Integer beta ) {
        // Find what our opposition's role is
        final Status oppRole = role == Status.RED ? Status.YELLOW : Status.RED;
        // Evaluate the state of the board
        final int staticEval = staticEval( state );
        // If this is a leaf node, we return its eval
        if ( depth == DEPTH_BOUND || staticEval == Integer.MAX_VALUE - 1 || staticEval == Integer.MIN_VALUE + 1 ) {
            return staticEval( state );
        }
        // For every move that we can make...
        final ArrayList<Integer> options = getValidActionsForState( state );
        for ( final Integer option : options ) {
            // Find the minimum of all the maximums
            beta = Math.min( beta, maxValue( super.perceive( option, clonePositions( state ), oppRole ), depth + 1,
                    oppRole, alpha, beta ) );
            // Alpha-Beta pruning
            if ( beta <= alpha ) {
                return beta;
            }
        }
        return beta;
    }

    // Evaluates the current state of the board
    //
    // If a player has won, their score is INT_MAX - 1
    //
    // If no one has won yet...
    // Give a player 1 point if they have 2 pieces in a row
    // Give a player 3 points if they have 3 pieces in a row
    //
    // The final score returned is RED_SCORE - YELLOW_SCORE
    private static int staticEval ( final Position[][] currentState ) {
        // If the game is already over, return corresponding values
        final int rows = currentState.length;
        final int cols = currentState[0].length;
        // Tally-up all horizontal points
        int horizontalEval = 0;
        for ( int i = 0; i < rows; i++ ) {
            for ( int j = 0; j < cols; j++ ) {
                final Status thisTile = currentState[i][j].getStatus();
                switch ( thisTile ) {
                    // If this tile is red
                    case RED:
                        // And if the next one is red, add ONE_IN_A_ROW_POINTS
                        // point
                        if ( j + 1 < cols && currentState[i][j + 1].getStatus() == Status.RED ) {
                            horizontalEval += TWO_IN_A_ROW_POINTS;
                            j++;
                            // And if the one after that is red, add
                            // THREE_IN_A_ROW_POINTS points
                            if ( j + 1 < cols && currentState[i][j + 1].getStatus() == Status.RED ) {
                                horizontalEval += THREE_IN_A_ROW_POINTS;
                                j++;
                                // If 4 in a row
                                if ( j + 1 < cols && currentState[i][j + 1].getStatus() == Status.RED ) {
                                    return Integer.MAX_VALUE - 1;
                                }
                            }
                        }
                        break;
                    // If this tile is yellow
                    case YELLOW:
                        // If the next one is yellow, subtract
                        // ONE_IN_A_ROW_POINTS point
                        if ( j + 1 < cols && currentState[i][j + 1].getStatus() == Status.YELLOW ) {
                            horizontalEval -= TWO_IN_A_ROW_POINTS;
                            j++;
                            // And if the one after that is yellow, subtract
                            // THREE_IN_A_ROW_POINTS
                            // points
                            if ( j + 1 < cols && currentState[i][j + 1].getStatus() == Status.YELLOW ) {
                                horizontalEval -= THREE_IN_A_ROW_POINTS;
                                j++;
                                // If 4 in arow
                                if ( j + 1 < cols && currentState[i][j + 1].getStatus() == Status.YELLOW ) {
                                    return Integer.MIN_VALUE + 1;
                                }
                            }
                        }
                        break;
                    // If this tile is blank, do nothing and go to next tile
                    case BLANK:
                        // Should be unreachable code
                    case DRAW:
                    case ONGOING:
                    case RED_WIN:
                    case YELLOW_WIN:
                    default:
                }
            }
        }
        // Tally-up all vertical points
        int verticalEval = 0;
        for ( int i = 0; i < cols; i++ ) {
            for ( int j = 0; j < rows; j++ ) {
                final Status thisTile = currentState[j][i].getStatus();
                switch ( thisTile ) {
                    // If this tile is red
                    case RED:
                        // And if the next one is red, add ONE_IN_A_ROW_POINTS
                        // point
                        if ( j + 1 < rows && currentState[j + 1][i].getStatus() == Status.RED ) {
                            verticalEval += TWO_IN_A_ROW_POINTS;
                            j++;
                            // And if the one after that is red, add
                            // THREE_IN_A_ROW_POINTS points
                            if ( j + 1 < rows && currentState[j + 1][i].getStatus() == Status.RED ) {
                                verticalEval += THREE_IN_A_ROW_POINTS;
                                j++;
                                // If 4 in a row
                                if ( j + 1 < rows && currentState[j + 1][i].getStatus() == Status.RED ) {
                                    return Integer.MAX_VALUE - 1;
                                }
                            }
                        }
                        break;
                    // If this tile is yellow
                    case YELLOW:
                        // If the next one is yellow, subtract
                        // ONE_IN_A_ROW_POINTS point
                        if ( j + 1 < rows && currentState[j + 1][i].getStatus() == Status.YELLOW ) {
                            verticalEval -= TWO_IN_A_ROW_POINTS;
                            j++;
                            // And if the one after that is yellow, subtract
                            // THREE_IN_A_ROW_POINTS
                            // points
                            if ( j + 1 < rows && currentState[j + 1][i].getStatus() == Status.YELLOW ) {
                                verticalEval -= THREE_IN_A_ROW_POINTS;
                                j++;
                                // If 4 in a row
                                if ( j + 1 < rows && currentState[j + 1][i].getStatus() == Status.YELLOW ) {
                                    return Integer.MIN_VALUE + 1;
                                }
                            }
                        }
                        break;
                    // If this tile is blank, do nothing and go to next tile
                    case BLANK:
                        // Should be unreachable code
                    case DRAW:
                    case ONGOING:
                    case RED_WIN:
                    case YELLOW_WIN:
                    default:
                }
            }
        }
        // Tally-up all diagonals that start at top left
        int topLeftDiagonalEval = 0;
        // There are rows + cols - 1 number of top-left starting points
        for ( int i = 0; i < rows + cols - 1; i++ ) {
            // Calculate starting point based on iteration number
            int rowIdx = 0, colIdx = 0;
            if ( i < rows ) {
                rowIdx = rows - i - 1;
            }
            else {
                colIdx = i - rows + 1;
            }
            // Evaluate this diagonal
            while ( rowIdx < rows && colIdx < cols ) {
                final Status thisTile = currentState[rowIdx][colIdx].getStatus();
                switch ( thisTile ) {
                    // If this tile is red
                    case RED:
                        // And if the next one is red, add ONE_IN_A_ROW_POINTS
                        // point
                        if ( rowIdx + 1 < rows && colIdx + 1 < cols
                                && currentState[rowIdx + 1][colIdx + 1].getStatus() == Status.RED ) {
                            topLeftDiagonalEval += TWO_IN_A_ROW_POINTS;
                            rowIdx++;
                            colIdx++;
                            // And if the one after that is red, add
                            // THREE_IN_A_ROW_POINTS points
                            if ( rowIdx + 1 < rows && colIdx + 1 < cols
                                    && currentState[rowIdx + 1][colIdx + 1].getStatus() == Status.RED ) {
                                topLeftDiagonalEval += THREE_IN_A_ROW_POINTS;
                                rowIdx++;
                                colIdx++;
                                // If 4 in a row
                                if ( rowIdx + 1 < rows && colIdx + 1 < cols
                                        && currentState[rowIdx + 1][colIdx + 1].getStatus() == Status.RED ) {
                                    return Integer.MAX_VALUE - 1;
                                }
                            }
                        }
                        rowIdx++;
                        colIdx++;
                        break;
                    // If this tile is yellow
                    case YELLOW:
                        // And if the next one is red, add ONE_IN_A_ROW_POINTS
                        // point
                        if ( rowIdx + 1 < rows && colIdx + 1 < cols
                                && currentState[rowIdx + 1][colIdx + 1].getStatus() == Status.YELLOW ) {
                            topLeftDiagonalEval -= TWO_IN_A_ROW_POINTS;
                            rowIdx++;
                            colIdx++;
                            // And if the one after that is red, add
                            // THREE_IN_A_ROW_POINTS points
                            if ( rowIdx + 1 < rows && colIdx + 1 < cols
                                    && currentState[rowIdx + 1][colIdx + 1].getStatus() == Status.YELLOW ) {
                                topLeftDiagonalEval -= THREE_IN_A_ROW_POINTS;
                                rowIdx++;
                                colIdx++;
                                // If 4 in a row
                                if ( rowIdx + 1 < rows && colIdx + 1 < cols
                                        && currentState[rowIdx + 1][colIdx + 1].getStatus() == Status.YELLOW ) {
                                    return Integer.MIN_VALUE + 1;
                                }
                            }
                        }
                        rowIdx++;
                        colIdx++;
                        break;
                    // If this tile is blank, do nothing and go to next tile
                    case BLANK:
                        rowIdx++;
                        colIdx++;
                        // Should be unreachable code
                    case DRAW:
                    case ONGOING:
                    case RED_WIN:
                    case YELLOW_WIN:
                    default:
                }
            }
        }
        // Tally-up all diagonals that start at top right
        int topRightDiagonalEval = 0;
        // There are rows + cols - 1 number of top-right starting points
        for ( int i = 0; i < rows + cols - 1; i++ ) {
            // Calculate starting point based on iteration number
            int rowIdx = 0, colIdx = cols - 1;
            if ( i < rows ) {
                rowIdx = rows - i - 1;
            }
            else {
                colIdx = i - rows;
            }
            // Evaluate this diagonal
            while ( rowIdx < rows && colIdx >= 0 ) {
                final Status thisTile = currentState[rowIdx][colIdx].getStatus();
                switch ( thisTile ) {
                    // If this tile is red
                    case RED:
                        // And if the next one is red, add ONE_IN_A_ROW_POINTS
                        // point
                        if ( rowIdx + 1 < rows && colIdx - 1 >= 0
                                && currentState[rowIdx + 1][colIdx - 1].getStatus() == Status.RED ) {
                            topRightDiagonalEval += TWO_IN_A_ROW_POINTS;
                            rowIdx++;
                            colIdx--;
                            // And if the one after that is red, add
                            // THREE_IN_A_ROW_POINTS points
                            if ( rowIdx + 1 < rows && colIdx - 1 >= 0
                                    && currentState[rowIdx + 1][colIdx - 1].getStatus() == Status.RED ) {
                                topRightDiagonalEval += THREE_IN_A_ROW_POINTS;
                                rowIdx++;
                                colIdx--;
                                // If 4 in a row
                                if ( rowIdx + 1 < rows && colIdx - 1 >= 0
                                        && currentState[rowIdx + 1][colIdx - 1].getStatus() == Status.RED ) {
                                    return Integer.MAX_VALUE - 1;
                                }
                            }
                        }
                        rowIdx++;
                        colIdx--;
                        break;
                    // If this tile is yellow
                    case YELLOW:
                        // And if the next one is red, add ONE_IN_A_ROW_POINTS
                        // point
                        if ( rowIdx + 1 < rows && colIdx - 1 >= 0
                                && currentState[rowIdx + 1][colIdx - 1].getStatus() == Status.YELLOW ) {
                            topRightDiagonalEval -= TWO_IN_A_ROW_POINTS;
                            rowIdx++;
                            colIdx--;
                            // And if the one after that is red, add
                            // THREE_IN_A_ROW_POINTS points
                            if ( rowIdx + 1 < rows && colIdx - 1 >= 0
                                    && currentState[rowIdx + 1][colIdx - 1].getStatus() == Status.YELLOW ) {
                                topRightDiagonalEval -= THREE_IN_A_ROW_POINTS;
                                rowIdx++;
                                colIdx--;
                                // If 4 in a row
                                if ( rowIdx + 1 < rows && colIdx - 1 >= 0
                                        && currentState[rowIdx + 1][colIdx - 1].getStatus() == Status.YELLOW ) {
                                    return Integer.MIN_VALUE + 1;
                                }
                            }
                        }
                        rowIdx++;
                        colIdx--;
                        break;
                    // If this tile is blank, do nothing and go to next tile
                    case BLANK:
                        rowIdx++;
                        colIdx--;
                        // Should be unreachable code
                    case DRAW:
                    case ONGOING:
                    case RED_WIN:
                    case YELLOW_WIN:
                    default:
                }
            }
        }
        return horizontalEval + verticalEval + topLeftDiagonalEval + topRightDiagonalEval;
    }

    // Helper debug function
    private static void printPositions ( final Position[][] positions ) {
        for ( int i = 0; i < positions.length; i++ ) {
            System.out.printf( "%d| ", i );
            for ( int j = 0; j < positions[i].length; j++ ) {
                final Status thisPos = positions[i][j].getStatus();
                Character print = ' ';
                if ( thisPos == Status.RED ) {
                    print = 'R';
                }
                else if ( thisPos == Status.YELLOW ) {
                    print = 'Y';
                }
                System.out.print( print );
            }
            System.out.println( " | " );
        }
        System.out.printf( "   0123456\n" );
    }

    /** Returns an ArrayList of columns with an available space. */
    // Repurposed from Environent.getValidActions()
    private ArrayList<Integer> getValidActionsForState ( final Position[][] state ) {
        final ArrayList<Integer> actions = new ArrayList<Integer>();
        for ( int col = 0; col < state[0].length; col++ ) {
            if ( state[0][col].getStatus() == Status.BLANK ) {
                actions.add( col );
            }
        }
        return actions;
    }

    // Clone a given positions array
    // Repurposed from Environment.clonePosition()
    private Position[][] clonePositions ( final Position[][] positions ) {
        // Create a blank version of the board
        final int rows = positions.length, cols = positions[0].length;
        final Position[][] clone = new Position[rows][cols];
        for ( int row = 0; row < rows; row++ ) {
            for ( int col = 0; col < cols; col++ ) {
                // Update the square to either blank, red, or yellow
                if ( positions[row][col].getStatus() == Status.BLANK ) {
                    clone[row][col] = new Position( row, col, Status.BLANK );
                }
                else if ( positions[row][col].getStatus() == Status.RED ) {
                    clone[row][col] = new Position( row, col, Status.RED );
                }
                else if ( positions[row][col].getStatus() == Status.YELLOW ) {
                    clone[row][col] = new Position( row, col, Status.YELLOW );
                }
            }
        }
        return clone;
    }

}
