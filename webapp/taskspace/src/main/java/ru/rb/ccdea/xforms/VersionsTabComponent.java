package ru.rb.ccdea.xforms;

import com.documentum.web.common.ArgumentList;
import ru.rb.ccdea.search.SearchResultsComponent;

/**
 * Created by ER21595 on 30.06.2015.
 */
public class VersionsTabComponent extends SearchResultsComponent {
    private String objectId;

    @Override
    public void onInit(ArgumentList arg) {
        super.onInit(arg);
        objectId = arg.get("objectId");
        String dql = getConfigLookup().lookupElement(getConfigBasePath(),getContext()).getChildValue("query");
        dql = dql.replace("#objectId", objectId);
        refreshDatagrid(dql);
    }
}
