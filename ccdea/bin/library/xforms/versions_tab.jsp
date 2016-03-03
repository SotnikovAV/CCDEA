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
<table>
<tr><td>&nbsp</td></tr>
<tr><td>
    <dmf:datagrid rowselection="true" name="<%= SearchResultsComponent.OBJECT_LIST_GRID %>" paged="true" preservesort="true"
              cssclass="doclistbodyDatagrid" width="100%" pagesize="5" cellspacing="0" cellpadding="0" bordersize="0">
        <tr><td colspan='30' class="pagerBackground">
                <table width='100%'><tr>
                     <td align='center' width='100%'><dmf:datapaging name="pager1" gotopageclass="doclistPager"/></td>
                     <td class="rightAlignment" nowrap valign='middle'><dmf:label nlsid='MSG_SHOW_ITEMS'/>&nbsp;</td>
                     <td valign='middle' nowrap> <dmf:datapagesize name='sizer' preference='application.display.classic'
                            pagesizevalues='5,50,100' tooltipnlsid='MSG_SHOW_ITEMS'/>&nbsp;</td>
                </tr></table>
        </td></tr>
        <tr class="colHeaderBackground">
            <dmf:celllist >
                <dmf:celltemplate type='number'>
                    <dmf:datagridTh scope='col' cssclass='doclistfilenamedatagrid leftAlignment' resizable="true">
                        <dmf:datasortlink name='sort4' datafield='CURRENT' mode='numeric'
                                                cssclass='doclistbodyDatasortlink'/>
                    </dmf:datagridTh>
                </dmf:celltemplate>
                <dmf:celltemplate type='date'>
                    <dmf:datagridTh scope='col' cssclass='doclistfilenamedatagrid leftAlignment' resizable="true">
                        <dmf:datasortlink name='sort5' datafield='CURRENT' mode='numeric'
                                                cssclass='doclistbodyDatasortlink'/>
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
            <dmf:celllist >
                <dmf:celltemplate type='date'>
                    <dmf:datagridRowTd nowrap="true" cssclass="doclistfilenamedatagrid">
                        <dmf:datevalueformatter type="medium">
                            <dmf:label datafield='CURRENT'/>
                        </dmf:datevalueformatter>
                    </dmf:datagridRowTd>
                </dmf:celltemplate>
                <dmf:celltemplate >
                    <dmf:datagridRowTd nowrap="true" cssclass="doclistfilenamedatagrid">
                        <ccdea:stringlengthformatter maxlen='40'>
                            <dmf:label datafield='CURRENT'/>
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
</td></tr>
</table>

</dmf:form>
</dmf:body>
</dmf:html>
