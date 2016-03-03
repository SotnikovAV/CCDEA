package ru.rb.ccdea.storage.persistence;

import com.documentum.fc.client.*;
import com.documentum.fc.common.DfException;

public class DossierNumeratorPersistence extends BasePersistence {

	protected static final String TYPE_NAME = "ccdea_dossier_numerator";

	protected static final String ATTR_LAST_NUMBER = "n_last_number";
	protected static final String ATTR_YEAR = "n_year";
	protected static final String ATTR_DOSSIER_PREFIX = "s_dossier_prefix";

	public static final synchronized IDfPersistentObject getNumerator(IDfSession dfSession, int year,
			String dossierPrefix) throws DfException {
		throwIfNotTransactionActive(dfSession);

		IDfPersistentObject numeratorObject = dfSession.getObjectByQualification(TYPE_NAME + " where " + ATTR_YEAR
				+ " = " + year + " and " + ATTR_DOSSIER_PREFIX + " = '" + dossierPrefix + "'");
		if (numeratorObject == null) {
			numeratorObject = dfSession.newObject(TYPE_NAME);
			numeratorObject.setInt(ATTR_YEAR, year);
			numeratorObject.setString(ATTR_DOSSIER_PREFIX, dossierPrefix);
			numeratorObject.setInt(ATTR_LAST_NUMBER, 0);
			numeratorObject.save();
		}
		return numeratorObject;
	}

	public static final int getNextNumber(IDfSession dfSession, int year, String dossierPrefix) throws DfException {
		throwIfNotTransactionActive(dfSession);

		IDfPersistentObject numeratorObject = getNumerator(dfSession, year, dossierPrefix);
		numeratorObject.lock();
		int lastNumber = numeratorObject.getInt(ATTR_LAST_NUMBER);
		int nextNumber = lastNumber + 1;
		numeratorObject.setInt(ATTR_LAST_NUMBER, nextNumber);
		numeratorObject.save();
		return nextNumber;
	}

}
