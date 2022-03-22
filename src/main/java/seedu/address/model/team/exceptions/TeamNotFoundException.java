package seedu.address.model.team.exceptions;

/**
 * Signals that the operation is unable to find the specified team.
 */
public class TeamNotFoundException extends RuntimeException {
    public TeamNotFoundException() {
        super("Could not find the team");
    }
}