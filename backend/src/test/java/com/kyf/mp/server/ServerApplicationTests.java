package com.kyf.mp.server;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.kyf.mp.server.practice.config.PracticeMapperConfig;
import com.kyf.mp.server.practice.repository.PracticeRepository;
import com.kyf.mp.server.practice.vo.SongUploadVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kyf.mp.server.config.MyBatisPlusConfig;
import com.kyf.mp.server.modules.song.entity.Songs;
import com.kyf.mp.server.practice.config.PracticeTestApplication;
import com.kyf.mp.server.practice.dto.SearchLikeDTO;
import com.kyf.mp.server.practice.mapper.PracticeMapper;

@SpringBootTest(classes = {
		PracticeMapperConfig.class,
		MyBatisPlusConfig.class,
		PracticeTestApplication.class,
		PracticeRepository.class,
})
@ActiveProfiles("practice")
class ServerApplicationTests {

	private final PracticeMapper.InnerSongsMapper songsMapper;
	private final PracticeMapper.InnerUsersMapper usersMapper;
	private final PracticeRepository practiceRepository;

	@Autowired
	public ServerApplicationTests(PracticeMapper.InnerSongsMapper songsMapper,
			PracticeMapper.InnerUsersMapper usersMapper,
			PracticeRepository practiceRepository) {
		this.songsMapper = songsMapper;
		this.usersMapper = usersMapper;
		this.practiceRepository = practiceRepository;
	}

	@Test
	@DisplayName("Spring 应用上下文应能正常启动")
	void contextLoads() {
	}

	@Test
	@DisplayName("测试一，查用户 101 上传的歌曲")
	void searchSongsByUser() {
		List<Songs> songs = practiceRepository.searchSongsByUser(101L);
		assertThat(songs).hasSize(3);
		assertThat(songs).extracting(Songs::getSongTitle).contains("夜曲");
		assertThat(songs).extracting(Songs::getSongId).contains(201L).contains(202L).contains(203L);
	}

	@Test
	@DisplayName("测试二，歌名包含 '晴' ")
	void searchSongByKeyword() {
		String keyword = "晴";
		List<Songs> songs = practiceRepository.searchSongByKeyword(keyword);
		assertThat(songs).hasSize(2);
		assertThat(songs).extracting(Songs::getSongTitle).contains("晴天").contains("晴空");
	}

	@Test
	@DisplayName("符合条件查询")
	void searchSsongByMultCoditions() {
		System.out.println("=======================测试输出============================");
		Long uploaderId = null;
		String keyword = "Silence";
		Long low = null;
		Long up = null;
		Long page = 1L;
		Long pageSize = 3L;
		SearchLikeDTO dto = new SearchLikeDTO();
		dto.setKeyword(keyword);
		dto.setLow(low);
		dto.setPage(page);
		dto.setPageSize(pageSize);
		dto.setUp(up);
		dto.setUploaderId(uploaderId);
		IPage<Songs> result1 = practiceRepository.searchLike(dto);
		List<Songs> songs1 = result1.getRecords();
		for (Songs song : songs1) {
			System.out.println("songname:" + song.getSongTitle() + "  " + "songid:" + song.getSongId() + "\n");
		}
		IPage<Songs> result2 = songsMapper.searchSongsIPage(dto, new Page<Songs>(dto.getPage(), dto.getPageSize()));
		List<Songs> songs2 = result2.getRecords();

		System.out.println("wrapper total=" + result1.getTotal()
				+ ", records=" + result1.getRecords().size());
		System.out.println("xml total=" + result2.getTotal()
				+ ", records=" + result2.getRecords().size());
		for (Songs song : songs2) {
			System.out.println("songname:" + song.getSongTitle() + "  " + "songid:" + song.getSongId() + "\n");
		}
		System.out.println("==========================输出结束================================");
	}

	@Test
	@DisplayName("连表查询")
	void searchSongUpload() {
		Long userId = 101L;
		List<SongUploadVO> vos = songsMapper.songUpload(userId);
		for (SongUploadVO vo : vos) {
			System.out
					.println("songId:" + vo.getSongId() + "\n" + "songTitle:" + vo.getSongTitle() + "\n" + "uploaderId"
							+ vo.getUploaderId() + "\n" + "uploderName:" + vo.getUploaderName() + "\n");

		}
	}

}
