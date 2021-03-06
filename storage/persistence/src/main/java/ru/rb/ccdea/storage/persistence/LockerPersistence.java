/**
 * 
 */
package ru.rb.ccdea.storage.persistence;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;

/**
 * Блокировщик
 * 
 * @author sotnik
 *
 */
public class LockerPersistence extends BasePersistence {

	/**
	 * Наименование типа: Блокировщик
	 */
	public static final String TYPE_NAME = "ccdea_locker";
	/**
	 * Уникальное наименование
	 */
	public static final String ATTR_UNIQUIE_NAME = "s_unique_name";
	/**
	 * Наименование набора разрешений для блокировщика
	 */
	public static final String BLOCKER_DEFAULT_ACL_NAME = "ccdea_blocker_acl";
	/**
	 * Наименование блокера для создания других блокеров
	 */
	public static final String DEFAULT_BLOCKER_NAME = "ccdea_default_blocker";

	/**
	 * Создать блокировщик
	 * 
	 * @param dfSession
	 *            - сессия Documentum
	 * @param lockerName
	 *            - уникальное наименование блокировщика
	 * @return блокировщик
	 * @throws DfException
	 */
	public static IDfSysObject getLocker(IDfSession dfSession, String lockerName) throws DfException {
		throwIfNotTransactionActive(dfSession);
		
		IDfSysObject locker = (IDfSysObject) dfSession
				.getObjectByQualification(TYPE_NAME + " where " + ATTR_UNIQUIE_NAME + " = '" + lockerName + "'");
		if(locker == null) {
			IDfSysObject defaultBlocker = null;
			if(!DEFAULT_BLOCKER_NAME.equals(lockerName)) {
				defaultBlocker = getLocker(dfSession, DEFAULT_BLOCKER_NAME);
			}
			if(defaultBlocker != null) {
				defaultBlocker.lock();
			}
			locker = (IDfSysObject) dfSession.newObject(TYPE_NAME);
			locker.setObjectName(lockerName);
			locker.setString(ATTR_UNIQUIE_NAME, lockerName);
			locker.setACL(getBlockerACL(dfSession));
			locker.save();
		}
		return locker;
	}

	/**
	 * Получить ACL для блокировщика
	 * 
	 * @param dfSession
	 *            - сессия Documentum
	 * @return ACL для блокировщика
	 * @throws DfException
	 */
	public static IDfACL getBlockerACL(IDfSession dfSession) throws DfException {
		throwIfNotTransactionActive(dfSession);
		
		IDfACL result = dfSession.getACL(dfSession.getDocbaseOwnerName(), BLOCKER_DEFAULT_ACL_NAME);
		if (result == null) {
			result = (IDfACL) dfSession.newObject("dm_acl");
			result.setObjectName(BLOCKER_DEFAULT_ACL_NAME);
			result.setDomain(dfSession.getDocbaseOwnerName());
			result.grant("docu", IDfACL.DF_PERMIT_DELETE, null);
			result.grant("dm_world", IDfACL.DF_PERMIT_WRITE, null);
			result.save();
		}
		return result;
	}

}
