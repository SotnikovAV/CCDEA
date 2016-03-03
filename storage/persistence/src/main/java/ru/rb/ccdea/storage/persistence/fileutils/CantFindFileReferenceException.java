/**
 * 
 */
package ru.rb.ccdea.storage.persistence.fileutils;

import com.documentum.fc.common.DfException;;

/**
 * @author sotnik
 *
 */
public class CantFindFileReferenceException extends DfException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public CantFindFileReferenceException(String msg) {
		super(msg);
	}

}
