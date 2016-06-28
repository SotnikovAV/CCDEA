<%@ page import="com.documentum.web.form.Form" %>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ taglib uri="/WEB-INF/tlds/ccdea_filters.tld" prefix="ccdea" %>
<dmf:html>
<dmf:head>
<dmf:webform/>
</dmf:head>
<dmf:body>
<dmf:form>
    <dmf:datagrid rowselection="true" name="contragent_datagrid" paged="false" preservesort="true"
              cssclass="doclistbodyDatagrid" width="100%" pagesize="5" cellspacing="0" cellpadding="0" bordersize="0">
        <tr class="colHeaderBackground">
            <dmf:celllist fields="s_contractor_name_r,s_contractor_country_code_r" nlsids="MSG_CONTRAGENT_NAME,MSG_CONTRAGENT_COUNTRY_CODE">
                <dmf:celltemplate field="s_contractor_name_r">
                    <dmf:datagridTh scope='col' cssclass='doclistfilenamedatagrid leftAlignment' style="width: 100%;">
                        <dmf:datasortlink name='contragent_name_sortlink' datafield='CURRENT' cssclass='doclistbodyDatasortlink' />
                    </dmf:datagridTh>
                </dmf:celltemplate>
                <dmf:celltemplate field="s_contractor_country_code_r">
                    <dmf:datagridTh scope='col' cssclass='doclistfilenamedatagrid leftAlignment' >
                        <dmf:datasortlink name='contragent_country_sortlink' datafield='CURRENT' cssclass='doclistbodyDatasortlink' />
                    </dmf:datagridTh>
                </dmf:celltemplate>
            </dmf:celllist>
        </tr>
        <tr>
            <td colspan="20" class="rowSeparator"/>
        </tr>
        <dmf:datagridRow cssclass="defaultDatagridRowAltStyle">
            <dmf:celllist fields="s_contractor_name_r,s_contractor_country_code_r">
                <dmf:celltemplate field="s_contractor_name_r">
                    <dmf:datagridRowTd nowrap="true" cssclass="doclistfilenamedatagrid">
                        <dmf:label datafield='CURRENT'/>
                    </dmf:datagridRowTd>
                </dmf:celltemplate>
                <dmf:celltemplate field="s_contractor_country_code_r">
                    <dmf:datagridRowTd nowrap="true" cssclass="doclistfilenamedatagrid">
                        <dmf:label datafield='CURRENT'/>
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

</dmf:form>
</dmf:body>
</dmf:html>
