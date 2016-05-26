package ru.rb.ccdea.storage.persistence;

import com.documentum.fc.common.DfException;

/**
 * Ошибка во время загрузки контента
 * Created by prokofev.dv on 27.05.2016.
 */
public class ContentLoaderException extends DfException {
    public ContentLoaderException(String message, Throwable cause) {
        super(message, cause);
    }
}