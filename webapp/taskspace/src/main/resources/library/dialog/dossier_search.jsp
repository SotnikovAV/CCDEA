<%@ page import="com.documentum.web.form.Form" %>
<%@ page import="ru.rb.ccdea.storage.persistence.DossierPersistence" %>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ taglib uri="/WEB-INF/tlds/ccdea_filters.tld" prefix="ccdea" %>
<dmf:html>
<dmf:head>
<dmf:webform/>
</dmf:head>
<dmf:body marginheight='0' marginwidth='0'
topmargin='0' bottommargin='0' leftmargin='0' rightmargin='0'>

<table>
<tr>
    <td><dmf:label nlsid='MSG_DOSSIER_TYPE'/></td>
    <td>
        <dmf:dropdownlist name="dossier_type" onselect="onDossierTypeSelect">
            <dmf:option value="<%=DossierPersistence.DOSSIER_TYPE_CONTRACT%>" nlsid='MSG_DOSSIER_TYPE_CONTRACT'/>
            <dmf:option value="<%=DossierPersistence.DOSSIER_TYPE_PASSPORT%>" nlsid='MSG_DOSSIER_TYPE_PASSPORT'/>
         </dmf:dropdownlist>
    </td>
</tr>
<tr>
    <td><dmf:label nlsid='MSG_CLIENT_NUMBER'/></td>
    <td><dmf:text name="client_number"/></td>
</tr>
<tr>
    <td><dmf:label name="reg_branch_code_lbl" nlsid='MSG_REG_BRANCH_CODE'/></td>
    <td colspan="2" class="topLayout">
        <ccdea:listboxfilter name="processing_unit" size='4' multiselect='true' />
    </td>
</tr>
<tr>
    <td><dmf:label name="passport_number_lbl" nlsid='MSG_PASSPORT_NUMBER'/></td>
    <td><dmf:text name="passport_number"/></td>
</tr>
<tr>
    <td><dmf:label name="contract_date_lbl" nlsid='MSG_CONTRACT_DATE'/></td>
    <td><dmf:dateinput name="contract_date"/></td>
</tr>
<tr>
    <td><dmf:label name="contract_number_lbl" nlsid='MSG_CONTRACT_NUMBER'/></td>
    <td><dmf:text name="contract_number"/></td>
</tr>
<tr>
    <td colspan="2"><dmf:button nlsid="MSG_SEARCH" name="search_btn" onclick="onClickSearch"/></td>
</tr>
<tr>
    <td colspan="2"><dmf:listbox name="results_list" size="3"/></td>
</tr>
</table>

</dmf:body>
</dmf:html>
