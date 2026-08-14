package com.kyf.mp.server.tools;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import com.kyf.mp.server.ServerApplication;
import com.kyf.mp.server.modules.song.business.AlbumsBusiness;
import com.kyf.mp.server.modules.song.business.SongsBusiness;


public class DbdataInit {
    public static void main(String[] args){
        ApplicationContext context = loadEnvironment();
        SongsBusiness songsBusiness = context.getBean(SongsBusiness.class);
        AlbumsBusiness albumsBusiness = context.getBean(AlbumsBusiness.class);
        Path songsPath = Path.of("Z:\\vue3_projects\\vue_musicplayer\\static\\songs");
        Path coversPath = Path.of("Z:\\vue3_projects\\vue_musicplayer\\static\\song_covers");
        try(Stream<Path> stream = Files.list(songsPath)){
            // 拿到元数据，构造song，生成id，存进数据库，提取出封面，存进文件夹
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public static ApplicationContext loadEnvironment(){
        return SpringApplication.run(ServerApplication.class,new String[0]);
    }


}
