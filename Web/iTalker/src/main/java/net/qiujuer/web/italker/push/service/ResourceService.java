package net.qiujuer.web.italker.push.service;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.*;

/**
 * 资源服务
 * 用以上传文件或下载文件的接口开发
 * <p>
 * PS：
 * 1.需要添加依赖库
 * 2.Application中注册依赖库
 * 3.AuthRequestFilter中添加过滤避免需要登录才能操作，也可以不过滤
 */
// 127.0.0.1/api/resource/...
@Path("/resource")
public class ResourceService extends BaseService {

    /**
     * 上传文件接口，该接口可通过Multipart方式上传文件，
     * 同时包含title字符串字段和file文件
     *
     * @param title      上传的文本字段
     * @param stream     文件流，文件读取从这里开始
     * @param fileDetail 文件描述信息，可读取文件名，扩展名等
     * @return 返回服务器的存储路径
     */
    @POST
    @Path("/upload")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.TEXT_PLAIN})
    public String uploadPdfFile(@FormDataParam("title") String title,
                                @FormDataParam("file") InputStream stream,
                                @FormDataParam("file") FormDataContentDisposition fileDetail) {
        // 这里是我的服务器存储根目录
        // 需要换成对应服务器的绝对路径，也可以通过java api得到相对路径
        String UPLOAD_PATH = "/Users/qiujuer/Downloads/Resources";
        try {
            // 将文件存储到服务器上的地址
            String path = UPLOAD_PATH + fileDetail.getFileName();

            int read;
            byte[] bytes = new byte[1024];
            OutputStream out = new FileOutputStream(new File(path));
            while ((read = stream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.close();
            // 返回地址字段
            return path;
        } catch (IOException e) {
            throw new WebApplicationException("Error while uploading file. Please try again !!");
        }
    }
}
