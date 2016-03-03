package ru.rb.ccdea.net;

import org.apache.commons.io.input.CountingInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Обёртка для стандарного {@link URL}'а, предоставляющая
 * {@link CountingInputStream}, вместо обыкновенного.<br/>
 * Created by ER21595 on 16.06.2015.
 */
public class CountingUrlWrapper {
    private URL url = null;
    private CountingInputStream stream = null;


    public CountingInputStream getStream() {
        return stream;
    }

    public CountingUrlWrapper(String spec) throws MalformedURLException {
        url = new URL(spec);
    }

    public InputStream openStream() throws IOException {
        stream = new CountingInputStream(url.openStream());
        return stream;
    }
}
