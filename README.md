# 安卓端开发说明

##数据库操作
* 数据表对应User、Moment、Comment三个类。
* 对数据库的增删改查都封装在DBOperator类中，所以可以数据库操作这样写：

```java
DBOperator dbOperator = new DBOperator();
User user = new User("1234","4515@qq,com","zhanp");
dbOperator.insertUser(user);
List<Moment> moments = dbOperator.selectMomentsRecent();
dbOperator.close();
```
*目前数据库操作方法有
```java
//插入用户
boolean insertUser(User user)
//更新用户头像
boolean updateUserPortrait(User user)
//更新用户其他信息
boolean updateUser(User user)
//查找用户
User selectUser(String userId)
//插入动态
boolean insertMoment(Moment moment)
//删除动态
boolean deleteMoment(String momentId)
//查找某个动态
Moment selectMoment(String momentId)
//查找最近插入的动态
List<Moment> selectMomentsRecent()
//查找某个用户发布的动态
List<Moment> selectMomentByUser(String userId)
//插入留言
boolean insertComment(Comment comment)
//查找和某个用户有关的所有留言
Comment selectCommentById(String commentId)
//查找某个动态的所有留言
List<Comment> selectCommentByMoment(String momentId,String userId)
```

注意：更新网络请求方法！！Retrofit Callback上面再封装了一个BaseCallBack,现在网络请求写法改为这样：

```java
 button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetworkManager.getInstance().test(new BaseCallback() {
                    @Override
                    public void onResponse(Call<ResultBean> call, Response<ResultBean> response) {
                        ResultBean resultBean=  response.body();
                        if(this.checkResult(MainActivity.this,resultBean)) {
                            Toast.makeText(MainActivity.this, (String) resultBean.getData(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResultBean> call, Throwable t) {
                        Log.e("MainActivity", t.getMessage()  );
                    }
                });
            }
        });
```



注意`NetworkManager.getInstance().test(new BaseCallback() {...}`

和``if(this.checkResult(MainActivity.this,resultBean)) {正常逻辑。。。。}``

## 包结构说明
```
----com.scut.weinxinshare

 		  ----exception |异常类放这儿

 		  ----manager|流程管理类，应该不需要怎么加类

 		  ----model |所有实体类放这里

 		  ----retrofit  |retrofit网络请求相关配置类，无特殊情况不需要加类

 		  ----service |retrofit api接口

 		  ----utils| 工具类放这里

 		  ----view| 视图类
 		   		  ----fragment  |碎片放这里

 		   		  xxxActivity  |Activity直接放view目录下

 		IConst 静态常量放这里

 		MyApplication 运行的程序首先第一个运行的东西
   ```

## 网络请求

* 网络请求分为三层，Activity层，NetworkManager层，retrofit Service接口层

     ### Activity中：

     ​

```java
public class MainActivity extends AppCompatActivity {
Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button= (Button) findViewById(R.id.testButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetworkManager.getInstance().test(new Callback<ResultBean>() {
                    @Override
                    public void onResponse(Call<ResultBean> call, Response<ResultBean> response{
                        ResultBean resultBean=  response.body();
                        Toast.makeText(MainActivity.this,			(String)resultBean.getData(),Toast.LENGTH_SHORT).show();
                   }
                    @Override
                    public void onFailure(Call<ResultBean> call, Throwable t) {
                        Log.e("MainActivity", t.getMessage()  );
                    }
                });
            }
        });
    }
}
```

其中callback为回调方法，onResponse为网络连接成功时的逻辑，onFailure为网络失败（连接超时等）的逻辑

**注意：此时规定服务器返回类型统一为ResultBean，具体的返回存放在ResultBean的data成员里**



### NetworkManager 中

```java
 public  void test(Callback<ResultBean> callback){
        TestService testService= retrofit.create(TestService.class);
        Call<ResultBean> call=testService.test("我不想写代码！");
        call.enqueue(callback);
    }
```

其中`call.enqueue(callback);`为异步请求，如果需要同步请求使用`call.execute()`

**注意：一般不使用`call.execute()`，因为安卓规定在主线程中不能使用同步请求，否则导致主线程阻塞**





###  Retrofit service请求接口

```java
public interface TestService {
    @POST("test")
    Call<ResultBean> test(@Body String test);
}
```

注意两个标签@Body和@POST,@post括号内为请求的url,即网址除去ip地址和端口号后面的部分。

@Body 为请求参数，可以为任何一个自定义对象，不仅仅可以为String。建议传入一个HashMap对象，HashMap对象内保存请求参数。

### 图片及文件上传

```java
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by skyluo on 2018/4/14.
 * ！！！！
 * ！！！！
 * 注意：所有multipart/form-data类型的请求请写在这里！！！！
 * ！！！！
 * ！！！！
 *
 */

public interface MultipartService {
    @Multipart
    @POST("MultipartTest")
    Call<ResultBean> test(@Part("test") String test1, @Part List<MultipartBody.Part> file);
}
```

