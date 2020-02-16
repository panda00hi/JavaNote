## 1 文件上传原理

### 1.1 文件上传的必要前提

A form 表单的 enctype 取值必须是：multipart/form-data
(默认值是:application/x-www-form-urlencoded)
enctype: 是表单请求正文的类型
B method 属性取值必须是 Post
C 提供一个文件选择域 `<input type=”file” />` 

### 1.2 文件上传的原理分析

当 form 表单的 enctype 取值不是默认值后，request.getParameter()将失效。
enctype=”application/x-www-form-urlencoded”时，form 表单的正文内容是：
key=value&key=value&key=value
当 form 表单的 enctype 取值为 Mutilpart/form-data 时，请求正文内容就变成：
每一部分都是 MIME 类型描述的正文
-----------------------------7de1a433602ac 分界符
Content-Disposition: form-data; name="userName" 协议头
aaa 协议的正文
-----------------------------7de1a433602ac
Content-Disposition: form-data; name="file"; 
filename="C:\Users\zhy\Desktop\fileupload_demofile\b.txt"
Content-Type: text/plain 协议的类型（MIME 类型）
bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb
-----------------------------7de1a433602ac--

### 1.3 借助Commons第三方组件实现文件上传

使用 Commons-fileupload 组件实现文件上传，需要导入该组件相应的支撑 jar 包：Commons-fileupload 和
commons-io。commons-io 不属于文件上传组件的开发 jar 文件，但Commons-fileupload 组件从 1.1 版本开始，它
工作时需要 commons-io 包的支持。
导入依赖

``` 
<dependency>
  <groupId>commons-io</groupId>
  <artifactId>commons-io</artifactId>
  <version>2.6</version>
</dependency>
<dependency>
  <groupId>commons-fileupload</groupId>
  <artifactId>commons-fileupload</artifactId>
  <version>1.3.3</version>
</dependency>
```

## 2 三种上传方式

### 2.1 使用JavaEE进行文件上传

传统的JavaEE文件上传思路是通过解析request对象,获取表单中的上传文件项并执行保存.
 
``` JAVA
@Controller
@RequestMapping("/fileUpload")
public class FileUploadController {

	@RequestMapping("/javaEE")
    public String fileupload1(HttpServletRequest request) throws Exception {
  
        // 创建目录保存上传的文件
        String path = request.getSession().getServletContext().getRealPath("/uploads/");
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        // 创建ServletFileUpload来解析request
        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        List<FileItem> items = upload.parseRequest(request);
        // 遍历解析的结果,寻找上传文件项
        for (FileItem item : items) { 
            if (!item.isFormField()) {
                // 不是普通表单项,说明是文件上传项
                
                // 服务器中保存的文件名
                String filename = UUID.randomUUID().toString().replace("-", "") + "_" + item.getName();
                // 上传文件
                item.write(new File(path, filename));
                // 删除临时文件
                item.delete();
            }
        }

        return "success";
    }
}
```

### 2.2 使用SpringMVC进行单服务器文件上传

我们上传的文件和访问的应用存在于同一台服务器上。
并且上传完成之后，浏览器可能跳转。
可以使用SpringMVC提供的文件解析器实现文件上传,在Spring容器中注入文件解析器CommonsMultipartResolver对象

#### 第一步：导入jar包依赖，commons-io、Commons-fileupload。

#### 第二步：编写jsp页面

``` 
<form action="/fileUpload" method="post" enctype="multipart/form-data">
名称：<input type="text" name="picname"/><br/>
图片：<input type="file" name="uploadFile"/><br/>
<input type="submit" value="上传"/>
</form>
```

#### 第三步：编写Controller控制器

