import com.carpooling.AppRun;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author LiangHanSggg
 * @date 2023-08-09 22:09
 */
@Slf4j
@SpringBootTest(classes = AppRun.class)
public class lstudet {

    @Test
    public void a(){
        log.error("出现错误");
    }
}