**所有文件上传操作规定写在此接口之下 **

统一按照上面写法

* 如果是文件的话` @Part List<MultipartBody.Part> file)`,注意此时不能@Part（"xxx"），文件名需要在构建`MultipartBody.Part`时指定而不能通过@Part括号内值指定。

* 如果是普通变量的话使用`@Part("test") String test1`,@Part括号内的为参数名称。

  ​

NetworkManager中

```java
 public  void MutiprtTest(Callback<ResultBean> callback, List<File> fileList) throws IOException {
        MultipartService multipartService=multipartRetrofit.create(MultipartService.class);
        Call<ResultBean> call=multipartService.test("卧槽", NetworkUtils.filesToMultipartBodyParts(fileList,"fileList"));
        call.enqueue(callback);
    }
```



**注意！！！！这里是使用multipartRetrofit构建api接口！！**

**是这样！！**

` MultipartService multipartService=multipartRetrofit.create(MultipartService.class);`

**不是这样！！！**

`TestService testService= retrofit.create(MultipartService.class);`

retrofit用于加密传输，但是multipart/form-data我实在想不出如何加密，只能使用原生的不加密的方法。

multipartRetrofit是没有使用加密传输的！！

**文件上传用multipartRetrofit，正常无文件上传请求用retrofit！！！**

**文件上传用multipartRetrofit，正常无文件上传请求用retrofit！！！**

**文件上传用multipartRetrofit，正常无文件上传请求用retrofit！！！**

**重要事情说三遍**



****

```java
public class NetworkUtils {
    /**
     * 将文件加入multipartBody
     * @param files
     * @return
     */
    public static List<MultipartBody.Part> filesToMultipartBodyParts(List<File> files,String fileListName) {
        List<MultipartBody.Part> parts = new ArrayList<>(files.size());
        for (File file : files) {
            // TODO: 16-4-2  这里为了简单起见，没有判断file的类型
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/png"), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData(fileListName, file.getName(), requestBody);
            parts.add(part);
        }
        return parts;
    }
}
```

如果是多个文件上传可以直接用上面的写法，上面的fileListName是你们之前写在接口文档的参数名称。

如果是单个文件的话，可以仿造`NetworkUtils. filesToMultipartBodyParts()`，很容易的。

## 图片相册api

```java
   btnPopPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PictureSelector.create(MainActivity.this)
                        .openGallery(PictureMimeType.ofImage())//全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio(
                        .maxSelectNum(9)// 最大图片选择数量 int
                        .imageSpanCount(4)// 每行显示个数 int
                        .selectionMode(PictureConfig.MULTIPLE)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                        .previewImage(false)// 是否可预览图片 true or false
                        .isCamera(true)// 是否显示拍照按钮 true or false
                        .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                        .sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                        .setOutputCameraPath("/CustomPath")// 自定义拍照保存路径,可不填
                        .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
            }
        });
```



该方法能直接打开相册并让用户选择图片

```java
  @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<File> fileList=new ArrayList<>();
        if (requestCode == PictureConfig.CHOOSE_REQUEST) {
            if (resultCode == RESULT_OK) {
                List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
//                StringBuilder sb = new StringBuilder();

                for (LocalMedia p : selectList) {
//                    sb.append(p);
//                    sb.append("\n");
                    fileList.add( new File(p.getPath()));
                }
                try {
                    NetworkManager.getInstance().MutiprtTest(new Callback<ResultBean>() {
                        @Override
                        public void onResponse(Call<ResultBean> call, Response<ResultBean> response) {
                            ResultBean resultBean=  response.body();
                            Toast.makeText(MainActivity.this,(String)resultBean.getData(),Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<ResultBean> call, Throwable t) {
                            Log.e("MainActivity", t.getMessage()  );
                        }
                    },fileList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //yjPublishEdit.setText(sb.toString());
            }
        }

       
  
```

该回调方法用于用户选择完图片回调，获得图片File.

# 获取地理位置
* 在 MainActivity 中已经尽力向用户申请获取地理位置权限，因此正常允许读取位置的情况下不需要再申请

1. 获取Location 单例，传入参数为context
2. 使用location对象获取经纬度，类型为double
```java
 Location location = LocationUtils.getInstance( MainActivity.this ).returnLocation();
 String address = "纬度：" + location.getLatitude() + "经度：" + location.getLongitude();

```

3. 重写该Activity的onDestroy函数，移除定位监听

```java
 @Override
    protected void onDestroy() {
        super.onDestroy();
        LocationUtils.getInstance(this).removeLocationUpdatesListener();

    }
```

## 动态、评论发布界面
* 请把项目的最低sdk版本设置为21，最高随意
* 主题颜色可以在color.xml中进行修改
