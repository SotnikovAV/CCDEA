package ru.rb.ccdea.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import ru.rb.ccdea.net.CountingUrlWrapper;

import java.io.IOException;

/**
 * Created by ER21595 on 16.06.2015.
 */
public class CPDDocument extends PDDocument {

    public static PDDocument load(CountingUrlWrapper url) throws IOException {
        return load(url.openStream());
    }

}
