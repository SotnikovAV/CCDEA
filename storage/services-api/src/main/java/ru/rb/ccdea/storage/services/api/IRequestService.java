package ru.rb.ccdea.storage.services.api;

import java.util.Date;

import com.documentum.fc.client.IDfService;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;
import ru.rb.ccdea.adapters.mq.binding.request.MCDocInfoModifyZAType;

/**
 * Сервис для обработки документов "Заявка"
 * 
 * @author SotnikovAV
 *
 */
public interface IRequestService extends IDfService {

	/**
	 * Создать карточку документа "Заявка"
	 * 
	 * @param dfSession
	 *            - сессия Documentum
	 * @param documentXmlObject
	 *            - сообщение
	 * @param docSourceCode
	 *            - код внешней системы, откуда пришло сообщение
	 * @param docSourceId
	 *            - идентификатор документа во внешней системе
	 * @param passportNumber
	 *            - номер паспорта
	 * @return идентификатор карточки документа "Заявка"
	 * @throws DfException
	 */
	String createDocumentFromMQType(IDfSession dfSession, MCDocInfoModifyZAType documentXmlObject, String docSourceCode,
			String docSourceId, String passportNumber) throws DfException;

	/**
	 * Создать карточку документа "Заявка"
	 * 
	 * @param dfSession
	 *            - сессия Documentum
	 * @param documentXmlObject
	 *            - сообщение
	 * @param docSourceCode
	 *            - код внешней системы, откуда пришло сообщение
	 * @param docSourceId
	 *            - идентификатор документа во внешней системе
	 * @param contractNumber
	 *            - номер контракта
	 * @param contractDate
	 *            - дата контракта
	 * @return идентификатор карточки документа "Заявка"
	 * @throws DfException
	 */
	String createDocumentFromMQType(IDfSession dfSession, MCDocInfoModifyZAType documentXmlObject, String docSourceCode,
			String docSourceId, String contractNumber, Date contractDate) throws DfException;

	/**
	 * Обновить карточку документа "Заявка"
	 * 
	 * @param dfSession
	 *            - сессия Documentum
	 * @param documentXmlObject
	 *            - сообщение
	 * @param docSourceCode
	 *            - код внешней системы, откуда пришло сообщение
	 * @param docSourceId
	 *            - идентификатор документа во внешней системе
	 * @param passportNumber
	 *            - номер контракта
	 * @param documentId
	 *            - идентификатор карточку документа "Заявка"
	 * @throws DfException
	 */
	void updateDocumentFromMQType(IDfSession dfSession, MCDocInfoModifyZAType documentXmlObject, String docSourceCode,
			String docSourceId, String passportNumber, IDfId documentId) throws DfException;

	/**
	 * Обновить карточку документа "Заявка"
	 * 
	 * @param dfSession
	 *            - сессия Documentum
	 * @param documentXmlObject
	 *            - сообщение
	 * @param docSourceCode
	 *            - код внешней системы, откуда пришло сообщение
	 * @param docSourceId
	 *            - идентификатор документа во внешней системе
	 * @param contractNumber
	 *            - номер контракта
	 * @param contractDate
	 *            - дата контракта
	 * @param documentId
	 *            - идентификатор карточку документа "Заявка"
	 * @throws DfException
	 */
	void updateDocumentFromMQType(IDfSession dfSession, MCDocInfoModifyZAType documentXmlObject, String docSourceCode,
			String docSourceId, String contractNumber, Date contractDate, IDfId documentId) throws DfException;
}
