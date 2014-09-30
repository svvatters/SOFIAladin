package cds.aladin;

/**
 * @author swatters@gmail.com
 *
 */
@SuppressWarnings("serial")
public final class UserCanceledException extends Exception {
	public UserCanceledException() {
        super();
    }	
	public UserCanceledException(String message) {
        super(message);
    }
    public UserCanceledException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
