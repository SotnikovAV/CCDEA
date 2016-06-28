<%@ page import="com.documentum.web.form.Form" %>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ taglib uri="/WEB-INF/tlds/ccdea_filters.tld" prefix="ccdea" %>
<dmf:html>
<dmf:head>
<dmf:webform/>
		<script language='JavaScript1.2' src='<%=Form.makeUrl(request, "/wdk/include/dynamicAction.js")%>'></script>
        <script language='JavaScript1.2'
                src='<%=Form.makeUrl(request, "/webcomponent/navigation/navigation.js")%>'></script>
</dmf:head>

<dmf:body marginheight='0' marginwidth='0'
topmargin='0' bottommargin='0' leftmargin='0' rightmargin='0'>

<table>
<tr>
    <td style="width: 1px;"><dmf:label nlsid='MSG_FILTER_VALUE'/>:</td>
    <td style="width: 1px;"><dmf:text name="value_filter" defaultonenter="true"/></td>
    <td style="width: 100%;"><dmf:button name="value_filter_img" onclick="onFilterClick" src="icons/search/search-go.gif" style="cursor: pointer;" default="true"/></td>
</tr>
<tr>
    <td colspan="3"><dmf:multiselector name="multivalue_selector" multiselect="true" itemsnlsid="MSG_FILTERED_VALUE_LIST" selecteditemsnlsid="MSG_SELECTED_VALUE_LIST" /></td>
</tr>
</table>

</dmf:body>
</dmf:html>
