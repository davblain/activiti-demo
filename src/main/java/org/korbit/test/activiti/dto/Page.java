package org.korbit.test.activiti.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class Page<T> {
    List<T> content = new ArrayList<>();
    long number;
    long totalElements;
    long totalPages;
    long getSize() {
        return content.size();
    }
    boolean isFirst() {
        return number == 0;
    }
    boolean isLast() {
        return number == totalPages;
    }



}
