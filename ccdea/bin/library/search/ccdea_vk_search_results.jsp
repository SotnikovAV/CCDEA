<%@ page import="com.documentum.web.form.Form" %>
<%@ page import="ru.rb.ccdea.search.SearchResultsComponent" %>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ taglib uri="/WEB-INF/tlds/ccdea_filters.tld" prefix="ccdea" %>
<dmf:html>
<dmf:head>
<dmf:webform/>
</dmf:head>
<% SearchResultsComponent component = (SearchResultsComponent)pageContext.getAttribute(Form.FORM, PageContext.REQUEST_SCOPE);%>
<dmf:body>
<dmf:form>
    <dmf:datagrid rowselection="true" name="<%= SearchResultsComponent.OBJECT_LIST_GRID %>" paged="true" pagesize="5"
            preservesort="true" cssclass="doclistbodyDatagrid" width="100%" cellspacing="0" cellpadding="0" bordersize="0">
        <tr><td colspan='30' class="pagerBackground">
                <table width='100%'><tr>
                     <td align='center' width='100%'><dmf:datapaging name="pager1" gotopageclass="doclistPager"/></td>
                     <td class="rightAlignment" nowrap valign='middle'><dmf:label nlsid='MSG_SHOW_ITEMS'/>&nbsp;</td>
                     <td valign='middle' nowrap> <dmf:datapagesize name='sizer' preference='application.display.classic'
                            pagesizevalues='5,50,100' tooltipnlsid='MSG_SHOW_ITEMS'/>&nbsp;</td>
                </tr></table>
        </td></tr>
        <tr class="colHeaderBackground">
            <dmf:datagridTh scope='col' cssclass='doclistcheckbox'>
                <dmf:checkbox name='checkAll' cssclass='doclistbodyDatasortlink' runatclient='true'
                        onclick="checkAllRows" id="datagridCheckAll"/>
            </dmf:datagridTh>
            <dmf:celllist >
                <dmf:celltemplate type='number'>
                    <dmf:datagridTh scope='col' cssclass='doclistfilenamedatagrid leftAlignment' resizable="true">
                        <dmf:datasortlink name='sort4' datafield='CURRENT' mode='numeric'
                                                cssclass='doclistbodyDatasortlink'/>
                    </dmf:datagridTh>
                </dmf:celltemplate>
                <dmf:celltemplate type='date'>
                    <dmf:datagridTh scope='col' cssclass='doclistfilenamedatagrid leftAlignment' resizable="true">
                        <nobr><dmf:datasortlink name='sort5' datafield='CURRENT' mode='numeric'
                                                cssclass='doclistbodyDatasortlink'/></nobr>
                    </dmf:datagridTh>
                </dmf:celltemplate>
                <dmf:celltemplate>
                    <dmf:datagridTh scope='col' cssclass='doclistfilenamedatagrid leftAlignment' resizable="true">
                        <dmf:datasortlink name='sort6' datafield='CURRENT' cssclass='doclistbodyDatasortlink'/>
                    </dmf:datagridTh>
                </dmf:celltemplate>
            </dmf:celllist>
        </tr>
        <tr>
            <td colspan="20" class="rowSeparator"/>
        </tr>
        <dmf:datagridRow cssclass="defaultDatagridRowAltStyle">
            <dmf:datagridRowTd align="center">&nbsp;
                <dmf:checkbox name='check' value="<%=component.getCurrentRowSelection()%>" runatclient="true"
                        onclick="unCheckAllRows">
                    <dmf:hidden name="objectIdStore" datafield="r_object_id"/>
                </dmf:checkbox>
            </dmf:datagridRowTd>
            <dmf:celllist >
                <dmf:celltemplate type='boolean'>
                    <dmf:datagridRowTd nowrap="true" cssclass="doclistfilenamedatagrid">
                        <dmf:booleanformatter truenlsid="MSG_YES" falsenlsid="MSG_NO">
                            <dmf:label datafield='CURRENT'/>
                        </dmf:booleanformatter>
                    </dmf:datagridRowTd>
                </dmf:celltemplate>
                <dmf:celltemplate type='date'>
                    <dmf:datagridRowTd nowrap="true" cssclass="doclistfilenamedatagrid">
                        <ccdea:datevalueformatter pattern="dd.MM.yyyy">
                            <dmf:label datafield='CURRENT'/>
                        </ccdea:datevalueformatter>
                    </dmf:datagridRowTd>
                </dmf:celltemplate>
                <dmf:celltemplate >
                    <dmf:datagridRowTd nowrap="true" cssclass="doclistfilenamedatagrid">
                        <ccdea:stringlengthformatter maxlen='40'>
                            <%--<dmf:label datafield='CURRENT'/>--%>
                            <dmfx:actionlink datafield='CURRENT' action='ccdea_properties'>
                                <dmf:argument name="objectId" datafield="r_object_id"/>
                                <dmf:argument name="formTemplateName" datafield="view_template"/>
                                <dmf:argument name="format" value="pdf"/>
                                <dmf:argument name="inline" value="false"/>
                            </dmfx:actionlink>
                        </ccdea:stringlengthformatter>
                    </dmf:datagridRowTd>
                </dmf:celltemplate>
            </dmf:celllist>
            <tr>
                <td colspan="20" class="rowSeparator"/>
            </tr>
        </dmf:datagridRow>
            <dmf:nodataRow>
                <td colspan="23" height="24">
                <dmf:label nlsid="MSG_NO_DATA"/>
                </td>
            </dmf:nodataRow>
    </dmf:datagrid>
<script type='text/javascript'>
var checkAllCB = document.getElementById("datagridCheckAll");
if (checkAllCB != null) {
    checkAllCB["otherPagesChecked"] = checkAllCB.checked || <%=component.isSinglePage()%>;
}
function checkAllRows(obj){
    for (var i=0; i < <%=component.getDisplayedRowCount()%>; i++){
        var cb=document.getElementsByName('<%=component.getName()%>_check_'+i)[0];
        cb.checked=obj.checked;
    }
    obj.otherPagesChecked = obj.checked;
}
function unCheckAllRows(obj){
    var checkAllCB = document.getElementById("datagridCheckAll");
    if(checkAllCB.checked){
        checkAllCB.checked=obj.checked;
    } else if(checkAllCB.otherPagesChecked){
        var allChecked = true;
        for (var i=0; i < <%=component.getDisplayedRowCount()%>; i++){
            var cb=document.getElementsByName('<%=component.getName()%>_check_'+i)[0];
            allChecked = allChecked && cb.checked;
        }
        checkAllCB.checked=allChecked;
    }
}
</script>
</dmf:form>
</dmf:body>
</dmf:html>
