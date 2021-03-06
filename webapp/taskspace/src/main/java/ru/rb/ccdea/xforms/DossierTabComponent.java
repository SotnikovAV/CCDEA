package ru.rb.ccdea.xforms;

import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfUtil;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.Checkbox;
import com.documentum.web.formext.action.ActionService;
import com.documentum.web.formext.action.IActionCompleteListener;

import ru.rb.ccdea.config.TypeConfigProcessor;
import ru.rb.ccdea.search.DqlQueryBuilder;
import ru.rb.ccdea.search.SearchResultsComponent;
import ru.rb.ccdea.storage.persistence.ContentPersistence;
import ru.rb.ccdea.utils.StringUtils;

/**
 * Класс для закладки "Досье" карточки документа
 * Created by ER21595 on 25.06.2015.
 */
public class DossierTabComponent extends SearchResultsComponent {
	
	private static final String ARCHIVE_TYPES = "'rar','zip','arj', '7z'";
	
	private static final String printDossierDql =
//    		" select cont.r_object_id, cont.object_name, cont.r_content_size " 
//    		+ " from dm_sysobject#1 cont, dm_relation rel, ccdea_base_doc doc "
//    		+ " where rel.relation_name='ccdea_content_relation' "
//    		+ " and cont.i_chronicle_id = rel.child_id "
//    		+ " and rel.parent_id = doc.r_object_id "
//    		+ " and doc.id_dossier = '#2'"
//    		+ " and cont.a_content_type = 'pdf' and cont.r_content_size > 0 "
			" select distinct cont.r_object_id " 
    		+ " from ccdea_doc_content#1 cont, dm_relation rel, ccdea_base_doc doc "
    		+ " where rel.relation_name='ccdea_content_relation' "
    		+ " and cont.i_chronicle_id = rel.child_id "
    		+ " and rel.parent_id = doc.r_object_id "
    		+ " and doc.id_dossier = '#2'"
    		+ " and cont.b_is_original = true and cont.r_content_size > 0 "
    ;
	
    private String objectId;
    private String dossierId;
    private TypeConfigProcessor proc = TypeConfigProcessor.getInstance();

    @Override
    public void onInit(ArgumentList arg) {
        super.onInit(arg);
        objectId = arg.get("objectId");
        IDfCollection col = null;
        try{
            IDfQuery query = new DfQuery();
            query.setDQL("select id_dossier from ccdea_base_doc where r_object_id="+ DfUtil.toQuotedString(objectId));
            col= query.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
            if (col.next()){
                dossierId = col.getString("id_dossier");
            }
        } catch (DfException e) {
            DfLogger.error(this, e.getMessage(),null,e);
        }finally {
            try{
                if(col!=null){
                    col.close();
                }
            } catch (DfException e) {
                DfLogger.error(this, e.getMessage(), null, e);
            }
        }
        String fullQueryStr = StringUtils.joinStrings(proc.getTypeNames(), " UNION ALL ",
                new StringUtils.IWorker<String>() {
                    public String doWork(String param) {
                        return createSingleTypeQuery(param, true);
                    }
                });
        DfLogger.debug(this, fullQueryStr, null, null);
        refreshDatagrid(fullQueryStr);
    }


    private String createSingleTypeQuery(String typeName, boolean isSubquery){
        DqlQueryBuilder builder = new DqlQueryBuilder();
        builder.addSelectField(proc.getSelectFields(typeName, isSubquery));
        builder.addFromTables(proc.getFromTypes(typeName,isSubquery));
        builder.addGroupBy(proc.getGroupBy(typeName,isSubquery));
        for(String cond:proc.getConditions(typeName,isSubquery)){
            builder.addWhereClause(cond);
        }
        builder.addWhereClause(typeName + ".r_object_id!='" + objectId + "'");
        builder.addWhereClause(typeName + ".id_dossier='" + dossierId + "'");
        return builder.getQuery();
    }

    public void onClickPrint(Control c, ArgumentList arg) {
    	
//        PrintControl print = (PrintControl) getControl("print");
//        print.setSession(getDfSession());
        
        boolean printAllVersions = true;
        if(printAllVersions){
            Checkbox doPrintAll = (Checkbox) getControl("print_all_versions", Checkbox.class);
            printAllVersions = doPrintAll.getValue();
        }

//        List<String> contentIds = new ArrayList<String>();
//        List<String> contentNames = new ArrayList<String>();
//        List<String> contentSizes = new ArrayList<String>();

        IDfCollection col = null;
        try {
            IDfQuery query = new DfQuery();
            String dql = printDossierDql;
            dql = dql.replace("#1", printAllVersions?"(all)":"");
            dql = dql.replace("#2", dossierId);
            dql = dql.replace("#3", ARCHIVE_TYPES);
            query.setDQL(dql);
            col = query.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
            while (col.next()) {
            	final String objId = col.getString("r_object_id");
//                contentIds.add(col.getString("r_object_id"));
//                contentNames.add(col.getString("object_name"));
//                contentSizes.add(col.getString("r_content_size"));
            	ArgumentList actionArgs = new ArgumentList();
            	actionArgs.add("objectId", objId);
            	actionArgs.add("type", ContentPersistence.TYPE_NAME);
            	ActionService.execute("ucfview", actionArgs, getContext(), this, new IActionCompleteListener() {

					@Override
					public void onComplete(String s, boolean b, Map m) {
						DfLogger.debug(this, "Action 'ucfview' for content '" + objId + "' successfully done.", null,
								null);
					}
				});
            }
        } catch (DfException e) {
            DfLogger.error(this, e.getMessage(), null, e);
        } finally {
            if (col != null) {
                try {
                    col.close();
                } catch (DfException e) {
                    DfLogger.error(this, e.getMessage(), null, e);
                }
            }
        }
//        print.setObjectToPrint(StringUtils.joinStrings(contentIds, ","));
//        print.setContentNames(StringUtils.joinStrings(contentNames, ","));
//        print.setContentSizes(StringUtils.joinStrings(contentSizes, ","));
//        print.setContentCount(contentIds.size());
//
//        print.setPrint(true);
    }

    

}

