package net.qiujuer.web.italker.push;

import net.qiujuer.web.italker.push.mobileim.ServerLauncherImpl;
import net.qiujuer.web.italker.push.provider.AuthRequestFilter;
import net.qiujuer.web.italker.push.provider.GsonProvider;
import net.qiujuer.web.italker.push.service.AccountService;
import net.qiujuer.web.italker.push.utils.Hib;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ResourceConfig 是jetsey 的一个库，最小代码，只需要配置下面的1.X 配置。
 * @author qiujuer
 */
public class Application extends ResourceConfig {
    private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

    public Application() {
        // 给Jersey注册MultiPart的支持库，用以上传文件
        register(MultiPartFeature.class);

        // 1.1 注册逻辑处理的包名
        //packages("net.qiujuer.web.italker.push.service");
        packages(AccountService.class.getPackage().getName());  // 上面的方法也可以，但推荐用下面的方法

        // 注册我们的全局请求拦截器
        register(AuthRequestFilter.class);

        // 1.2 注册Json解析器
        // register(JacksonJsonProvider.class);    //已经被Gson替代了。
        // 替换解析器为Gson
        register(GsonProvider.class);

        // 1.3 注册日志打印输出
        register(Logger.class);

        // 启动时直接初始化Hibernate数据库
        Hib.setup();

        // 输出启动成功日志
        LOGGER.log(Level.INFO, "Application setup succeed!");

       new Thread(new Runnable() {
           @Override
           public void run() {
               //开启MobileIM
               try {
                   ServerLauncherImpl.serverLauncherMain();
               } catch (Exception e) {
                   e.printStackTrace();
               }
           }
       }).start();

        LOGGER.log(Level.INFO, "*****************************!");
    }
}
