<%@ page import="com.documentum.web.form.Form" %>
<%@ page import="ru.rb.ccdea.search.SearchComponent" %>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page errorPage="/wdk/errorhandler.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>
<%@ taglib uri="/WEB-INF/tlds/ccdea_filters.tld" prefix="ccdea" %>
<dmf:html>
<dmf:head>
<dmf:webform/>

<link rel='stylesheet' href='<%=Form.makeUrl(request, "/ccdea/style/search_component.css")%>' type='text/css' />

<script type='text/javascript' src='<%=Form.makeUrl(request, "/wdk/include/paneset.js")%>'></script>
<script type='text/javascript' src='/taskspace/taskspace/include/splitpane.js'></script>
<script>
    function retrieveSingleValue(valueType) {
        var prefs = InlineRequestEngine.getPreferences(InlineRequestType.JSON);
        prefs.setCallback(retrieveSingleValueCallback);
        var valueInput = document.getElementsByName("ccdea_vk_search_contents_" + valueType + "_0")[0];
        if (valueInput != null) {
            postInlineServerEvent(null,prefs,null,null,"onaction","action","retrieveSingleValue","valueType",valueType,"valueStart",valueInput.value);
        }
    }
    function retrieveSingleValueCallback(data) {
        if (isEventPostingLocked()) {
            releaseEventPostingLock();
        }
        if (data) {
            var result = data["RESPONSE_DATA"][0];
            var resultCommaIndex = result.indexOf(',');
            var resultType = result.substring(0, resultCommaIndex);
            var resultValue = result.substring(resultCommaIndex + 1);
            if (resultValue != "null") {
                setKeys(event);
                safeCall(postServerEvent2,"GenericFormContainer_0",null,"ccdea_vk_search_contents_" + resultType + "_search_value_0","GenericFormContainer_ccdea_vk_search_contents_0","completeInputValue","valueType",resultType);
            }
            else {
                setKeys(event);
                safeCall(beginModalPopupMode,null,"GenericFormContainer_0","medium","true");
                safeCall(postServerEvent2,"GenericFormContainer_0",null,"ccdea_vk_search_contents_" + resultType + "_search_value_0","GenericFormContainer_ccdea_vk_search_contents_0","searchInputValue","valueType",resultType);
            }
        }
    }
</script>
</dmf:head>
<% SearchComponent component = (SearchComponent)pageContext.getAttribute(Form.FORM, PageContext.REQUEST_SCOPE);%>
<dmf:body marginheight='0' marginwidth='0'
topmargin='0' bottommargin='0' leftmargin='0' rightmargin='0'>

