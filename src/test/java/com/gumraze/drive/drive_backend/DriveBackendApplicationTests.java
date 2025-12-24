package com.gumraze.drive.drive_backend;

import com.gumraze.drive.drive_backend.region.service.RegionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@ActiveProfiles("test")
class DriveBackendApplicationTests {

	@Mock
	RegionService regionService;

	@Test
	void contextLoads() {
	}

}
