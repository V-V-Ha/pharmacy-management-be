package com.fu.pha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class PageResponseModel<T>{
    private int page;
    private int size;
    private long total;
    private String msg;
    private List<T> listData;

}
