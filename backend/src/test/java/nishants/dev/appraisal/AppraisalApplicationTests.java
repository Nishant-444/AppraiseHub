package nishants.dev.appraisal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import dev.nishants.appraisal.AppraisalApplication;

@SpringBootTest(classes = AppraisalApplication.class)
@ActiveProfiles("test")
class AppraisalApplicationTests {

  @Test
  void contextLoads() {
  }

}
