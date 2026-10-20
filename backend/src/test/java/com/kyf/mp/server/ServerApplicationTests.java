package com.kyf.mp.server;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kyf.mp.server.modules.agent.mapper.AgentMapper;
import com.kyf.mp.server.modules.song.entity.Songs;
import com.kyf.mp.server.modules.song.repository.SongsRepository;
import com.kyf.mp.server.modules.user.repository.UsersRepository;
import com.kyf.mp.server.utils.SongScannerTask;
import com.kyf.mp.server.modules.agent.dto.GetPlaylistsDTO;
import com.kyf.mp.server.modules.agent.dto.GetSongsDTO;
import com.kyf.mp.server.modules.agent.dto.GetUsersDTO;

import lombok.AllArgsConstructor;

@SpringBootTest(classes = ServerApplication.class, properties = {
		"song.scanner.enabled=True",
		"spring.flyway.enabled=false"
})
// @Transactional
@AllArgsConstructor(onConstructor_ = @Autowired)
class ServerApplicationTests {
	SongsRepository songsRepository;
	UsersRepository usersRepository;
	SongScannerTask songScannerTask = new SongScannerTask(songsRepository, usersRepository);

	@Test
	@DisplayName("测试供agent使用的mapper方法")
	void agentMapperTest() {
		try {
			songScannerTask.writeInSqlFromNull();
		} catch (Exception e) {
			System.err.println("发生异常");
		}
	}
}
