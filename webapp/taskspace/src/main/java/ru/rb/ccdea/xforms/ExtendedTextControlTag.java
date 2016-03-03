package ru.rb.ccdea.xforms;

import com.documentum.web.form.ControlTag;
import com.documentum.web.formext.control.xforms.xformscontrol.XFormsTextControlTag;

public class ExtendedTextControlTag extends XFormsTextControlTag {
    protected void setControlTagProperties(ControlTag controlTag)
    {
        super.setControlTagProperties(controlTag);
        if ((controlTag instanceof ExtendedTextTag)) {
            ((ExtendedTextTag)controlTag).setXFormsElementId(getXFormsElementId());
        }
    }
}
