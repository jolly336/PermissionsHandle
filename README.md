# PermissionsHandle (Android 6.0 运行时权限处理)
> An easy-to-use library for handling Android M runtime permissions based on the Annotation Processor.

## Android权限说明

### 6.0以前权限一刀切
Android6.0以前的系统，所有权限都是一刀切处理方式，只要用户安装了应用，Manifest清单中申请的权限都会被赋予，且安装后撤销不了。当弹出安装对话框后，用户只有两个选择，要么选择安装，默认所有的敏感权限；要么拒绝安装应用。所以，这种一刀切的处理方式，我们是没有办法只允许某些权限或者拒绝某些权限。例如，小米5手机安装应用的情景。

<img src="/screenshots/app_install_before.png" width="280px"/>&emsp;<img src="/screenshots/app_installing.png" width="280px"/>

### 6.0运行时权限
从Android 6.0M 开始，系统引入了新的运行时权限机制。以某个需要拍照的应用为例，当运行时权限生效时，其Camera权限不是在安装后赋予，而是在应用运行的时候请求权限。比如当用户按下相机拍照按钮后，看到的效果是这样子的，接下来，对于Camera权限的处理权完全交给用户。

请求拍照时<img src="/screenshots/open_camera.png" width="280px"/>

### 权限的分组
6.0系统对权限进行了分组，一般包括如下几类：
* 正常权限（Normal Protection）
* 危险权限（Dangerous）
* 特殊权限（Particular）
* 其他权限（一般很少用到）

正常权限一般不涉及用户隐私，是不需要用户进行授权的，比如访问网络，手机震动等；危险权限一般是涉及用户隐私的，需要用户进行授权，比如读取手机Sdcard,访问通讯录，打开相机等。

**Normal Permissions如下**
1. ACCESS_NETWORK_STATE
2. VIBRATE
3. NFC
4. BLUETOOTH
...

**Dangerous Permissions如下**
1. group:android.permission-group.CONTACTS
  permission:android.permission.WRITE_CONTACTS
  permission:android.permission.GET_ACCOUNTS
  permission:android.permission.READ_CONTACTS
2. group:android.permission-group.CAMERA
  permission:android.permission.CAMERA
...


可以通过adb shell pm list permissions -d -g进行查看。
看到上面的dangerous permission会发现危险权限都是一组一组的，分组对于我们会有什么影响吗？的确是有影响的，如果app运行在Android 6.x的系统上，对于授权机制是这样子的，如果你申请某个危险的权限，假设你的app早已被用户授予了同一组的某个危险权限，那么系统会立即授权，而不需要弹窗提示用户点击授权。对于申请时弹出的dialog上面的文本说明也是对整个权限组的说明，而不是单个权限。（注意：权限dialog是不能进行定制的）。
ps：不过需要注意的是，不要对权限组过多的依赖，尽可能对每个危险权限都进行正常的申请，以为在后面的版本权限组则可能发生变化！

### 必须要支持运行时权限么
目前应用实际上是可以不需要支持运行时权限的，但是最终肯定还是需要支持的，只是时间问题而已。

想要不支持运行时权限很简单，只需要将targetSdkVersion设置低于23就可以了，意思是告诉系统，我还没有完全在API 23（6.0）上完全搞定，不要给我启动新的特性。

### 不支持运行时权限会崩溃么
可能会，但不是那种一上来就噼里啪啦崩溃不断的那种。

如果你的应用将targetSdkVersion设置低于23，那么在6.x的系统上不会为这个应用开启运行时权限机制，即按照以前的一刀切方式处理。
然而，6.x系统提供了一个应用权限管理界面，界面长得是这样子的

6.0应用权限管理界面<img src="/screenshots/runtime_permission_manage.png" width="280px"/>

既然是可以管理的，用户就能取消权限，当一个不支持运行时权限的应用某项权限被取消时，系统会弹出一个对话框提醒撤销的危害，如果用户执意撤销，会带来如下反应：
* 如果你的应用程序在运行，则会被杀掉
* 当你的应用再次运行时，可能出现崩溃

为什么会可能崩溃的，比如下面这段代码

```java
TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
String deviceId = telephonyManager.getDeviceId();
if (deviceId.equals(mLastDeviceId)) {
  //do something here
}
```
如果用户撤销了获取DeviceId的权限，那么再次运行时，deviceId就是null,如果程序后续处理不当，就会出现崩溃。

## 相关API
6.0运行时权限，我们最终都是要支持的，通常我们需要使用如下的API

* int checkSelfPermission(String permission) 用来检测应用是否已经具有权限
* void requestPermissions(String[] permissions, int requestCode) 进行请求单个或多个权限
* void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) 用户对请求作出响应后的回调
* shouldShowRequestPermissionRationale 判断接下来的对话框是否包含”不再询问“选择框。

