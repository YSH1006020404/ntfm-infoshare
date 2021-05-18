package cn.les;

import cn.les.framework.mybatis.dynamic.datasource.DynamicDataSourceRegister;
import cn.les.framework.zlmq.util.InitUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(value = "cn.les")
@MapperScan({"cn.les.ntfm.infoshare.dao"})
@Import(DynamicDataSourceRegister.class)
public class InfoshareApplication {

    public static void main(String[] args) {
        InitUtil.initRegion(new String[1]);
        SpringApplication.run(InfoshareApplication.class, args);
        System.out.println("ヾ(◍°∇°◍)ﾉﾞ    les-pd启动成功      ヾ(◍°∇°◍)ﾉﾞ\n");
    }

}
