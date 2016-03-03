package ru.rb.ccdea.utils;

import java.util.Collection;

/**
 * Created by ER21595 on 09.06.2015.
 */
public class StringUtils {
    public static interface IWorker <T>{
        String doWork(T param);
    }

    /**
     * Простой аналог StringJoiner'a из Java8.<br/>
     * Конструирует строку, в которой через разделитель перечисляются
     * результаты работы IWorker'а над элементами коллекции.
     *
     * @param col Коллекция, элементы которой нужно обработать
     * @param delim разделитель для строки
     * @param wrk содержит алгоритм обработки элементов Коллекции
     * @param <T> тип элементов Коллекции
     * @return
     */
    public static <T> String joinStrings(Collection<T> col, String delim, IWorker<T> wrk){
        StringBuilder bld = new StringBuilder();
        String prefix = "";
        for(T str:col){
            bld.append(prefix);
            prefix = delim;
            bld.append(wrk.doWork(str));
        }
        return bld.toString();
    }

    /**
     * Сборка строки из строк, содержащихся в коллекци, через разделитель
     * @param col
     * @param delim
     * @return
     */
    public static String joinStrings(Collection<String> col, String delim){
        return joinStrings(col, delim, new IWorker<String>() {
            public String doWork(String param) {
                return param;
            }
        });
    }
}
