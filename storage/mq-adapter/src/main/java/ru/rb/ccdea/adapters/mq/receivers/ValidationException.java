/**
 * 
 */
package ru.rb.ccdea.adapters.mq.receivers;

/**
 * @author ER19391
 *
 */
public class ValidationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ValidationException() {
		super();
	}
	
	public ValidationException(Throwable ex) {
		super(ex);
	}

	public ValidationException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public ValidationException(String msg) {
		super(msg);
	}

}
