package ru.rb.ccdea.storage.persistence.ctsutils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.w3c.dom.Node;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.services.cts.df.transform.ICTSAddJobService;
import com.documentum.services.cts.df.transform.ICTSGetJobService;
import com.documentum.services.cts.df.transform.ICTSRequest;
import com.documentum.services.dam.df.transform.IMediaProfile;
import com.documentum.services.dam.df.transform.IParameterContent;
import com.documentum.services.dam.df.transform.IParameterContentAttribute;
import com.documentum.services.dam.df.transform.IProfileParameter;

public class CTSRequestBuilder {

	public static final String CONVERT_TO_PDF_PROFILE_NAME = "convert_to_pdf";
    public static final String MERGE_PDF_PROFILE_NAME = "mergePDF_adts";
    public static final String RESPONSE_STATUS_COMPLITED = "Completed";
    public static final String RESPONSE_STATUS_FAILED = "Failed";
	public static final String RESPONSE_STATUS_TIMEOUT = "Timeout";
	public static final long CTS_JOB_WAITING_TIMEOUT = 180;

    public static String convertToPdfRequest(IDfSession dfSession, String targetId, boolean createNewVersion, IDfSysObject sourceObject) throws DfException{
        return transformRequest(dfSession, CONVERT_TO_PDF_PROFILE_NAME, targetId, createNewVersion, sourceObject, null, null, null, null, null, true);
    }
    
    public static String mergePdfRequest(IDfSession dfSession, String targetId, boolean createNewVersion, IDfSysObject sourceObject, IDfSysObject additionalObject) throws DfException{
        return transformRequest(dfSession, MERGE_PDF_PROFILE_NAME, targetId, createNewVersion, sourceObject, additionalObject, null, null, null, null, true);
    }
    
    public static String convertToPdfRequest(IDfSession dfSession, String targetId, boolean createNewVersion, IDfSysObject sourceObject, boolean async) throws DfException{
        return transformRequest(dfSession, CONVERT_TO_PDF_PROFILE_NAME, targetId, createNewVersion, sourceObject, null, null, null, null, null, async);
    }
    
    public static String mergePdfRequest(IDfSession dfSession, String targetId, boolean createNewVersion, IDfSysObject sourceObject, IDfSysObject additionalObject, boolean async) throws DfException{
        return transformRequest(dfSession, MERGE_PDF_PROFILE_NAME, targetId, createNewVersion, sourceObject, additionalObject, null, null, null, null, async);
    }
    