以请求Camera权限为例

```java
private static final int REQUEST_PERMISSION_CAMERA_CODE = 1;
@Override
public void onClick(View v) {
    if (!(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
        requestCameraPermission();
    }
}

private void requestCameraPermission() {
    requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA_CODE);
}

@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQUEST_PERMISSION_CAMERA_CODE) {
        int grantResult = grantResults[0];
        boolean granted = grantResult == PackageManager.PERMISSION_GRANTED;
        Log.i(TAG, ">>> onRequestPermissionsResult camera granted = " + granted);
    }
}
```

当用户选择允许，我们就可以在onRequestPermissionsResult方法中进行响应的处理，比如打开摄像头，进行下一步操作。当用户拒绝，你的应用可能就开始危险了，当我们再次尝试申请权限时，弹出的对话框和之前有点不一样了，主要表现为多了一个checkbox复选框。如下图

再次请求拍照时<img src="/screenshots/open_camera_again.png" width="280px"/>

当用户勾选了”不再询问“拒绝后，你的程序基本这个权限就Game Over了。
不过，你还有一丝希望，那就是再出现上述的对话框之前做一些说明信息，比如你使用这个权限的目的（一定要坦白）。

shouldShowRequestPermissionRationale这个API可以帮我们判断接下来的对话框是否包含”不再询问“选择框。

### 一个标准的申请流程

```java
if (!(checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)) {
  if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
      Toast.makeText(this, "Please grant the permission this time", Toast.LENGTH_LONG).show();
    }
    requestReadContactsPermission();
} else {
  Log.i(TAG, "onClick granted");
}
```

### 批量申请
批量申请权限很简单，只需要字符串数组放置多个权限即可。如请求代码

```java
private static final int REQUEST_CODE = 1;
private void requestMultiplePermissions() {
    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE};
    requestPermissions(permissions, REQUEST_CODE);
}
```
注意：间隔较短的多个权限申请建议设置成单次多个权限申请形式，避免弹出多个对话框，造成不太好的视觉效果。
### 注意事项
由于checkSelfPermission和requestPermissions从API 23才加入，低于23版本，需要在运行时判断 或者使用Support Library v4中提供的方法
* ContextCompat.checkSelfPermission
* ActivityCompat.requestPermissions
* ActivityCompat.shouldShowRequestPermissionRationale

### 多系统问题
当我们支持了6.0必须也要支持4.4，5.0这些系统，所以需要在很多情况下，需要有两套处理。比如Camera权限
```java
if (Util.isOverMarshmallow()) {
    requestPermission();//6.x申请权限
} else {
    openCamera();//低于6.0直接使用Camera
}
```

## PermissionHandle库使用（封装）
虽然权限处理并不复杂，但是需要编写很多重复的代码，PermissionHandle借鉴[PermissionGen](https://github.com/lovedise/PermissionGen)库来封装的，基于Annotation Processor编译时注解的方式来实现运行时权限申请回调。

### 使用
* 申请权限

```java
@OnClick(R.id.btn_camera)
    public void open(View view) {
        if (view.equals(mBtnOpenCamera)) {
            PermissionsHandle.requestPermissions(this, REQUEST_CODE_OPEN_CAMERA, Manifest.permission.CAMERA);
        }
    }
```
* 根据授权情况进行回调

```java
    // open camera
    @PermissionGrant(REQUEST_CODE_OPEN_CAMERA)
    public void requestCameraSucess() {
        Toast.makeText(mContext, "Grant app access camera!", Toast.LENGTH_SHORT).show();
    }

    @PermissionDenied(REQUEST_CODE_OPEN_CAMERA)
    public void requestCameraFailed() {
        Toast.makeText(mContext, "Deny app access camera!!!", Toast.LENGTH_SHORT).show();
    }
    
    // request result
      @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsHandle.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
```
思路：反射实例化注解处理器生成的代理类，根据注解和requestCode找到方法，然后执行即可。详细请看[PermissionsHandle](https://github.com/haoxunwang/PermissionsHandle)

### 框架依赖
1. sample依赖permission-api，使用apt插件编译permission-compiler；
2. permission-api依赖permission-annotation

## Thanks
* [PermissionGen](https://github.com/lovedise/PermissionGen)
* [MPermissions](https://github.com/hongyangAndroid/MPermissions)
* [聊一聊Android 6.0的运行时权限](http://droidyue.com/blog/2016/01/17/understanding-marshmallow-runtime-permission/)
* Java api的方式来生成代码[javapoet](https://github.com/square/javapoet)
* com.google.auto.service:auto-service:1.0-rc2，auto-service库生成META-INF等信息

# Contacts
 * Email:haoxunwang525@163.com

