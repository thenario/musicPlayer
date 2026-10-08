package com.kyf.mp.server;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kyf.mp.server.modules.agent.mapper.AgentMapper;
import com.kyf.mp.server.modules.agent.dto.GetPlaylistsDTO;
import com.kyf.mp.server.modules.agent.dto.GetSongsDTO;
import com.kyf.mp.server.modules.agent.dto.GetUsersDTO;

import lombok.AllArgsConstructor;

@SpringBootTest(classes = ServerApplication.class, properties = {
		"song.scanner.enabled=false",
		"spring.flyway.enabled=false"
})
@Transactional
@AllArgsConstructor(onConstructor_ = @Autowired)
class ServerApplicationTests {
	private AgentMapper.InnerPlaylistMapper playlistMapper;
	private AgentMapper.InnerSongMapper songMapper;
	private AgentMapper.InnerUserMapper userMapper;

	@Test
	@DisplayName("测试供agent使用的mapper方法")
	void agentMapperTest() {
		Long page = 1L;
		Long pageSize = 10L;
		IPage<GetSongsDTO> songPage = new Page<>(page, pageSize);
		IPage<GetUsersDTO> userPage = new Page<>(page, pageSize);
		IPage<GetPlaylistsDTO> playlistPage = new Page<>(page, pageSize);
		IPage<GetSongsDTO> songResult = songMapper.getSongs("青", songPage);
		IPage<GetUsersDTO> userResult = userMapper.getUsers("青", userPage);
		IPage<GetPlaylistsDTO> playlistResult = playlistMapper.getPlaylists("青", playlistPage);

		System.out.println("\n=================songs======================");
		List<GetSongsDTO> songs = songResult.getRecords();
		for (GetSongsDTO song : songs) {
			assertThat(song.getId()).isNotNull();
			boolean titleMatches = song.getSongTitle() != null && song.getSongTitle().contains("青");
			boolean artistMatches = song.getArtist() != null && song.getArtist().contains("青");
			assertThat(titleMatches || artistMatches).isTrue();
			System.out.println("\nsongTitle is" + song.getSongTitle());
		}
		System.out.println("\n=================users======================");
		List<GetUsersDTO> users = userResult.getRecords();
		for (GetUsersDTO user : users) {
			assertThat(user.getId()).isNotNull();
			assertThat(user.getUserName()).contains("青");
			System.out.println("\nuserName is" + user.getUserName());
		}
		System.out.println("\n================playlists===================");
		List<GetPlaylistsDTO> playlists = playlistResult.getRecords();
		for (GetPlaylistsDTO playlist : playlists) {
			assertThat(playlist.getId()).isNotNull();
			assertThat(playlist.getPlaylistName()).contains("青");
			System.out.println("\nplaylistName is" + playlist.getPlaylistName());
		}
	}
}
