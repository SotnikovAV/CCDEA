package ru.rb.ccdea.search;

import com.documentum.web.formext.component.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Заглушка для пустого компонента без таблицы,
 * показываемого пользователю, когда он только зашёл в систему
 * и ещё ничего не искал.
 * Created by ER21595 on 13.07.2015.
 */
public class NoResultsComponent extends Component {
    public int getDisplayedRowCount(){
            return 0;
    }

    public void refreshDatagrid(String query){

    }

    public void clearDatagrid(){

    }

    public Set<String> getSelectedDocuments() {
        return new HashSet<String>();
    }

}
