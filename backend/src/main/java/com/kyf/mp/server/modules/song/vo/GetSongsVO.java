package com.kyf.mp.server.modules.song.vo;

import java.util.List;

import com.kyf.mp.server.modules.song.entity.Songs;

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
        private int totalPages;
        private int currentPage;
        private int pageLimit;
    }
}
