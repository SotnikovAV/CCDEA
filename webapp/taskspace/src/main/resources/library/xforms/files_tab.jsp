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
	<script language='JavaScript1.2' src='<%=Form.makeUrl(request, "/wdk/include/dynamicAction.js")%>'></script>
    <script language='JavaScript1.2' src='<%=Form.makeUrl(request, "/webcomponent/navigation/navigation.js")%>'></script>
</dmf:head>
<% SearchResultsComponent component = (SearchResultsComponent)pageContext.getAttribute(Form.FORM, PageContext.REQUEST_SCOPE);%>
<dmf:body>
<dmf:form>
<table>
<tr><td><dmfx:actionbutton name="printButton" action="ucfview" nlsid="MSG_PRINT" dynamic="multiselect" /></td></tr>
<tr><td>
	<dmfx:actionmultiselect name='multi'>
		<dmf:argument name="objectId"/>
	    <dmf:argument name="type" value="ccdea_doc_content"/>
	    <dmf:datagrid rowselection="false" name="<%= SearchResultsComponent.OBJECT_LIST_GRID %>" paged="true" preservesort="true"
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
	        	<dmf:datagridTh scope='col' cssclass="doclistcheckbox leftAlignment" nowrap="true">
            		<dmfx:actionmultiselectcheckall/>
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
	        	<dmf:datagridRowTd cssclass="doclistcheckbox" style="vertical-align:top" valign="left">
		            <dmfx:actionmultiselectcheckbox name='check' value='false'>
		               <dmf:argument name='objectId' datafield='r_object_id'/>
		               <dmf:argument name='type' value="ccdea_doc_content"/>
		            </dmfx:actionmultiselectcheckbox>
		         </dmf:datagridRowTd>
		         
	            <dmf:celllist>
	            	<dmf:celltemplate field='doc_type_state'>
                  		<dmf:datagridRowTd nowrap="true" cssclass="doclistfilenamedatagrid">
								<dmfx:docbaseicon formatdatafield='a_content_type'
									typedatafield='r_object_type' linkcntdatafield='r_link_cnt'
									isvirtualdocdatafield='r_is_virtual_doc'
									assembledfromdatafield='r_assembled_from_id'
									isfrozenassemblydatafield='r_has_frzn_assembly'
									isreplicadatafield='i_is_replica'
									isreferencedatafield='i_is_reference' size='16' />
								<ccdea:stringlengthformatter maxlen='40'>
	                            <dmfx:actionlink action="ucfview" datafield='CURRENT'>
	                            	<dmf:argument name="objectId" datafield="r_object_id"/>
	                                <dmf:argument name="type" value="ccdea_doc_content"/>
	                            </dmfx:actionlink>
	                        </ccdea:stringlengthformatter>
	                    </dmf:datagridRowTd>
	                </dmf:celltemplate>
	                <dmf:celltemplate type='date'>
	                    <dmf:datagridRowTd nowrap="true" cssclass="doclistfilenamedatagrid">
	                        <dmf:datevalueformatter type="medium">
	                            <dmfx:actionlink action="ucfview" datafield='CURRENT'>
	                            	<dmf:argument name="objectId" datafield="r_object_id"/>
	                                <dmf:argument name="type" value="ccdea_doc_content"/>
	                            </dmfx:actionlink>
	                        </dmf:datevalueformatter>
	                    </dmf:datagridRowTd>
	                </dmf:celltemplate>
	                <dmf:celltemplate >
	                    <dmf:datagridRowTd nowrap="true" cssclass="doclistfilenamedatagrid">
	                        <ccdea:stringlengthformatter maxlen='40'>
	                            <dmfx:actionlink action="ucfview" datafield='CURRENT'>
	                            	<dmf:argument name="objectId" datafield="r_object_id"/>
	                                <dmf:argument name="type" value="ccdea_doc_content"/>
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
    </dmfx:actionmultiselect>
</td></tr>
</table>

</dmf:form>
</dmf:body>
</dmf:html>
