[TOC]



# 完整版的下载Demo 详详详解

## 需求

> 从任意一个下载网址后台下载文件，通知栏显示进度，支持断点续传

## 分析

> 关键词:
>
> + 下载--访问网络，需要权限，还有我们用OkHttp库来使用Http协议
> + 后台--用到服务，既然下载是耗时任务，就必须开启线程，但是要有下载进度吧，下载进度是UI控件显示的，这意味着必须用到异步消息处理才能在子线程中切换到主线程刷新UI，所以我们用AsyncTask处理异步消息，下载进度我们放在通知栏，所以在下载过程中服务应该变成前台服务
> + 文件--要操作文件所以要权限

## 需要注意的地方

> 这里说的后台下载，是指在另一个线程下载
>
> 所谓的前台服务是指让服务控制一个通知常驻在通知栏，被我们看的见，所以千万不要搞混

## 用到的类

> + AsyncTask
>
>   > 该类封装了异步消息处理，就是说该类很好的处理了从子线程切换到主线程的过程
>
> + service
>
>   > 用来在后台操作一些东西，具体点就是，这个类里面的方法能在后台完成一些任务，配合线程使用，如果没有开线程，那就有意思了，你的程序，有一个服务，一个活动，都由主线程控制，意味着你的程序有两个手下，都可以去做任务，但是活动是你看的到的，服务是你看不到的，一旦服务开了线程，意味着大部分活动能做的任务，服务也能做，但是活动是在你面前做，服务在你背后做，你看到的就是活动了

## 各种类用到的关系

> AsyncTask里面有一个方法会自动开启线程，有自动切换回主线程的方法，所以就不在服务开启线程了，在这个AsyncTask来下载，在服务里面进行下载进度的UI更新，这样用户在活动中控制下载的状态
>
> 这意味着
>
> AsyncTask(下载的线程)被服务管理
>
> 服务被活动管理，形成这样的层级关系
>
> + 活动控制下载的状态的过程
>
>   1. 用户使用活动控制服务
>   2. 服务控制下载
>   3. 下载的状态将返回给服务
>   4. 服务刷新进度控件
>   5. 用户从进度控件中看到的下载的状态被改变
>
>   这意味这服务要一直监控下载的状态，所以要用到java回调方法

## 具体实现

+ 回调接口

  ~~~java
  //回调接口，专门用来给DownloadService监听DownloadTask的状态的
  public interface DownloadListener {

      //当下载的进度发生改变的时候告诉service
      public void refleshProgress(int progress);

      //当已经暂停的时候告诉service
      public void onPaused();

      //已经取消的时候告诉service
      public void onCanceled();

      //任务完成的时候告诉service
      public void onSuccess();

      //任务失败的时候告诉service
      public void onFailed();

      //网络发生改变时告诉service
      public void onIntenet();
  }
  ~~~

  ​


