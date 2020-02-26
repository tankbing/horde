package com.geekbang.horde;

import com.geekbang.horde.entity.User;
import org.beetl.sql.core.SQLManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
class HordeApplicationTests {

    @Autowired
    private SQLManager sqlManager;

    @Test
    void contextLoads() {
        User user = new User();
        user.setAge(22);
        user.setName("xxx");
        user.setCreateDate(new Date());
        sqlManager.insert(user);
    }

}
