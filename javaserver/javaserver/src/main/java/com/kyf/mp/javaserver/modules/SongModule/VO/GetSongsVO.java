package com.kyf.mp.javaserver.modules.SongModule.VO;

import java.util.List;

import com.kyf.mp.javaserver.modules.SongModule.entity.Songs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class GetSongsVO {
    private List<Songs> songs;// 此处的song是不包括lyrics的
    private PaginationVO pagination;

    @Data
    @AllArgsConstructor
    public static class PaginationVO {
        private long totalItems;
        private long totalIages;
        private long currentIage;
        private int pageIimit;
    }
}
