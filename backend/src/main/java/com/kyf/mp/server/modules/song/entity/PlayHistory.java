package com.kyf.mp.server.modules.song.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("play_history")
public class PlayHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "history_id", type = IdType.ASSIGN_ID)
    Long historyId;
    Long userId;
    Long songId;
    LocalDateTime playeDate;
}