+ ### 用AsyncTask开线程下载，我们要继承AsyncTask

  ~~~java
  //第一个参数是你等下在耗时任务中将要用的数据的类型，第二个在你从子线程切换到主线程时想要从子线程带什么类型的数据给主线程，第三个声明是耗时任务结束后返回值的类型
  public class DownloadTask extends AsyncTask<String, Integer, Integer> {

      //用于在线程中返回下载的结果
      private final int PAUSE = 1;
      private final int FAIL_CONNECTE=2;
      private final int CANCLE = 3;
      private final int SUCCESS = 4;
      private final int FAIL = 5;

      //用来控制下载状态的开关
      private boolean isPause = false;
      private boolean isCancel = false;

      public int lastProgest = 0;

    //服务监听下载状态的监听器
      private DownloadListener listener;

    //实例化的时候，服务会设置监听
      public DownloadTask(DownloadListener listener) {
          this.listener = listener;
      }

      //当doInBackground方法结束后，就会自动切换到主线程
      // 自动调用该方法，参数就是结果我们上面定义的返回结果
      //然后使用回调是告诉服务，下载的最后状态
      @Override
      protected void onPostExecute(Integer integer) {
          switch (integer) {
              case SUCCESS:
                  listener.onSuccess();
                  break;
              case FAIL:
                  listener.onFailed();
                  break;
              case PAUSE:
                  listener.onPaused();
                  break;
              case CANCLE:
                  listener.onCanceled();
                  break;
              case FAIL_CONNECTE:
                  listener.onIntenet();
                  break;
          }
      }

    //这个方法是用来告诉服务，下载的实时状态
      @Override
      protected void onProgressUpdate(Integer... values) {
          if (values[0] > lastProgest) {
              lastProgest = values[0];
              listener.refleshProgress(values[0]);
          }

      }

      //doInBackground()方法中
      // (第一个参数传入的时URL，第二个是储存的目录
    //这个方法会自动开启线程，所以我们在这里下载
      @Override
      protected Integer doInBackground(String... params) {
          String url = params[0];//获取下载的url
        
        //这个类用来操作文件，特点就是可以在已有的文件的末尾添加数据
        //用他来实现断点续传功能
          RandomAccessFile saveFile = null;

          //设置目录，名字是网址中最后一个 /  后面的字符串
          File file = new File(params[1], url.substring(url.lastIndexOf("/")));
        
        //记录已经下载了多少数据，为了实现断点续传
          long downloadedFile = 0;
        
        //带有缓冲区的字节流
          BufferedInputStream bi = null;
          try {
            //如果文件已经存在，说明下载过了，获取已经下载的数据大小
              if (file.exists())
                  downloadedFile = file.length();
              else
                  file.createNewFile();//没有下载过就新建文件
            //获取将要下载的文件大小，用来设置进度，还有判断上次下载的文件有没有下载完
              long downloadContentSize = getDownloadContentSize(url);
              if (downloadContentSize == 0) {
                  return FAIL;//如果将要下载的文件获取不到数据，说明下载不了所以返回下载的结果
              }
              if (downloadedFile == downloadContentSize) {
                  return SUCCESS;//如果下载过的文件数据大小等于将要下载的文件数据大小，说明上次下载成功了
              }
            //如果没有下载失败，而且下载没有完成或者没有下载，就开始下载
              OkHttpClient client = new OkHttpClient();
              Request request = new Request.Builder()
                      .url(url)
                //意思是，我们下载过的数据就不用请求下载了
                      .addHeader("RANGE", "bytes=" + downloadedFile + "-")
                      .build();
              Response response = client.newCall(request).execute();
              
            //如果请求的数据有返回，就写到文件中
              if (response != null) {
                //打开字节流
                  InputStream is = response.body().byteStream();
                  bi = new BufferedInputStream(is);
                  byte[] bytes = new byte[1024 * 1024];
                //记录这一次已经下载的数据，用来更新下载进度
                  int total, len;
                  total = 0;
                //获取可以在文件末尾添加数据的实例，然后获取权限
                  saveFile = new RandomAccessFile(file, "rw");
                //定位到文件中数据的末尾
                  saveFile.seek(downloadedFile);
                //在每一次写入文件时判断用户有没有要求改变下载状态
                  while ((len = bi.read(bytes)) != -1) {
                      if (isPause) {
                          return PAUSE;
                      } else if (isCancel) {
                        //既然取消了下载，那刚刚下载的文件就要删除掉
                          file.delete();
                          return CANCLE;
                      } else {
                        //如果没有，写入数据，将下载进度告诉服务，然后服务刷新控件
                          total += len;
                          saveFile.write(bytes, 0, len);
                          int progress = (int) 
                                  ((total + downloadedFile) * 100 / downloadContentSize);
                          publishProgress(progress);
                      }
                  }
                //关闭流
                  response.body().close();
                //执行到这一步说明下载已经成功了
                  return SUCCESS;
              }
          } catch (Exception e) {
              e.printStackTrace();
          } finally {
              try {
                  if (bi != null)
                      bi.close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
        //如果能执行到这一步，说明下载没成功，可能是网络问题
          return FAIL_CONNECTE;
      }

    //获取将要下载的文件的数据大小
      public long getDownloadContentSize(String url) throws IOException {
          long size = 0;
          OkHttpClient client = new OkHttpClient();
          Request request = new Request.Builder()
                  .url(url)
                  .build();
          Response response = client.newCall(request).execute();
          if (response != null && response.isSuccessful()) {
              size = response.body().contentLength();
              response.close();
          }
          return size;
      }

    //服务会调用这两个方法控制下载的状态
      public void onPauseDownload() {
          isPause = true;
      }

      public void onCancelDownload() {
          isCancel = true;
      }
  }
  ~~~

  ​

  ### 服务类

  ~~~java
  public class MyDownloadService extends Service {
    //记录将要下载数据的Url
    //我们知道，在我们点了开始下载后，会传一个url给服务，然后服务再去传给下载
    //很多时候我们在点击开始下载后，然后没有网络导致下载被暂停，然后我们取消下载
    //这个时候，如果文件是没有下载完的话，我们应该删除刚刚下载到的文件
    //但是程序并不知道发什么时候该删除，你看，如果是调用了start方法后，
    //再取消就是意味着，要删除，删除又要知道文件的地址，strat方法的参数就要传入文件目录，如果我们
    //在Strat方法中记录目录，然后在取消的时候判断有没有记录，有的话说明调用过Strat方法，那就删
      String url = null;
      String dir;
    
    //这个就是服务用来控制下载的
    //由于在上面的类中我们控制了暂停时或者取消已经结束了线程
    //所以每次开始我们都要新建一个Task对象
      DownloadTask dt = null;
    
    //等下用来给活动控制服务的
      private DownloadBind db = new DownloadBind();

      public class DownloadBind extends Binder {
          public void startDownload(String... value) {
              if (dt == null) {
                  dt = new DownloadTask(downloadListener);
                  dt.execute(value);
                  url = value[0];
                  dir = value[1];
                //开启前台服务，让下载进度显示出来
                  startForeground(1, changeDownloadNotification("下载中", 0));
                  Toast.makeText(MyDownloadService.this, "开始下载"
                          , Toast.LENGTH_SHORT).show();
              }
          }

          public void pause() {
              if (dt != null)
                  dt.onPauseDownload();
          }

        //这里我们为啥要对dt是不是null，因为在我们可能会在下载的过程中取消下载
        //如果是下载的过程中。dt是非null的，所以直接控制下dt里面的方法来取消下载
        //如果在暂停的时候取消，就是null的
          public void cancel() {
              if (dt != null) {
                  dt.onCancelDownload();
              } else if (url != null) {
                  String name = url.substring(url.lastIndexOf("/"));
                  File file = new File(dir, name);
                  if (file.exists()) {
                      file.delete();
                  }
                  getNotificationManager().cancel(1);
                  Toast.makeText(MyDownloadService.this, "已取消下载"
                          , Toast.LENGTH_SHORT).show();
              }
          }
      }

      public MyDownloadService() {
      }

    //这里就是给AsyncTask的回调方法，一旦暂停，什么的都要将dt设置为null，这样做的原因在
    //上一个注释里
      int nowProgress = 0;
      DownloadListener downloadListener = new DownloadListener() {
          @Override
          public void refleshProgress(int progress) {
              nowProgress = progress;
              getNotificationManager().notify(1, changeDownloadNotification(
                      "下载中", progress));
              Log.d("sssss", progress + "");
          }
        
        public void onIntenet(){
              //除了更新进度条，其他的都取消前台服务，原因很简单，取消下载，暂停下载
          //，不应该作为常驻通知
              stopForeground(true);
              dt=null;
              Toast.makeText(MyDownloadService.this, 
                             "连接失败!", Toast.LENGTH_SHORT).show();
              getNotificationManager().notify(1, changeDownloadNotification(
                      "连接失败",nowProgress));
          }

          @Override
          public void onPaused() {
              stopForeground(true);
              dt = null;
              getNotificationManager()
                .notify(1, changeDownloadNotification("暂停中", nowProgress));
              Toast.makeText(MyDownloadService.this, 
                             "暂停下载", Toast.LENGTH_SHORT).show();
          }

          @Override
          public void onCanceled() {
              stopForeground(true);
              dt = null;
              Toast.makeText(MyDownloadService.this,
                             "已取消下载！", Toast.LENGTH_SHORT).show();
          }

          @Override
          public void onSuccess() {
              stopForeground(true);
              dt = null;
              url = null;
              getNotificationManager()
                .notify(1, changeDownloadNotification("下载成功", -1));
              Toast.makeText(MyDownloadService.this,
                             "下载成功！", Toast.LENGTH_SHORT).show();
              Log.d("sssss", nowProgress + "");
          }

          @Override
          public void onFailed() {
              stopForeground(true);
              dt = null;
              NotificationManager nm = getNotificationManager();
              nm.notify(1, changeDownloadNotification("下载失败", -1));
          }
      };
         public NotificationManager getNotificationManager() {
           return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
         }

         public Notification changeDownloadNotification(String situation, int progress) {
           Intent intent = new Intent(this, MainActivity.class);
           PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
           NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                builder.setContentTitle(situation);
          if (progress > 0)
               builder.setContentText(progress + "%");
          builder.setSmallIcon(R.mipmap.ic_launcher);
          builder.setLargeIcon
              (BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
          builder.setContentIntent(pendingIntent);
          return builder.build();
     }

          @Override
          public IBinder onBind(Intent intent) {
            return db;
          }
    }
  ~~~


### 活动的代码

~~~java
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    MyDownloadService.DownloadBind downloadBind;
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBind = (MyDownloadService.DownloadBind) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button start = (Button) findViewById(R.id.start);
        Button pause = (Button) findViewById(R.id.pause);
        Button cancel = (Button) findViewById(R.id.cancel);

        start.setOnClickListener(this);
        pause.setOnClickListener(this);
        cancel.setOnClickListener(this);

        Intent intent = new Intent(this, MyDownloadService.class);
        startService(intent);
        bindService(intent, connection, BIND_AUTO_CREATE);

        int wesp = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (wesp != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this
                    , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            File file = new File("/storage/emulated/0/leejh");
            if (!file.isDirectory())
                file.mkdir();
        }

        //Log.d("sss",getExternalCacheDir().toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                downloadBind.startDownload
                  ("http://sw.bos.baidu.com/sw-search-sp/software/9a2808964b476/QQ_8.9.3.21169_setup.exe",
                        "/storage/emulated/0/leejh");
                break;
            case R.id.pause:
                downloadBind.pause();
                break;
            case R.id.cancel:
                downloadBind.cancel();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == 0) {
                    File file = new File("/storage/emulated/0/leejh");
                    if (!file.isDirectory())
                        file.mkdir();
                } else {
                    Toast.makeText(this, "没有权限", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, MyDownloadService.class);
        stopService(intent);
        unbindService(connection);
    }
}
~~~

