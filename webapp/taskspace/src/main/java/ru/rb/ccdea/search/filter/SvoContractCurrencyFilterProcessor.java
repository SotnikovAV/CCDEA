package ru.rb.ccdea.search.filter;

import com.documentum.web.form.control.Text;
import com.documentum.web.formext.component.Component;
import ru.rb.ccdea.control.SearchListBox;
import ru.rb.ccdea.search.DqlQueryBuilder;
import ru.rb.ccdea.utils.StringUtils;

/**
 * Класс для фильтра по валюте контракта для СВО.<br/>
 * В СВО отсутствует соответствующий атрибут, поэтому
 * мы джойним таблицу контрактов по условию совпадения
 * номера и даты контракта
 * Created by ER21595 on 20.07.2015.
 */
public class SvoContractCurrencyFilterProcessor extends TextFilterProcessor {

    @Override
    public void addCondition(Component component, DqlQueryBuilder builder) {
        Text textControl = (Text) component.getControl(controlName);
        if(textControl!=null) {
            if (textControl.getValue() != null && !textControl.getValue().trim().isEmpty()) {
                builder.addFromTable("ccdea_contract");
                StringBuilder bld = new StringBuilder("ccdea_svo_detail.s_contract_number = ccdea_contract.s_contract_number " +
                        "and ccdea_svo_detail.t_contract_date=ccdea_contract.t_contract_date ");
                bld.append("and ccdea_contract.s_currency_code like '");
                bld.append(textControl.getValue()).append("%'");
                builder.addWhereClause(bld.toString());
            }
        }

    }
}
