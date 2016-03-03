package ru.rb.ccdea.storage.services.api;

import com.documentum.fc.client.IDfService;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;

public interface IDossierService extends IDfService {
    void closeDossier(IDfSession dfSession, IDfId dossierId) throws DfException;
    void reopenDossier(IDfSession dfSession, IDfId dossierId) throws DfException;
    void attachDoc(IDfSession dfSession, IDfId dossierId, IDfId docId) throws DfException;
}