<div id="main_area" style="overflow:auto">
    <table>
        <tr><td>
            <table>
            <tr>
                <td colspan="3" class="topLayout">
                    <table><tr><td colspan='6'>
                        <dmf:label nlsid='MSG_CLIENT'/>
                    </td></tr><tr><td>
                        <dmf:label nlsid='MSG_CLIENT_NUMBER'/>
                    </td><td>
                        <dmf:text name="customer_number" style="width: 100px;"/>
                    </td><td style="vertical-align: middle;">
                        <a href='#' onclick='retrieveSingleValue("customer_number"); return false;'>
                            <dmf:image name="customer_number_search_value" style="border: none; outline: none;" src="icons/search/search-go.gif" />
                        </a>
                    </td><td>
                        <dmf:label nlsid='MSG_CLIENT_NAME'/>
                    </td><td>
                        <dmf:text name="customer_name" style="width: 400px;"/>
                    </td><td style="vertical-align: middle;">
                        <a href='#' onclick='retrieveSingleValue("customer_name"); return false;'>
                            <dmf:image name="customer_name_search_value" style="border: none; outline: none;" src="icons/search/search-go.gif" />
                        </a>
                    </td></tr></table>
                </td>
                <td class="topLayout">
                    <ccdea:listboxfilter name="doctype" size='4' multiselect='true'
                            captionnlsid="MSG_DOCUMENT_TYPE">
                    </ccdea:listboxfilter>
                </td>
                <td class="topLayout">
                    <table>
                        <tr><td>
                            <dmf:label nlsid='MSG_DOCUMENT_STATUS'/>
                        </td></tr>
                        <tr><td>
                            <dmf:dropdownlist name="document_status">
                                <dmf:option value='' nlsid='MSG_ANY'/>
                                <dmf:option value='DONE' nlsid='MSG_ACCEPTED'/>
                                <dmf:option value='RJT' nlsid='MSG_RETURNED'/>
                            </dmf:dropdownlist>
                        </td></tr>
                     </table>
                </td>
            </tr>
            <tr><td colspan="5"><hr/></td></tr>
            <tr>
                <td class="topLayout">
                    <table><tr><td colspan='2'>
                        <dmf:label nlsid='MSG_DOCUMENT_OFFER_DATE'/>
                    </td></tr><tr><td>
                        <dmf:label nlsid='MSG_FROM'/>
                    </td><td>
                        <dmf:dateinput name="document_offer_date_from"/>
                    </td></tr><tr><td>
                        <dmf:label nlsid='MSG_TO'/>
                    </td><td>
                        <dmf:dateinput name="document_offer_date_to"/>
                    </td></tr></table>
                </td>
                <td class="topLayout">
                    <table><tr><td colspan='2'>
                        <dmf:label nlsid='MSG_ACCEPT_REJECT_DATE'/>
                    </td></tr><tr><td>
                        <dmf:label nlsid='MSG_FROM'/>
                    </td><td>
                        <dmf:dateinput name="accept_reject_date_from"/>
                    </td></tr><tr><td>
                        <dmf:label nlsid='MSG_TO'/>
                    </td><td>
                        <dmf:dateinput name="accept_reject_date_to"/>
                    </td></tr></table>
                </td>
                <td class="topLayout">
                    <table><tr><td colspan='2'>
                        <dmf:label nlsid='MSG_CHANGE_DATE'/>
                    </td></tr><tr><td>
                        <dmf:label nlsid='MSG_FROM'/>
                    </td><td>
                        <dmf:dateinput name="change_date_from"/>
                    </td></tr><tr><td>
                        <dmf:label nlsid='MSG_TO'/>
                    </td><td>
                        <dmf:dateinput name="change_date_to"/>
                    </td></tr></table>
                </td>
                <td colspan="2" class="topLayout">
                    <ccdea:listboxfilter name="processing_unit" size='4' multiselect='true' captionnlsid="MSG_PROCESSING_UNIT" />
                </td>
            </tr>
            <tr><td colspan="5"><hr/></td></tr>
            <tr>
                <td colspan="2" class="topLayout">
                    <table cellpadding="0" cellspacing="0"><tr><td class="zeroLayout">
                        <table><tr><td colspan='3'>
                            <dmf:label nlsid='MSG_PASSPORT_NUMBER'/>
                        </td></tr><tr><td>
                            <dmf:text name="passport_number" style="width: 350px;"/>
                        </td><td style="vertical-align: middle;">
                            <a href='#' onclick='retrieveSingleValue("passport_number"); return false;'>
                                <dmf:image name="passport_number_search_value" style="border: none; outline: none;" src="icons/search/search-go.gif" />
                            </a>
                        </td><td style="vertical-align: middle;">
                            <dmf:image name="passport_number_manage_value" style="border: none; outline: none;" onclick="manageMultipleValue" src="icons/plus.gif">
                                <dmf:argument name="valueType" value="passport_number" />
                                <dmf:argument name="usemodalpopup" value="true" />
                                <dmf:argument name="refreshparentwindow" value="true" />
                            </dmf:image>
                            <dmf:hidden name="passport_number_value" />
                        </td></tr></table>
                    </td></tr><tr><td class="zeroLayout">
                        <table><tr><td colspan="7">
                            <dmf:label nlsid='MSG_PASSPORT_TYPE_CODE'/>
                        </td></tr><tr><td>
                            <dmf:checkbox name="passport_type_code_1" label="1" />
                        </td><td>
                            <dmf:checkbox name="passport_type_code_2" label="2" />
                        </td><td>
                            <dmf:checkbox name="passport_type_code_3" label="3" />
                        </td><td>
                            <dmf:checkbox name="passport_type_code_4" label="4" />
                        </td><td>
                            <dmf:checkbox name="passport_type_code_5" label="5" />
                        </td><td>
                            <dmf:checkbox name="passport_type_code_6" label="6" />
                        </td><td>
                            <dmf:checkbox name="passport_type_code_9" label="9" />
                        </td></tr></table>
                    </td></tr></table>
                </td>
                <td class="topLayout">
                    <table><tr><td colspan='2'>
                        <dmf:label nlsid='MSG_PASSPORT_DATE'/>
                    </td></tr><tr><td>
                        <dmf:label nlsid='MSG_FROM'/>
                    </td><td>
                        <dmf:dateinput name="passport_date_from"/>
                    </td></tr><tr><td>
                        <dmf:label nlsid='MSG_TO'/>
                    </td><td>
                        <dmf:dateinput name="passport_date_to"/>
                    </td></tr></table>
                </td>
                <td class="topLayout">
                    <table><tr><td colspan='2'>
                        <dmf:label nlsid='MSG_PS_CLOSE_DATE'/>
                    </td></tr><tr><td>
                        <dmf:label nlsid='MSG_FROM'/>
                    </td><td>
                        <dmf:dateinput name="ps_close_date_from"/>
                    </td></tr><tr><td>
                        <dmf:label nlsid='MSG_TO'/>
                    </td><td>
                        <dmf:dateinput name="ps_close_date_to"/>
                    </td></tr></table>
                </td>
                <td class="topLayout">
                    <table><tr><td>
                        <dmf:label nlsid='MSG_PS_CLOSE_REASON'/>
                    </td></tr><tr><td>
                        <dmf:text name="ps_close_reason" style="width: 100px;"/>
                    </td></tr></table>
                </td>
            </tr>
            <tr><td colspan="5"><hr/></td></tr>
            <tr>
                <td colspan="2" class="topLayout">
                    <table><tr><td colspan='3'>
                        <dmf:label nlsid='MSG_CONTRACT_NUMBER'/>
                    </td></tr><tr><td>
                        <dmf:text name="contract_number" style="width: 350px;"/>
                    </td><td style="vertical-align: middle;">
                        <a href='#' onclick='retrieveSingleValue("contract_number"); return false;'>
                            <dmf:image name="contract_number_search_value" style="border: none; outline: none;" src="icons/search/search-go.gif" />
                        </a>
                    </td><td style="vertical-align: middle;">
                        <dmf:image name="contract_number_manage_value" style="border: none; outline: none;" onclick="manageMultipleValue" src="icons/plus.gif">
                            <dmf:argument name="valueType" value="contract_number" />
                            <dmf:argument name="usemodalpopup" value="true" />
                            <dmf:argument name="refreshparentwindow" value="true" />
                        </dmf:image>
                        <dmf:hidden name="contract_number_value" />
                    </td></tr></table>
                </td>
                <td class="topLayout">
                    <table><tr><td colspan='2'>
                        <dmf:label nlsid='MSG_CONTRACT_DATE'/>
                    </td></tr><tr><td>
                        <dmf:label nlsid='MSG_FROM'/>
                    </td><td>
                        <dmf:dateinput name="contract_date_from"/>
                    </td></tr><tr><td>
                        <dmf:label nlsid='MSG_TO'/>
                    </td><td>
                        <dmf:dateinput name="contract_date_to"/>
                    </td></tr></table>
                </td>
                <td class="topLayout">
                    <table><tr><td colspan='2'>
                        <dmf:label nlsid='MSG_CONTRACT_CURRENCY'/>
                    </td></tr><tr><td>
                        <dmf:text name="contract_currency" style="width: 100px;"/>
                    </td><td style="vertical-align: middle;">
                        <a href='#' onclick='retrieveSingleValue("contract_currency"); return false;'>
                            <dmf:image name="contract_currency_search_value" style="border: none; outline: none;" src="icons/search/search-go.gif" />
                        </a>
                    </td></tr></table>
                </td>
                <td class="topLayout">
                    <table><tr><td colspan='2'>
                        <dmf:label nlsid='MSG_CONTRACT_FINISH_DATE'/>
                    </td></tr><tr><td>
                        <dmf:label nlsid='MSG_FROM'/>
                    </td><td>
                        <dmf:dateinput name="contract_finish_date_from"/>
                    </td></tr><tr><td>
                        <dmf:label nlsid='MSG_TO'/>
                    </td><td>
                        <dmf:dateinput name="contract_finish_date_to"/>
                    </td></tr></table>
                </td>
            </tr>
            <tr><td colspan="5"><hr/></td></tr>
            <tr>
                <td colspan="2" class="topLayout">
                    <table><tr><td colspan='3'>
                        <dmf:label nlsid='MSG_DOCUMENT_NUMBER'/>
                    </td></tr><tr><td>
                        <dmf:text name="document_number" style="width: 350px;"/>
                    </td><td style="vertical-align: middle;">
                        <a href='#' onclick='retrieveSingleValue("document_number"); return false;'>
                            <dmf:image name="document_number_search_value" style="border: none; outline: none;" src="icons/search/search-go.gif" />
                        </a>
                    </td><td style="vertical-align: middle;">
                        <dmf:image name="document_number_manage_value" style="border: none; outline: none;" onclick="manageMultipleValue" src="icons/plus.gif">
                            <dmf:argument name="valueType" value="document_number" />
                            <dmf:argument name="usemodalpopup" value="true" />
                            <dmf:argument name="refreshparentwindow" value="true" />
                        </dmf:image>
                        <dmf:hidden name="document_number_value" />
                    </td></tr></table>
                </td>
                <td class="topLayout">
                    <table><tr><td colspan='2'>
                        <dmf:label nlsid='MSG_DOCUMENT_DATE'/>
                    </td></tr><tr><td>
                        <dmf:label nlsid='MSG_FROM'/>
                    </td><td>
                        <dmf:dateinput name="document_date_from"/>
                    </td></tr><tr><td>
                        <dmf:label nlsid='MSG_TO'/>
                    </td><td>
                        <dmf:dateinput name="document_date_to"/>
                    </td></tr></table>
                </td>
                <td class="topLayout">
                    <table><tr><td colspan='2'>
                        <dmf:label nlsid='MSG_DOCUMENT_CURRENCY'/>
                    </td></tr><tr><td>
                        <dmf:text name="document_currency" style="width: 100px;"/>
                    </td><td style="vertical-align: middle;">
                        <a href='#' onclick='retrieveSingleValue("document_currency"); return false;'>
                            <dmf:image name="document_currency_search_value" style="border: none; outline: none;" src="icons/search/search-go.gif" />
                        </a>
                    </td></tr></table>
                </td>
                <td class="topLayout">
                    <table><tr><td>
                        <dmf:label nlsid='MSG_CONFIRM_DOCUMENT_TYPE'/>
                    </td></tr><tr><td>
                        <dmf:text name="confirm_document_type" style="width: 100px;"/>
                    </td></tr></table>
                </td>
            </tr>
            <tr><td colspan="5"><hr/></td></tr>
            <tr>
                <td class="topLayout">
                    <table><tr><td colspan="4">
                        <dmf:label nlsid='MSG_PAYMENT_FLAG'/>
                    </td></tr><tr><td>
                        <dmf:checkbox name="payment_flag_1" label="1" />
                    </td><td>
                        <dmf:checkbox name="payment_flag_2" label="2" />
                    </td><td>
                        <dmf:checkbox name="payment_flag_9" label="9" />
                    </td><td>
                        <dmf:checkbox name="payment_flag_0" label="0" />
                    </td></tr></table>
                </td>
                <td class="topLayout">
                    <table><tr><td>
                        <dmf:label nlsid='MSG_IS_CHANGED'/>
                    </td></tr><tr><td>
                        <dmf:dropdownlist name="correction_flag">
                            <dmf:option value='' nlsid='MSG_ANY'/>
                            <dmf:option value='TRUE' nlsid='MSG_YES'/>
                            <dmf:option value='FALSE' nlsid='MSG_NO'/>
                        </dmf:dropdownlist>
                     </td></tr></table>
                </td>
                <td class="topLayout">
                    <table><tr><td colspan='2'>
                        <dmf:label nlsid='MSG_OPERATION_DATE'/>
                    </td></tr><tr><td>
                        <dmf:label nlsid='MSG_FROM'/>
                    </td><td>
                        <dmf:dateinput name="operation_date_from"/>
                    </td></tr><tr><td>
                        <dmf:label nlsid='MSG_TO'/>
                    </td><td>
                        <dmf:dateinput name="operation_date_to"/>
                    </td></tr></table>
                </td>
                <td colspan="2" class="topLayout">
                    <table><tr><td>
                        <dmf:label nlsid='MSG_OPERATION_CODE'/>
                    </td></tr><tr><td>
                        <dmf:text name="operation_code" style="width: 100px;" maxlength="5"/>
                    </td></tr></table>
                </td>
            </tr>
            <tr><td colspan="5"><hr/></td></tr>
            <tr>
                <td colspan="5" align="left" class="topLayout"><dmf:label name="error_message" cssclass="error" style='color:red'/></td>
            </tr>
            <tr><td colspan="5" class="topLayout">
                <table><tr><td>
                    <dmf:button name="searchButton" onclick="onClickSearch" nlsid="MSG_FIND"/>
                </td><td>
                    <dmf:button name="clearButton" onclick="onClickClearConditions" nlsid="MSG_CLEAR_CONDITIONS"/>
                </td><td>
                    <dmf:button name="printButton" onclick="onClickPrint" nlsid="MSG_PRINT"/>
                </td>
                <% if(component.canPrintAllVersions()){%>
                <td>
                    <dmf:checkbox name="print_all_versions" nlsid="MSG_ALL_VERSIONS" tooltipnlsid="MSG_PRINT_ALL_VERSIONS"/>
                </td>
                <%}%>
                <td>
                    <dmf:button name="printDossierButton" onclick="onClickPrint" nlsid="MSG_PRINT_DOSSIER"/>
                </td></tr></table>
            </td></tr>
            </table>
        </td></tr>
        <tr><td>
            <ccdea:print name="print"/>
        </td></tr>
        <tr><td>
            <dmfx:containerinclude/>
        </td></tr>
    </table>
</div>

<script>
    var mainDiv = document.getElementById("main_area");
    mainDiv.style.width=document.body.clientWidth+"px";
    mainDiv.style.height=document.body.clientHeight+"px";
</script>

</dmf:body>
</dmf:html>
