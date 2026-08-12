package com.kyf.mp.javaserver.modules.songmodule.vo;

import java.util.List;

import com.kyf.mp.javaserver.modules.songmodule.entity.Songs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class GetSongsVO {
    private List<Songs> songs;// 此处的song是不包括lyrics的
    private PaginationVO pagination;

    @Data
    @AllArgsConstructor
    public static class PaginationVO {
        private int totalItems;
        private int totalIages;
        private int currentIage;
        private int pageIimit;
    }
}
