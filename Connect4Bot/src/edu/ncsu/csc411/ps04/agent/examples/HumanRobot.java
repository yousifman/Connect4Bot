package edu.ncsu.csc411.ps04.agent.examples;

import java.util.Scanner;

import edu.ncsu.csc411.ps04.agent.Robot;
import edu.ncsu.csc411.ps04.environment.Environment;

/**
 * A VERY dumb agent that attempts to drop game pieces outside of the game
 * board. This is only really useful for debugging your own agent's decisions.
 * DO NOT MODIFY.
 *
 * @author Adam Gaweda
 *
 */
public class HumanRobot extends Robot {

    public HumanRobot ( final Environment env ) {
        super( env );
    }

    /** Returns -1, or an invalid move, thus DO_NOTHING. */
    @Override
    public int getAction () {
        System.out.println( "What column will you drop in?[1-7]" );
        final Scanner input = new Scanner( System.in );
        return input.nextInt() - 1;
    }

}