	public static String transformRequest(IDfSession dfSession, String transformProfileName, String targetId, boolean createNewVersion,
			IDfSysObject sourceObject, IDfSysObject additionalObject1, IDfSysObject additionalObject2,
			IDfSysObject additionalObject3, IDfSysObject additionalObject4, IDfSysObject additionalObject5, boolean async)
					throws DfException {
		if (sourceObject.getContentSize() == 0) {
			throw new DfException("Указанный объект (" + sourceObject.getObjectId().getId() + ") не имеет контента.");
		}

		ICTSAddJobService ctsAddJobService = getCtsAddJobService(dfSession);

		IMediaProfile profile = (IMediaProfile) dfSession
				.getObjectByQualification(String.format("dm_media_profile where object_name='%1$s'", transformProfileName));
		if (profile == null) {
			throw new DfException("Media profile not found: " + transformProfileName);
		}
		IProfileParameter[] params = profile.getParameters();
		for (IProfileParameter param : params) {
			if (param.getParameterName().startsWith("doc_token_contentObjectId")) {
				int contentNumber = -1;
				try {
					contentNumber = Integer.parseInt(
							String.valueOf(param.getParameterName().charAt(param.getParameterName().length() - 1)));
				} catch (NumberFormatException e) {
				}
				if (contentNumber == 1 && additionalObject1 != null && additionalObject1.getContentSize() > 0) {
					param.setContentObjects(createParameterContentArray(additionalObject1.getObjectId().getId(),
							additionalObject1.getContentType()));
				} else if (contentNumber == 2 && additionalObject2 != null && additionalObject2.getContentSize() > 0) {
					param.setContentObjects(createParameterContentArray(additionalObject2.getObjectId().getId(),
							additionalObject3.getContentType()));
				} else if (contentNumber == 2 && additionalObject3 != null && additionalObject3.getContentSize() > 0) {
					param.setContentObjects(createParameterContentArray(additionalObject3.getObjectId().getId(),
							additionalObject3.getContentType()));
				} else if (contentNumber == 4 && additionalObject4 != null && additionalObject4.getContentSize() > 0) {
					param.setContentObjects(createParameterContentArray(additionalObject4.getObjectId().getId(),
							additionalObject4.getContentType()));
				} else if (contentNumber == 5 && additionalObject5 != null && additionalObject5.getContentSize() > 0) {
					param.setContentObjects(createParameterContentArray(additionalObject5.getObjectId().getId(),
							additionalObject5.getContentType()));
				}
			} else {
				String value = ProfileProperties.getValue(param.getParameterName());
				if (value != null && !value.trim().isEmpty()) {
					param.setParameterValue(value);
				}
			}
		}

		ICTSRequest transformRequest = ctsAddJobService.getNewCTSRequest(dfSession.getDocbaseName());
		transformRequest.setParameters(params);
		transformRequest.setSourceObjectId(sourceObject.getObjectId().getId());
		transformRequest.setMediaProfileId(profile.getObjectId().getId());
		transformRequest.setMediaProfileName(profile.getObjectName());
		transformRequest.setSourceFormat(sourceObject.getContentType());
		transformRequest.setTargetFormat("pdf");
		transformRequest.setSourcePageModifier("");
		transformRequest.setTargetPageModifier("");
		transformRequest.setLocale(Locale.getDefault());
		transformRequest.setPriority(1);
		transformRequest.setSourcePage(0);
		transformRequest.setTargetPage(0);
		transformRequest.setTargetFolder("/Temp");
		if(async) {
			transformRequest.setTransformationType("asynchronous");
		}
		transformRequest.setRelatedObjectId(targetId);
		transformRequest.setNotifyUser(false);
		transformRequest.setDeleteOnCompletion(false);
		transformRequest.setVersionUp(createNewVersion);

		transformRequest.save();

		DfLogger.info(sourceObject,
				"Отправляем на трансформацию объект SourceObjectId = '" + sourceObject.getObjectId().getId()
						+ "', MediaProfileId = '" + profile.getObjectId().getId() + "', SourceFormat = '"
						+ sourceObject.getContentType()
						+ "', TargetFormat = 'pdf', SourcePageModifier = '', TargetPageModifier = '', Locale = '"
						+ Locale.getDefault()
						+ ", Priority = 1, SourcePage = 0, TargetPage = 0, TargetFolder = '/Temp', RelatedObjectId = '"
						+ targetId + "', NotifyUser = false, DeleteOnCompletion = false, VersionUp = "
						+ createNewVersion + ", MediaProfileName = " + transformRequest.getMediaProfileName(),
				null, null);

		return ctsAddJobService.addJob(transformRequest);
	}

    public static String getCTSResponseIfFinished(IDfSession dfSession, String jobId) throws DfException {
        ICTSGetJobService ctsGetJobService = getCtsGetJobService(dfSession);
        String jobStatus = ctsGetJobService.getJobStatus(jobId);
        if (RESPONSE_STATUS_COMPLITED.equalsIgnoreCase(jobStatus) ||
                RESPONSE_STATUS_FAILED.equalsIgnoreCase(jobStatus)) {
            return jobStatus;
        }
        else {
            return null;
        }
    }

