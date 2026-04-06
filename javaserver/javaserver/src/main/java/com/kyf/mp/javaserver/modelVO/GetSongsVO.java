package com.kyf.mp.javaserver.modelVO;

import java.util.List;

import com.kyf.mp.javaserver.entity.Songs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class GetSongsVO {
    private List<Songs> songs;
    private PaginationVO pagination;

    @Data
    @AllArgsConstructor
    public static class PaginationVO {
        private long total_items; // 对应 totalItems
        private long total_pages; // 对应 totalPages
        private long current_page; // 对应 sqlPage
        private int page_limit; // 对应 pageLimit
    }
}
