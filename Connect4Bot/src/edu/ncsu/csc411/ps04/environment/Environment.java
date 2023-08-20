package edu.ncsu.csc411.ps04.environment;

import java.util.ArrayList;

import edu.ncsu.csc411.ps04.agent.Robot;

/**
 * This produces a 6x7 Connect Four board. These dimensions will not change. DO
 * NOT MODIFY.
 *
 * @author Adam Gaweda
 */
public class Environment {
    private final Position[][] positions;
    private Robot              redPlayer, yellowPlayer;
    private final int          rows, cols;
    private int                turn;

    /** Constructors to build the grid */
    public Environment () {
        this( 6, 7 );
    }

    public Environment ( final int rows, final int columns ) {
        this.rows = rows;
        this.cols = columns;
        this.positions = new Position[this.rows][this.cols];
        for ( int row = 0; row < rows; row++ ) {
            for ( int col = 0; col < cols; col++ ) {
                // Create a new position at (row, col)
                final Position p = new Position( row, col, Status.BLANK );
                this.positions[row][col] = p;
            }
        }
        this.turn = 0;
    }

    /** Returns an ArrayList of columns with an available space. */
    public ArrayList<Integer> getValidActions () {
        final ArrayList<Integer> actions = new ArrayList<Integer>();
        for ( int col = 0; col < cols; col++ ) {
            if ( this.positions[0][col].getStatus() == Status.BLANK ) {
                actions.add( col );
            }
        }
        return actions;
    }

    /**
     * Returns a boolean value indicating whether the game has reached a
     * terminal state
     */
    public boolean hasGameTerminated () {
        final Status status = getGameStatus();
        return ( status == Status.DRAW || status == Status.RED_WIN || status == Status.YELLOW_WIN );
    }

    /**
     * Runs through each tile and checks to see if it has winning connection.
     */
    public Status getGameStatus () {
        // Iterate through each tile, checking if there are 4 adjacent tiles
        // that
        // are the same
        for ( int row = 0; row < rows; row++ ) {
            for ( int col = 0; col < cols; col++ ) {
                final Status status = this.positions[row][col].getStatus();
                if ( status == Status.BLANK ) {
                    continue;
                }
                final boolean horizontal = checkHorizontal( row, col );
                final boolean vertical = checkVertical( row, col );
                final boolean diagonalBR = checkDiagonalBottomRight( row, col );
                final boolean diagonalTR = checkDiagonalTopRight( row, col );
                if ( horizontal || vertical || diagonalBR || diagonalTR ) {
                    if ( status == Status.RED ) {
                        return Status.RED_WIN;
                    }
                    else {
                        return Status.YELLOW_WIN;
                    }
                }
            }
        }

        // Check for Blank Tiles
        boolean blankTileCheck = false;
        for ( int row = 0; row < rows; row++ ) {
            for ( int col = 0; col < cols; col++ ) {
                if ( this.positions[row][col].getStatus() == Status.BLANK ) {
                    blankTileCheck = true;
                }
            }
        }

        // If there are no BLANK spaces left and no 4 connection, the game is
        // a DRAW. Otherwise, the game is still ONGOING.
        if ( blankTileCheck ) {
            return Status.ONGOING;
        }
        else {
            return Status.DRAW;
        }
    }

