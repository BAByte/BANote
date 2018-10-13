[TOC]

# 最快的json解析库使用方法

## 先说两句

​	我以为gson就非常屌了，又跑出个jackson库，这个库的性能是gson的十倍。先说说差别，gson是可以根据需求，从json数据中单独获取json中的一个对象的，但是jackson库就不能了，他是一次就将json里面的数据全部都变成了对象

## 举个例子

~~~java
[{"appname":"qq"},{"appname":"weixing"}]
~~~

上面是一组非常简单的json数据，使用gson是可以单一的解析里面其中一个对象出来的，但是使用jackson时是不可以的，jackson库会一次性把这个json数据解析完，说白了，gson是可以按需解析，jackson不可以

## 导入库

jackson-annotations-2.7.0.jar

jackson-core-2.7.0.jar

jackson-databind-2.7.0.jar

都在文件夹里面有

## 功能

1. Java Object -> Json String
2. Java Object -> Xml String
3. Json String -> Java Object
4. Xml String -> Java Object

## 还需要注意的地方

在app下的目录的build.gradle文件里面的安卓闭包加下面这个

~~~xml
android {
    ...

    packagingOptions {
        exclude 'META-INF/LICENSE'
        exclude 'META-INF/NOTICE'
    }
}
~~~



## 我用到的功能

这里就用快快送的里面的代码吧，我觉得写的很好，这个json库可以重复使用

~~~java
/**
 * Created by Azusa on 2016/1/24.
 */
public class JsonUtils {
  //饿汉式单例，线程安全，用来解析json的实例
  private static ObjectMapper mapper=new ObjectMapper();

    //将任意bean实例转换陈json字符串
    public static <T> String getJsonStringformat(T oject) {
        mapper = new ObjectMapper(); 
        String JsonString = "";
        try {
            JsonString = mapper.writeValueAsString(oject);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return JsonString;
        }
        return JsonString;
    }

    //将一个json字符串转成list
    public static ArrayList<String> getlistfromString(String string) {
        mapper = new ObjectMapper();
        ArrayList<String> list = new ArrayList<>();
        if (StringUtil.isEmpty(string)) {
            return list;
        }
        try {
            list = mapper.readValue(string, new TypeReference<ArrayList<String>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    //将一个Json字符串转换成对应的类
    public static <T> T getObjectfromString(String jsonString, Class<T> clazz) {
        mapper = new ObjectMapper();
        try {
            return mapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 原因就是因为jackcon无法按需解析，所以就用jsonObject方法来
     * @param json json数据
     * @param key  要获取的数据的key
     * @return 该key对应的值
     */
    public static String getStringbyKey(String json, String key) {
        String result = "";
        try {
            JSONObject jsonObject = new JSONObject(json);
            result = jsonObject.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
~~~

---

## 案例

这里我用快快送的登入的json来啦，至于你嘛，可以自己搭个服务器测试的，很多信息吧，刺激吧

~~~java
{"sessionId":"59FC33E584A5A660721DEA8A30FBE374",
  "user":{"id":"14d63c1d7fb04d9f82ac9e3eee1a7dc5",
          "loginPassword":"40BD001563085FC35165329EA1FF5C5ECBDBBEEF",
          "payPassword":"7C4A8D09CA3762AF61E59520943DC26494F8941B",
          "mobile":"123456",
          "name":"测试",
    	  "sex":"男",
          "identity":"445222197909082499",
          "path":"/Express/images/icon/1234563.jpg",
          "balance":375.15,
          "jifen":0,
          "pingjia":"态度好 ×2;速度快 ×0;包裹完好 ×2;好评 ×2;准时 ×0;",
          "pingfen":1.8,
          "order_count":15,
          "credit":-15,
          "accept_able":true,
          "send_able":true,
          "longitude":114.423542,
          "latitude":23.041771,
          "address":"惠州市 惠城区 东江西路 在惠大美术馆附近",
          "role":"courier",
          "state":"3",
          "pass":true,
          "forbidTime":1480594997000,
          "unionid":"oHPtgwI6nybiWtoY06ZjC0fOtNxQ",
          "openId":"oMKhzwSFI2XCiL5AE47SmQMcG_qo",
          "loginStatus":"on"},
 "status":"登录成功"}
~~~

---

在学数据库的时候就知道，读取数据后，一般都是映射成对象，后台其实也是数据库嘛，也是写个bean咯，依然是快快送的代码，写的也很漂亮，我就直接copy

~~~java
/**
 * Created by Azusa on 2016/7/19 0019.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private String id;//用户id
    private String loginPassword;//登录密码
    private String payPassword;//支付密码
    private String mobile;//手机号
    private String name;//真实姓名
    private String sex;//性别
    private String identity;//身份证号
    private String path;//用户头像位置
    private double balance;//余额
    private int jifen;//积分
    private String pingjia;//评价
    private float pingfen;//平均评分
    private int order_count;//历史接单数
    private int credit;//信誉值
    private boolean accept_able;//是否可接单
    private boolean send_able;//是否可发单
    private double longitude;//经度
    private double latitude;//纬度
    private String address;//用户当前定位地址
    private String role;//用户角色 normal:普通用户 courier:快递员
    private String state;//用户认证状态  0:未认证, 1:一级认证 ,2:二级认证,3:认证中，-1:认证失败
    private boolean pass;//是否免担保
    private Date forbidTime;//禁止接单日期
    private String loginStatus;//离线或在线：on:在线, off:离线
    private String openId; // 微信用户openId

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public boolean isPass() {
        return pass;
    }

    public void setPass(boolean pass) {
        this.pass = pass;
    }

    public Date getForbidTime() {
        return forbidTime;
    }

    public void setForbidTime(Date forbidTime) {
        this.forbidTime = forbidTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public String getPayPassword() {
        return payPassword;
    }

    public void setPayPassword(String payPassword) {
        this.payPassword = payPassword;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public int getJifen() {
        return jifen;
    }

    public void setJifen(int jifen) {
        this.jifen = jifen;
    }

    public String getPingjia() {
        return pingjia;
    }

    public void setPingjia(String pingjia) {
        this.pingjia = pingjia;
    }

    public float getPingfen() {
        return pingfen;
    }

    public void setPingfen(float pingfen) {
        this.pingfen = pingfen;
    }

    public int getOrder_count() {
        return order_count;
    }

    public void setOrder_count(int order_count) {
        this.order_count = order_count;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public boolean isAccept_able() {
        return accept_able;
    }

    public void setAccept_able(boolean accept_able) {
        this.accept_able = accept_able;
    }

    public boolean isSend_able() {
        return send_able;
    }

    public void setSend_able(boolean send_able) {
        this.send_able = send_able;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(String loginStatus) {
        this.loginStatus = loginStatus;
    }

  //可以不写这个
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", loginPassword='" + loginPassword + '\'' +
                ", payPassword='" + payPassword + '\'' +
                ", mobile='" + mobile + '\'' +
                ", name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", identity='" + identity + '\'' +
                ", path='" + path + '\'' +
                ", balance=" + balance +
                ", jifen=" + jifen +
                ", pingjia='" + pingjia + '\'' +
                ", pingfen=" + pingfen +
                ", order_count=" + order_count +
                ", credit=" + credit +
                ", accept_able=" + accept_able +
                ", send_able=" + send_able +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", address='" + address + '\'' +
                ", role='" + role + '\'' +
                ", state='" + state + '\'' +
                ", pass=" + pass +
                ", forbidTime=" + forbidTime +
                ", loginStatus='" + loginStatus + '\'' +
                '}';
    }
}
~~~

使用就很简单了，调用就好，就不写了