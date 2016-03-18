package ru.rb.ccdea.storage.persistence;

import com.documentum.fc.client.*;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfId;
import ru.rb.ccdea.storage.persistence.details.DossierKeyDetails;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DossierPersistence extends BasePersistence {

	protected static final String TYPE_NAME = "ccdea_dossier";

	protected static final String ATTR_ARCHIVE_NUMBER = "n_archive_number";
	protected static final String ATTR_ARCHIVE_YEAR = "n_archive_year";
	protected static final String ATTR_CLOSE_DATE = "t_close_date";
	protected static final String ATTR_ARCHIVE_DATE = "t_archive_date";
	protected static final String ATTR_STATE = "s_state";
	protected static final String ATTR_FULL_ARCHIVE_NUMBER = "s_archive_number";
	public static final String ATTR_DOSSIER_TYPE = "s_dossier_type";
	protected static final String ATTR_CUSTOMER_NUMBER = "s_customer_number";
	public static final String ATTR_PASSPORT_NUMBER = "s_passport_number";
	protected static final String ATTR_CONTRACT_NUMBER = "s_contract_number";
	protected static final String ATTR_CONTRACT_DATE = "t_contract_date";
	protected static final String ATTR_BRANCH_CODE = "s_reg_branch_code";
	protected static final String ATTR_DOSSIER_TYPE_DOC = "id_dossier_type_doc";
	protected static final String ATTR_CREATION_DATE = "t_creation_date";
	public static final String ATTR_CHANGE_AUTHOR = "s_change_author";
	
	public static final String DOSSIER_TYPE_PASSPORT = "PASSPORT";
	public static final String DOSSIER_TYPE_CONTRACT = "CONTRACT";
	public static final String DOSSIER_TYPE_CONTRACT_PREFIX = "БП";

	public static final String STATE_OPENED = "Открыто";
	public static final String STATE_CLOSED = "Закрыто";

	protected static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

	public static String getFullArchiveNumber(String prefix, int number, int year) {
		String numStr = String.valueOf(number);
		while (numStr.length() < 4) {
			numStr = "0" + numStr;
		}
		String yy = String.valueOf((year % 100));
		if (DOSSIER_TYPE_CONTRACT_PREFIX.equals(prefix)) {
			return prefix + '/' + yy + '/' + numStr;
		} else {
			return yy + '/' + prefix + '/' + numStr;
		}
	}

	public static String getDossierDescription(IDfPersistentObject dossier) throws DfException {
		String result = "Досье ";
		if (DOSSIER_TYPE_CONTRACT.equalsIgnoreCase(dossier.getString(ATTR_DOSSIER_TYPE))) {
			result += "по договору ";
			result += dossier.getString(ATTR_CONTRACT_NUMBER);
			result += " от ";
			result += dateFormat.format(dossier.getTime(ATTR_CONTRACT_DATE).getDate());
		} else if (DOSSIER_TYPE_PASSPORT.equalsIgnoreCase(dossier.getString(ATTR_DOSSIER_TYPE))) {
			result += "по ПС " + dossier.getString(ATTR_PASSPORT_NUMBER);
		} else {
			result += "неизвестного типа";
		}
		result += " (Номер клиента: " + dossier.getString(ATTR_CUSTOMER_NUMBER) + ")";
		return result;
	}

	@Deprecated
	public static String closeDossierWithNumber(IDfPersistentObject dossier, String dossierPrefix, int number, int year) throws DfException {
		throwIfNotTransactionActive(dossier.getSession());
		
		String fullArchiveNumber = getFullArchiveNumber(dossierPrefix, number, year);
				
		Date closeDate = new Date();
		dossier.setInt(ATTR_ARCHIVE_NUMBER, number);
		dossier.setInt(ATTR_ARCHIVE_YEAR, year);
		dossier.setTime(ATTR_CLOSE_DATE, new DfTime(closeDate));
		dossier.setTime(ATTR_ARCHIVE_DATE, new DfTime(closeDate));
		dossier.setString(ATTR_STATE, STATE_CLOSED);
		dossier.setString(ATTR_FULL_ARCHIVE_NUMBER, fullArchiveNumber);
		dossier.save();

		return fullArchiveNumber;
	}

	public static void checkDossierForClosing(IDfPersistentObject dossier) throws DfException {
		throwIfNotTransactionActive(dossier.getSession());

		String dossierState = dossier.getString(ATTR_STATE);
		if (STATE_CLOSED.equalsIgnoreCase(dossierState)) {
			throw new DfException("Wrong dossier state for closing: " + dossierState);
		}
	}

	public static void checkDossierForAttachDoc(IDfPersistentObject dossier) throws DfException {
		throwIfNotTransactionActive(dossier.getSession());

		String dossierState = dossier.getString(ATTR_STATE);
		if (STATE_CLOSED.equalsIgnoreCase(dossierState)) {
			throw new DfException("Wrong dossier state for attach doc: " + dossierState);
		}
	}

	public static final String getDossierPrefix(IDfPersistentObject dossier) throws DfException {
		String dossierPrefix = DossierPersistence.DOSSIER_TYPE_CONTRACT_PREFIX;
		String dossierType = dossier.getString(DossierPersistence.ATTR_DOSSIER_TYPE);
		if (DossierPersistence.DOSSIER_TYPE_PASSPORT.equals(dossierType)) {
			String passportNumber = dossier.getString(DossierPersistence.ATTR_PASSPORT_NUMBER);
			if (passportNumber != null && !passportNumber.trim().isEmpty()) {
				if (passportNumber.trim().length() > 19)
					dossierPrefix = String.valueOf(passportNumber.charAt(19));
			}
		}
		return dossierPrefix;
	}

	@Deprecated
	public static void reopenDossier(IDfPersistentObject dossier) throws DfException {
		throwIfNotTransactionActive(dossier.getSession());

		dossier.setInt(ATTR_ARCHIVE_NUMBER, 0);
		dossier.setInt(ATTR_ARCHIVE_YEAR, 0);
		dossier.setTime(ATTR_CLOSE_DATE, DfTime.DF_NULLDATE);
		dossier.setTime(ATTR_ARCHIVE_DATE, DfTime.DF_NULLDATE);
		dossier.setString(ATTR_STATE, STATE_OPENED);
		dossier.setString(ATTR_FULL_ARCHIVE_NUMBER, "");
		dossier.save();
	}

	public static void checkDossierForReopen(IDfPersistentObject dossier) throws DfException {
		throwIfNotTransactionActive(dossier.getSession());

		String dossierState = dossier.getString(ATTR_STATE);
		if (STATE_OPENED.equalsIgnoreCase(dossierState)) {
			throw new DfException("Wrong dossier state for reopen: " + dossierState);
		}
	}

	public static int searchFirstFreeNumber(IDfSession dfSession, int year, String dossierPrefix) throws DfException {
		return DossierNumeratorPersistence.getNextNumber(dfSession, year, dossierPrefix);
	}

	public static DossierKeyDetails getKeyDetails(IDfPersistentObject dossier) throws DfException {
		DossierKeyDetails result = new DossierKeyDetails();
		result.dossierId = dossier.getObjectId().getId();
		result.branchCode = dossier.getString(ATTR_BRANCH_CODE);
		result.customerNumber = dossier.getString(ATTR_CUSTOMER_NUMBER);
		result.passportNumber = dossier.getString(ATTR_PASSPORT_NUMBER);
		result.contractNumber = dossier.getString(ATTR_CONTRACT_NUMBER);
		result.contractDate = dossier.getTime(ATTR_CONTRACT_DATE).getDate();
		return result;
	}

	public static boolean isDossierTypeDoc(IDfPersistentObject dossier, IDfId docId) throws DfException {
		return docId.equals(dossier.getId(ATTR_DOSSIER_TYPE_DOC));
	}

	/**
	 * Найти досье по ключевой информации
	 * 
	 * @param dfSession
	 *            - сессия Documentum
	 * @param keyDetails
	 *            - ключевая информация по досье
	 * @return досье или null, если досье не найдено
	 * @throws DfException
	 */
	public static IDfPersistentObject getDossierByKeyDetails(IDfSession dfSession, DossierKeyDetails keyDetails)
			throws DfException {
		String dossierQualification = TYPE_NAME + " where " + ATTR_BRANCH_CODE + " = '" + keyDetails.branchCode + "'"
				+ " and " + ATTR_CUSTOMER_NUMBER + " = '" + keyDetails.customerNumber + "'";
		if (keyDetails.passportNumber != null && !keyDetails.passportNumber.trim().isEmpty()) {
			dossierQualification += " and " + ATTR_PASSPORT_NUMBER + " = '" + keyDetails.passportNumber + "'" + " and "
					+ ATTR_DOSSIER_TYPE + " = '" + DOSSIER_TYPE_PASSPORT + "'";
		} else if (keyDetails.contractNumber != null && !keyDetails.contractNumber.trim().isEmpty()
				&& keyDetails.contractDate != null) {
			dossierQualification += " and " + ATTR_CONTRACT_NUMBER + " = '" + keyDetails.contractNumber + "'" + " and "
					+ ATTR_CONTRACT_DATE + " = date('" + dateFormat.format(keyDetails.contractDate) + "','dd.mm.yyyy')"
					+ " and " + ATTR_DOSSIER_TYPE + " = '" + DOSSIER_TYPE_CONTRACT + "'";
		} else {
			throw new DfException("Cant get or create dossier, key fields is empty." + keyDetails);
		}
		DfLogger.debug(dfSession, "Search dossier: " + dossierQualification, null, null);
		IDfPersistentObject dossierObject = dfSession.getObjectByQualification(dossierQualification);
		DfLogger.debug(dfSession, "Dossier: " + (dossierObject == null ? "not found" : dossierObject.getObjectId()),
				null, null);
		return dossierObject;
	}

	/**
	 * Найти или создать новое досье по ключевой информации
	 * 
	 * @param dfSession
	 *            - сессия Documentum
	 * @param keyDetails
	 *            - ключевая информация по досье
	 * @return досье или null, если досье не найдено
	 * @throws DfException
	 */
	public static IDfPersistentObject getOrCreateDossierByKeyDetails(IDfSession dfSession, DossierKeyDetails keyDetails)
			throws DfException {
		throwIfNotTransactionActive(dfSession);

		IDfPersistentObject dossierObject = getDossierByKeyDetails(dfSession, keyDetails);
		if (dossierObject == null) {
			dossierObject = dfSession.newObject(TYPE_NAME);
			dossierObject.setString(ATTR_STATE, STATE_OPENED);
			dossierObject.setTime(ATTR_CREATION_DATE, new DfTime(new Date()));
			dossierObject.setString(ATTR_BRANCH_CODE, keyDetails.branchCode);
			dossierObject.setString(ATTR_CUSTOMER_NUMBER, keyDetails.customerNumber);
			
			if (keyDetails.passportNumber != null && !keyDetails.passportNumber.trim().isEmpty()) {
				dossierObject.setString(ATTR_DOSSIER_TYPE, DOSSIER_TYPE_PASSPORT);
				dossierObject.setString(ATTR_PASSPORT_NUMBER, keyDetails.passportNumber);
				
			} else {
				dossierObject.setString(ATTR_DOSSIER_TYPE, DOSSIER_TYPE_CONTRACT);
				dossierObject.setString(ATTR_CONTRACT_NUMBER, keyDetails.contractNumber);
				dossierObject.setTime(ATTR_CONTRACT_DATE, new DfTime(keyDetails.contractDate));
			}
			dossierObject.save();
		}

		return dossierObject;
	}

	public static boolean isClosed(IDfPersistentObject dossier) throws DfException {
		String dossierState = dossier.getString(ATTR_STATE);
		return STATE_CLOSED.equalsIgnoreCase(dossierState);
	}
	
	public static boolean isOpened(IDfPersistentObject dossier) throws DfException {
		String dossierState = dossier.getString(ATTR_STATE);
		return STATE_OPENED.equalsIgnoreCase(dossierState);
	}

	public static String closeDossier(IDfPersistentObject dossier, Date closeDate, String changeAuthor) throws DfException {
		throwIfNotTransactionActive(dossier.getSession());
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		int year = calendar.get(Calendar.YEAR);
		String dossierPrefix = DossierPersistence.getDossierPrefix(dossier);
		int freeNumber = DossierPersistence.searchFirstFreeNumber(dossier.getSession(), year, dossierPrefix);
		return DossierPersistence.closeDossierWithNumber(dossier, dossierPrefix, freeNumber, year, closeDate, changeAuthor);
	}
	
	public static String closeDossierWithNumber(IDfPersistentObject dossier, String dossierPrefix, int number, int year, Date closeDate, String changeAuthor) throws DfException {
		throwIfNotTransactionActive(dossier.getSession());
		
		String fullArchiveNumber = getFullArchiveNumber(dossierPrefix, number, year);
				
		Date archiveDate = new Date();
		dossier.setInt(ATTR_ARCHIVE_NUMBER, number);
		dossier.setInt(ATTR_ARCHIVE_YEAR, year);
		dossier.setTime(ATTR_CLOSE_DATE, new DfTime(closeDate));
		dossier.setTime(ATTR_ARCHIVE_DATE, new DfTime(archiveDate));
		dossier.setString(ATTR_STATE, STATE_CLOSED);
		dossier.setString(ATTR_FULL_ARCHIVE_NUMBER, fullArchiveNumber);
		dossier.setString(ATTR_CHANGE_AUTHOR, changeAuthor);
		dossier.save();

		return fullArchiveNumber;
	}
	
	public static void reopenDossier(IDfPersistentObject dossier, String changeAuthor) throws DfException {
		throwIfNotTransactionActive(dossier.getSession());

		dossier.setInt(ATTR_ARCHIVE_NUMBER, 0);
		dossier.setInt(ATTR_ARCHIVE_YEAR, 0);
		dossier.setTime(ATTR_CLOSE_DATE, DfTime.DF_NULLDATE);
		dossier.setTime(ATTR_ARCHIVE_DATE, DfTime.DF_NULLDATE);
		dossier.setString(ATTR_STATE, STATE_OPENED);
		dossier.setString(ATTR_FULL_ARCHIVE_NUMBER, "");
		dossier.setString(ATTR_CHANGE_AUTHOR, changeAuthor);
		dossier.save();
	}
}
