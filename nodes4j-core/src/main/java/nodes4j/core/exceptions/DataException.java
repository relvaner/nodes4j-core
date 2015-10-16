package nodes4j.core.exceptions;

public class DataException extends RuntimeException {
	protected static final long serialVersionUID = 4074182809583815660L;

	public DataException() {
		super("data is undefined");
	}
}
