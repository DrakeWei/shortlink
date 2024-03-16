import com.drake.shortlink.project.dao.entity.ShortLinkGotoDO;
import com.drake.shortlink.project.service.ShortLinkGotoService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(classes = ShortLinkTest1.class)
@ContextConfiguration
@ComponentScan(basePackages = "com.drake.shortlink.project")
public class ShortLinkTest1 {

    @Resource
    public ShortLinkGotoService shortLinkGotoService;

    @Test
    public void save(){
        ShortLinkGotoDO shortLinkGotoDO = ShortLinkGotoDO.builder()
                .gid("hz09a8")
                .fullShortUrl("https://nurl.ink/p6WuC")
                .build();
        shortLinkGotoService.save(shortLinkGotoDO);
    }
}
