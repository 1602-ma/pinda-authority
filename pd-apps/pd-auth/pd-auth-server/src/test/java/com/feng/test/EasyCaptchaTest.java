package com.feng.test;

import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.base.Captcha;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * @author f
 * @date 2023/5/14 22:54
 */
public class EasyCaptchaTest {

    @Test
    public void test01() throws FileNotFoundException {
        Captcha captcha = new ArithmeticCaptcha(115, 42);
        captcha.setCharType(2);
        captcha.out(new FileOutputStream(new File("d:\\hello.png")));
        String text = captcha.text();
        System.out.println(text);
    }
}
