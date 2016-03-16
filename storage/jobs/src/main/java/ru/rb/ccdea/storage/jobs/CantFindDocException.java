/**
 * 
 */
package ru.rb.ccdea.storage.jobs;

import com.documentum.fc.common.DfException;

/**
 * Исключение "Не найден документ"
 * 
 * @author SotnikovAV
 *
 */
public class CantFindDocException extends DfException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CantFindDocException(String msg) {
		super(msg);
	}

}
