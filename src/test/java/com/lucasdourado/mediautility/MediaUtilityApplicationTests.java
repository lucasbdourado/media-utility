package com.lucasdourado.mediautility;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.lucasdourado.mediautility.media.conversion.Mp4ToMp3Converter;
import com.lucasdourado.mediautility.persistence.OperationRepository;
import com.lucasdourado.mediautility.storage.TemporaryStorageService;

@SpringBootTest(properties = {
		"MEDIA_UTILITY_STORAGE_ROOT=target/temp-storage",
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
})
class MediaUtilityApplicationTests {

	@MockitoBean
	private OperationRepository operationRepository;

	@MockitoBean
	private Mp4ToMp3Converter mp4ToMp3Converter;

	@MockitoBean
	private TemporaryStorageService temporaryStorageService;

	@Test
	void contextLoads() {
	}

}
