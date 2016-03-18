package ru.rb.ccdea.search;

import ru.rb.ccdea.utils.StringUtils;

import java.util.*;

/**
 * Простейший построитель запросов
 *
 * Created by ER21595 on 03.06.2015.
 */
public class DqlQueryBuilder {
    private Set<String> selectFields = new LinkedHashSet<String>();
    private Set<String> fromTables = new LinkedHashSet<String>();
    private Set<String> whereClauses = new LinkedHashSet<String>();
    private Set<String> groupBy = new LinkedHashSet<String>();

    public void addSelectFields(String selectFieldList, String delimiter){
        selectFields.addAll(Arrays.asList(selectFieldList.split(delimiter)));
    }
    public void addSelectField(String field){
        if(field!=null&&field.length()>0)selectFields.add(field);
    }

    public void addFromTable(String table){
        if(table!=null&&table.length()>0)fromTables.add(table);
    }
    
    public void addGroupBy(String groupByFields){
        if(groupByFields!=null&&groupByFields.length()>0) {
        	groupBy.add(groupByFields);
        }
    }

    public void addFromTables(Collection<String> tables){
        fromTables.addAll(tables);
    }

    public void addWhereClause(String clause){
        if(clause!=null&&clause.length()>0)whereClauses.add(clause);
    }

    public String getQuery(){
        StringBuilder bld = new StringBuilder();
        bld.append("SELECT ");
        bld.append(StringUtils.joinStrings(selectFields,", "));
        bld.append(" FROM ");
        bld.append(StringUtils.joinStrings(fromTables,", "));
        if(whereClauses.size()>0){
            bld.append(" WHERE ");
            bld.append(StringUtils.joinStrings(whereClauses," AND "));
        }
        if(groupBy.size() > 0) {
        	bld.append(" GROUP BY ");
        	bld.append(StringUtils.joinStrings(groupBy,", "));
        }
        return bld.toString();
    }
    
}
