

import com.gexin.rp.sdk.base.IPushResult;
import com.gexin.rp.sdk.http.IGtPush;

import java.util.Map;

public class GetPushMessageResultDemo {
    //您应用的mastersecret
    private static final String MASTERSECRET = "CQjbsmy5gN7E00XQ1eDl46";
    //您应用的appkey
    private static final String APPKEY = "MgbBRw1xDp89sHbN3xGxE5";
    //您要查询的taskid
    private static final String TASKID = "OSS-0812_8d21bd1df6ea7bde56ad41bec47959a3";  //一个推送就是一个推送任务，这个任务ID需要个推反馈。

    static String host ="http://sdk.open.api.igexin.com/apiex.htm";
    public static void main(String[] args) {

        IGtPush push = new IGtPush(host,APPKEY, MASTERSECRET);
        Map<String, Object> res = (Map<String, Object>) push.getPushResult(TASKID).getResponse();
        for(Map.Entry<String,Object> entry: res.entrySet()){
            System.out.println(entry.getKey()+" "+entry.getValue());
        }
    }

}