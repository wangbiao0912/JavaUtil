package passwordUtil;

import com.after00.other.ZipPwdUtil;
import com.after00.password.Check;
import com.after00.password.DictionaryUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author xiaofan
 */
public class DictionaryUtilTest {
    static String path7ZFile = "E:\\123.7z";

    static int lengthMin = 5;

    static int lengthMax = 8;

    List<String> params = new ArrayList<>(Arrays.asList("z", "x", "f", "c"));

    /**
     * 校验器myCheck实时校验.
     */
    @Test
    public void testValidate() {
        for (int i = 0; i < 10; i++) {
            params.add(i + "");
        }
        DictionaryUtil.validate(params, lengthMin, lengthMax, myCheck);
    }

    
    /**
     * 生成全量字典.
     * 
     */
    @Test
    public void testBuildAll() {
        List<String> result = DictionaryUtil.buildAll(params, 3, 3);
        System.out.println(result.size() + ":" + result.toString());
    }

    /**
     * 重写校验器，校验7z压缩包密码.
     */
    static Check myCheck = new Check(lengthMin, lengthMax) {
        /**
         * {@inheritDoc}.
         */
        @Override
        protected boolean execute(String param) {
            if (ZipPwdUtil.validatePwd(path7ZFile, param)) {
                return true;
            }
            return false;
        }
    };
}
