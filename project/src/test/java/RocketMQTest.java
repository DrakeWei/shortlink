import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(classes = RocketMQTest.class)
@ContextConfiguration
@ComponentScan(basePackages = "com.drake.shortlink.project")
public class RocketMQTest {

    @Resource
    public RocketMQTemplate rocketMQTemplate;

    @Test
    public void test(){
        rocketMQTemplate.syncSend("test","this is a test message");
    }
}
