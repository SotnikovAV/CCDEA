package ru.rb.ccdea.storage.persistence;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;

public class BasePersistence {

    public static final String HOME_FOLDER = "/ucb/ccdea";

    public static final String SOURCE_SYSTEM_CCDEA = "CCDEA";
    public static final String SOURCE_SYSTEM_TBSVK = "TBSVK";
    public static final String SOURCE_SYSTEM_DVK = "DVK";
    public static final String SOURCE_SYSTEM_JETDOCER = "JETDOCER";

    public static void throwIfNotTransactionActive(IDfSession dfSession) throws DfException {
        if (!dfSession.isTransactionActive()) {
            throw new DfException("Transaction is not active");
        }
    }
}
