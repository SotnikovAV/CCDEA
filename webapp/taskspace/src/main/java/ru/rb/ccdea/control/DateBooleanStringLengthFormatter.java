package ru.rb.ccdea.control;

import com.documentum.web.form.Control;
import com.documentum.web.form.control.format.StringLengthFormatter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Форматтер, не пропускающий nulldate
 * и правильно форматирующий значения булевых полей
 * (при получении результатов запроса из базы последние
 * конвертируются в integer и double на уровне CollectionHandle,
 * поэтому приходится отлавливать значения и проверять поля).
 * Created by ER21595 on 17.07.2015.
 */
public class DateBooleanStringLengthFormatter extends StringLengthFormatter {
    private static final Set<String> NonStandardBooleanFields = new HashSet<String>();
    private static final String NLS_YES = "MSG_YES";
    private static final String NLS_NO = "MSG_NO";
    private static final String NULLDATE ="01.01.0001";
    static{
        //На всякий случай, если понадобится отображать в форматтере
        //булевы поля, не начинающиеся с b_
        //NonStandardBooleanFields.add("");
    }
    @Override
    public String format(String strValue) {
        if(NULLDATE.equals(strValue)){
            return "";
        }
        if(canBeBooleanValue(strValue)){
            Iterator it = getContainedControls();
            boolean foundBooleanAttr = false;
            while(it.hasNext()&& !foundBooleanAttr){
                Control c = (Control) it.next();
                String field = c.getDatafield();
                foundBooleanAttr = field!=null &&field.length()>0 &&
                        (field.startsWith("b_") ||NonStandardBooleanFields.contains(field));
            }
            if(foundBooleanAttr){
                String nls = ("1".equals(strValue)||"1.0".equals(strValue))?NLS_YES:NLS_NO;
                return getForm().getTopForm().getString(nls);
            }
        }
        return super.format(strValue);

    }

    private boolean canBeBooleanValue(String value){
        return "1".equals(value)||"0".equals(value)||
                "0.0".equals(value)||"1.0".equals(value);
    }
}
