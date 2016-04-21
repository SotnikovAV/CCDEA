/**
 * 
 */
package ru.rb.ccdea.xforms;

import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;

import ru.rb.ccdea.search.SearchResultsComponent;

/**
 * @author SotnikovAV
 *
 */
public class FilesTabComponent extends SearchResultsComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private String objectId;

	@Override
	public void onInit(ArgumentList arg) {
		super.onInit(arg);
		objectId = arg.get("objectId");
		String filesQueryStr = "select "
				+ " r_object_id, object_name, s_content_source_id, s_content_source_code, "
				+ " r_version_label, r_creation_date, r_modify_date, a_content_type, r_content_size "
				+ " , r_object_type, r_link_cnt, r_is_virtual_doc, r_assembled_from_id, r_has_frzn_assembly, i_is_replica, i_is_reference "
				+ " from "
				+ " ccdea_doc_content (all) where r_object_id in ("
				+ " select child_id from dm_relation where relation_name='ccdea_content_relation' and parent_id='"
				+ objectId + "') and b_is_original=true ";
		DfLogger.debug(this, filesQueryStr, null, null);
		refreshDatagrid(filesQueryStr);
	}
}
