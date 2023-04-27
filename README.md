# AutoDingding
#### Kotlin+Java混编实现的钉钉打卡小工具，解决您上班途中迟到问题，只需一部备用手机置于公司工位，设置一下第二天上班打卡时间，接下来的事就交给我们吧。相比于之前的版本，此版本做了版本兼容，最低兼容 6.0，最高兼容到Android 12或者鸿蒙 3.0系统。
#### 此应用最开始的本意是方便自己，但后来本人换了新的单位，此款工具软件也就不用了，所以选择开源，有不到之处还请谅解。
#### 本应用仅限学习和内部使用，严禁商用和用作其他非法用途，如有违反，与本人无关！！！
#### 本应用的出发点是为了解决上班路途遥远，或者每天卡点上班族的燃眉之急，出发点自认为是友好的，但是，不可滥用！！！

# 使用注意事项：
#### 1、请先确认好通知栏监听已开启，如不开启将无法监听打卡成功的通知。（首次启动会有提示，会直接跳转到系统通知监听页面，打开开关就好了。放心，不会有其他窃密小动作）

![打开通知监听](https://github.com/AndroidCoderPeng/AutoDingding/blob/master/appImage/1.jpg)
![打开通知监听](https://github.com/AndroidCoderPeng/AutoDingding/blob/master/appImage/2.jpg)

#### 2、钉钉设置为“极速打卡”。

#### 3、设置打卡结果通知邮箱（经自测试，邮箱设置支持QQ邮箱和163邮箱，别的邮箱有需要的可以自行测试）

![添加邮箱](https://github.com/AndroidCoderPeng/AutoDingding/blob/master/appImage/3.jpg)

#### 好了，基本设置就是这样了，附一张主页面，如下：

![主页面](https://github.com/AndroidCoderPeng/AutoDingding/blob/master/appImage/4.jpg)
![主页面](https://github.com/AndroidCoderPeng/AutoDingding/blob/master/appImage/5.jpg)

#### 4、打卡结果如下：

#### a、打卡成功

![打卡成功](https://github.com/AndroidCoderPeng/AutoDingding/blob/master/appImage/6.png)

#### b、打卡失败（失败原因有很多，比如，钉钉账号被自己另一个手机挤下去，再比如，钉钉未设置极速打卡，或者钉钉应用内部打卡通知或者手机通知被关闭，或者钉钉打卡手机又2个以上，因为钉钉最多只能有两个常用打卡手机等等情况都会导致打卡失败，所以，在使用本软件之前，最好先自行测试一两天没确认没问题之后再使用，谢谢理解！）