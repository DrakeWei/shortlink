import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(classes = ShortLinkTest.class)
@ContextConfiguration
@ComponentScan(basePackages = "com.drake.shortlink.admin")
public class ShortLinkTest {

    @Resource
    private RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    @Test
    public void testRBloom(){
        userRegisterCachePenetrationBloomFilter.add("Drake");
        userRegisterCachePenetrationBloomFilter.add("Eminem");
        System.out.println(userRegisterCachePenetrationBloomFilter.contains("Drake"));
        System.out.println(userRegisterCachePenetrationBloomFilter.contains("Eminem"));
        System.out.println(userRegisterCachePenetrationBloomFilter.contains("Kanye"));
    }
}