``` JAVA
/**

* 文件上传的的控制器

*/
@Controller("fileUploadController")
public class FileUploadController {
/**

* 文件上传

*/
@RequestMapping("/fileUpload")
public String testResponseJson(String picname,MultipartFile 
uploadFile,HttpServletRequest request) throws Exception{
//定义文件名
String fileName = "";
//1.获取原始文件名
String uploadFileName = uploadFile.getOriginalFilename();
//2.截取文件扩展名
String extendName = 
uploadFileName.substring(uploadFileName.lastIndexOf(".")+1, 
uploadFileName.length());
//3.把文件加上随机数，防止文件重复
String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
//4.判断是否输入了文件名
if(!StringUtils.isEmpty(picname)) {
fileName = uuid+"_"+picname+"."+extendName;
}else {
fileName = uuid+"_"+uploadFileName;
}
System.out.println(fileName);
//2.获取文件路径
ServletContext context = request.getServletContext();
String basePath = context.getRealPath("/uploads");
//3.解决同一文件夹中文件过多问题
String datePath = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
//4.判断路径是否存在
File file = new File(basePath+"/"+datePath);
if(!file.exists()) {
file.mkdirs();
}
//5.使用 MulitpartFile 接口中方法，把上传的文件写到指定位置
uploadFile.transferTo(new File(file,fileName));
return "success";
}
}

```
#### 第四步：配置文件解析器
```
<!--注册multipartResolver,由DispatcherServlet来负责调用-->
<bean id="multipartResolver" class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
    <!--设置字符编码防止文件名乱码-->
    <property name="defaultEncoding" value="utf-8"/>
    <!--设置上传文件的总大小，单位是字节b-->
    <property name="maxUploadSize" value="1048576"/>
    <!--设置单个上传文件的大小，单位是字节b-->
    <property name="maxUploadSizePerFile" value="1048576"/>
    <!--设置内存缓冲区的大小，当超过该值的时候会写入到临时目录-->
    <property name="maxInMemorySize" value="1048576"/>
    <!--设置临时目录-->
    <property name="uploadTempDir" value="tempupload"/>
    <!--默认是false，如果设置为true的话，不会将文件路径去除，在IE浏览器下上传时会将路径名也作为文件名上传：D:\image\monkey.png-->
    <property name="preserveFilename" value="false"/>
    <!--是否使用懒加载，默认是false-->
    <property name="resolveLazily" value="true"/>
</bean>
```
注意：
文件上传的解析器 id 是固定的，不能起别的名称，否则无法实现请求参数的绑定。（不光是文件，其他字段也将无法绑定）

**设置临时上传文件目录的作用：**
- 提高安全性
客户端上传的文件直接传到临时目录，这样子对于客户端来说隐藏了真实的文件存放目录
- 便于管理 
当用户取消上传或上传失败的话，直接操作临时目录即可，无需再去修改真实目录中的文件。

### 2.3 使用SpringMVC进行跨服务器文件上传
#### 引入jersey库
我们可以引入jersey库进行服务器间通信,实现将文件上传到一个专用的文件服务器,需要在pom.xml中引入jersey库。
```xml
<dependency>
    <groupId>com.sun.jersey</groupId>
    <artifactId>jersey-core</artifactId>
    <version>1.18.1</version>
</dependency>
<dependency>
    <groupId>com.sun.jersey</groupId>
    <artifactId>jersey-client</artifactId>
    <version>1.18.1</version>
</dependency>
```
#### 在处理器方法中创建Client对象实现服务器间通信,将文件上传到文件服务器上,代码如下:

``` JAVA
@Controller
@RequestMapping("/fileUpload")
public class FileUploadController {

	@RequestMapping("/betweenServer")
    public String fileupload3(@RequestParam("fileParam") MultipartFile upload) throws Exception {
        System.out.println("跨服务器文件上传...");

        // 文件服务器URL
        String fileServerPath = "http://localhost:9090/uploads/";	
        
        // 获取服务器中保存的文件名
        String filename = UUID.randomUUID().toString().replace("-", "") + "_" + upload.getOriginalFilename();

        // 创建客户端对象并在文件服务器上创建资源
        Client client = Client.create();
        WebResource webResource = client.resource(fileServerPath + filename);
        webResource.put(upload.getBytes());

        return "success";
    }
}
```
#### 编写处理文件的工具类
```java

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

public class FileUtil {

    // 上传文件
    public static void uploadFile(byte[] file, String filePath, String fileName) throws Exception {
        File targetFile = new File(filePath);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        FileOutputStream out = new FileOutputStream(filePath + fileName);
        out.write(file);
        out.flush();
        out.close();
    }

    // 删除文件,返回值表示是否删除成功
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // 重命名文件
    public static String renameToUUID(String fileName) {
        return UUID.randomUUID() + "." + fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
```

保存文件可以简化代码为：
``` JAVA
String fileName = FileUtil.renameToUUID(uploadFile.getOriginalFilename());
String filePath = request.getSession().getServletContext().getRealPath("/uploads/");
FileUtil.uploadFile(uploadFile.getBytes(), filePath, fileName);
```