    /**
     * Looks 4 tiles to the right to see if there are 4 adjacent tiles with the
     * same color
     */
    private boolean checkHorizontal ( final int row, final int col ) {
        final Status status = this.positions[row][col].getStatus();
        if ( col + 4 > cols ) {
            return false; // out of bounds
        }
        for ( int i = 0; i < 4; i++ ) {
            if ( status != this.positions[row][col + i].getStatus() ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Looks 4 tiles downward to see if there are 4 adjacent tiles with the same
     * color
     */
    private boolean checkVertical ( final int row, final int col ) {
        final Status status = this.positions[row][col].getStatus();
        if ( row + 4 > rows ) {
            return false; // out of bounds
        }
        for ( int i = 0; i < 4; i++ ) {
            if ( status != this.positions[row + i][col].getStatus() ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Looks 4 tiles to the bottom-right to see if there are 4 adjacent tiles
     * with the same color
     */
    private boolean checkDiagonalBottomRight ( final int row, final int col ) {
        final Status status = this.positions[row][col].getStatus();
        if ( row + 4 > rows ) {
            return false; // out of bounds
        }
        if ( col + 4 > cols ) {
            return false; // out of bounds
        }
        for ( int i = 0; i < 4; i++ ) {
            if ( status != this.positions[row + i][col + i].getStatus() ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Looks 4 tiles to the top-right to see if there are 4 adjacent tiles with
     * the same color
     */
    private boolean checkDiagonalTopRight ( final int row, final int col ) {
        final Status status = this.positions[row][col].getStatus();
        if ( row - 4 < 0 ) {
            return false; // out of bounds
        }
        if ( col + 4 > cols ) {
            return false; // out of bounds
        }
        for ( int i = 0; i < 4; i++ ) {
            if ( status != this.positions[row - i][col + i].getStatus() ) {
                return false;
            }
        }
        return true;
    }

    /** Traditional Getters */
    public int getRows () {
        return this.rows;
    }

    public int getCols () {
        return this.cols;
    }

    /**
     * Gets the new state of the world after robot actions. The Environment
     * increments the turn variable to determine whose move it is (even turns =
     * YELLOW, odd turns = RED).
     */
    public void updateEnvironment () {
        if ( this.redPlayer == null || this.yellowPlayer == null ) {
            throw new IllegalArgumentException( "Agents not initialized" );
        }
        if ( hasGameTerminated() ) {
            return;
        }

        Robot agent;
        // Per the official rules, yellow goes first
        if ( this.turn % 2 == 0 ) {
            // Yellow Player's Turn
            agent = this.yellowPlayer;
        }
        else {
            // Red Player's Turn
            agent = this.redPlayer;
        }

        final int col = agent.getAction();

        // Check to make sure this column is a valid move.
        // If it is not, treats the agent's move as DO_NOTHING
        // Only need to look at the 0th row, since we're dropping
        // the game piece to the bottom-most row
        if ( validPosition( 0, col ) ) {
            drop( col, agent.getRole() );
        }

        this.turn++;
    }

    /**
     * Returns a boolean value if a particular (row, col) is within the Position
     * array (aka, within the game board).
     */
    private boolean validPosition ( final int row, final int col ) {
        if ( row < 0 || row > this.rows - 1 ) {
            return false;
        }
        else if ( col < 0 || col > this.cols - 1 ) {
            return false;
        }

        final Position position = this.positions[row][col];
        if ( position.getStatus() != Status.BLANK ) {
            return false;
        }

        return true;
    }

    /** "Drops" the player's marker to the next available spot in the column */
    public void drop ( final int col, final Status role ) {
        Status status = Status.RED;
        if ( role == Status.YELLOW ) {
            status = Status.YELLOW;
        }
        for ( int row = 0; row < rows; row++ ) {
            if ( this.positions[row][col].getStatus() != Status.BLANK ) {
                this.positions[row - 1][col].setStatus( status );
                return;
            }
        }
        this.positions[rows - 1][col].setStatus( status );
    }

    /** Adds a player to the game and sets its role (YELLOW or RED) */
    public void addPlayer ( final Robot robot, final Status role ) {
        if ( role == Status.RED ) {
            this.redPlayer = robot;
        }
        else if ( role == Status.YELLOW ) {
            this.yellowPlayer = robot;
        }

        robot.setRole( role );
    }

    /**
     * Returns a cloned copy of the Position[][] positions variable. This will
     * allow agents to view a copy of the board without actually modifying the
     * current status of the game. Since Position is a complex data type, each
     * element in the array needs to be recreated; otherwise it would allow a
     * malicious agent to "cheat" by automatically changing the Position's
     * status with setStatus.
     */
    public Position[][] clonePositions () {
        // Create a blank version of the board
        final Position[][] clone = new Position[this.rows][this.cols];
        for ( int row = 0; row < this.rows; row++ ) {
            for ( int col = 0; col < this.cols; col++ ) {
                // Update the square to either blank, red, or yellow
                if ( this.positions[row][col].getStatus() == Status.BLANK ) {
                    clone[row][col] = new Position( row, col, Status.BLANK );
                }
                else if ( this.positions[row][col].getStatus() == Status.RED ) {
                    clone[row][col] = new Position( row, col, Status.RED );
                }
                else if ( this.positions[row][col].getStatus() == Status.YELLOW ) {
                    clone[row][col] = new Position( row, col, Status.YELLOW );
                }
            }
        }
        return clone;
    }
}
