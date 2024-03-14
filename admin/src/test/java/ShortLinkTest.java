import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;

import static com.drake.shortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_TOKEN;

@SpringBootTest(classes = ShortLinkTest.class)
@ContextConfiguration
@ComponentScan(basePackages = "com.drake.shortlink.admin")
public class ShortLinkTest {

    @Resource
    private RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRBloom(){
        userRegisterCachePenetrationBloomFilter.add("Drake");
        userRegisterCachePenetrationBloomFilter.add("Eminem");
        userRegisterCachePenetrationBloomFilter.add("Kanye");
        userRegisterCachePenetrationBloomFilter.add("Wayne");
        System.out.println(userRegisterCachePenetrationBloomFilter.contains("Drake"));
        System.out.println(userRegisterCachePenetrationBloomFilter.contains("Eminem"));
        System.out.println(userRegisterCachePenetrationBloomFilter.contains("Kanye"));
        System.out.println(userRegisterCachePenetrationBloomFilter.contains("Wayne"));
    }

    @Test
    public void testJsonObject(){
        Object object = stringRedisTemplate.opsForHash().get(USER_LOGIN_TOKEN + "Wayne", "3951765f-dfb3-418a-83ad-25d334b88585");
        System.out.println(object);
    }
}