    protected static IParameterContent[] createParameterContentArray(String contentId, String contentFormat) throws DfException{
        List<ParameterContent> parameterContentList = new ArrayList<ParameterContent>();

        ParameterContent parameterContent = new ParameterContent();
        ParameterContentAttribute idAttribute = new ParameterContentAttribute();
        idAttribute.setName(IParameterContentAttribute.CONTENT_OBJECT_ID_NAME);
        idAttribute.setToken(IParameterContentAttribute.CONTENT_OBJECT_ID_TOKEN);
        idAttribute.setValue(contentId);
        parameterContent.addParameterContentAttribute(idAttribute);

        ParameterContentAttribute formatAttribute = new ParameterContentAttribute();
        formatAttribute.setName(IParameterContentAttribute.CONTENT_FORMAT_NAME);
        formatAttribute.setToken(IParameterContentAttribute.CONTENT_FORMAT_TOKEN);
        formatAttribute.setValue(contentFormat);
        parameterContent.addParameterContentAttribute(formatAttribute);

        ParameterContentAttribute pageModifierAttribute = new ParameterContentAttribute();
        pageModifierAttribute.setName(IParameterContentAttribute.CONTENT_PAGE_MODIFIER_NAME);
        pageModifierAttribute.setToken(IParameterContentAttribute.CONTENT_PAGE_MODIFIER_TOKEN);
        pageModifierAttribute.setValue("");
        parameterContent.addParameterContentAttribute(pageModifierAttribute);

        ParameterContentAttribute pageAttribute = new ParameterContentAttribute();
        pageAttribute.setName(IParameterContentAttribute.CONTENT_PAGE_NAME);
        pageAttribute.setToken(IParameterContentAttribute.CONTENT_PAGE_TOKEN);
        pageAttribute.setValue("");
        parameterContent.addParameterContentAttribute(pageAttribute);

        ParameterContentAttribute filePathAttribute = new ParameterContentAttribute();
        filePathAttribute.setName(IParameterContentAttribute.CONTENT_FILE_PATH_NAME);
        filePathAttribute.setToken(IParameterContentAttribute.CONTENT_FILE_PATH_TOKEN);
        filePathAttribute.setValue("");
        parameterContent.addParameterContentAttribute(filePathAttribute);

        parameterContentList.add(parameterContent);

        return parameterContentList.toArray(new IParameterContent[parameterContentList.size()]);
    }

    protected static ICTSAddJobService getCtsAddJobService(IDfSession session) throws DfException {
        try {
            return (ICTSAddJobService)session.getClient().newService(ICTSAddJobService.class.getName(), session.getSessionManager());
        } catch (Throwable e) {
            throw new DfException("CTS AddJob is not available", e);
        }
    }

    protected static ICTSGetJobService getCtsGetJobService(IDfSession session) throws DfException {
        try {
            return (ICTSGetJobService)session.getClient().newService(ICTSGetJobService.class.getName(), session.getSessionManager());
        } catch (Throwable e) {
            throw new DfException("CTS GetJob is not available", e);
        }
    }

    static class ParameterContent implements IParameterContent {
        private List<IParameterContentAttribute> attributeList = new ArrayList<IParameterContentAttribute>();

        public void addParameterContentAttribute(IParameterContentAttribute attribute) {
            attributeList.add(attribute);
        }

        @Override
        public IParameterContentAttribute[] getParameterContentAttributes() {
            IParameterContentAttribute[] array = new IParameterContentAttribute[attributeList.size()];
            return attributeList.toArray(array);
        }

        @Override
        public boolean validate() throws DfException {
            return true;
        }

        @Override
        public void addToXMLNode(Node node) {
        }
    }

    static class ParameterContentAttribute implements IParameterContentAttribute {
        private String name;
        private String value;
        private String token;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String getToken() {
            return token;
        }

        @Override
        public void setName(String s) {
            name = s;
        }

        @Override
        public void setValue(String s) {
            value = s;
        }

        @Override
        public void setToken(String s) {
            token = s;
        }
    }

    enum ProfileProperties {
        doc_token_changeDefaultSettings("Yes"),
        doc_token_showRelationship("No"),
        doc_token_enableHyperlinks("No"),
        doc_token_resolution("150"),
        doc_token_enableSecurity("No"),
        doc_token_toc("No"),
        doc_token_encryptionMode("40bit"),
        doc_token_enableBookMarks("No");

        private final String value;
        ProfileProperties(String value) {
            this.value = value;
        }

        public static String getValue(String name) {
            ProfileProperties profileProperties = null;
            try {
                profileProperties = valueOf(name);
            } catch (IllegalArgumentException e) {
                return null;
            }
            return profileProperties.value;
        }
    }
    
	public static String waitForJobComplete(IDfSession dfSession, String transformJobId) throws DfException {
		long counter = 0;
		String status = "";
		while (!CTSRequestBuilder.RESPONSE_STATUS_COMPLITED.equals(status)
				&& !CTSRequestBuilder.RESPONSE_STATUS_FAILED.equals(status)
				&& !CTSRequestBuilder.RESPONSE_STATUS_TIMEOUT.equals(status)) {
			IDfPersistentObject response = dfSession.getObject(new DfId(transformJobId));
			status = response.getString("job_status");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			counter++;
			if(CTS_JOB_WAITING_TIMEOUT == counter) {
				status = CTSRequestBuilder.RESPONSE_STATUS_TIMEOUT;
				throw new DfException("Transform waiting timeout.");
			}
		}
		return status;
	}
	
	
}
