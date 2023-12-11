### 介绍
KCamera 是基于Camera2 API 进行封装可以灵活，快速的接入Camera库， 这里不介绍架构设计实现，主要介绍如果快速使用

### 支持功能
1. 拍照
2. 变焦
3. 手动对焦
4. 曝光补偿
5. 获取相机预览数据
6. 自定义相机参数
7. 预览画面镜像

### 工程结构
1. `KCamera` : Camera2 API 的核心控制库
2. `KGLCamera` : 是基于`KCamera` 库实现的以GL为基础的一些功能

这里没有配置Maven，需要下载源码进行自己打包， 打包方法不介绍

### 接入
使用这个Camera库`KCamera`这个类一定要先了解一下，这个类是控制Camera的入口。
#### 预览
预览很简单，只需要下面4步
1. 创建预览`View`， 可以是`TextureView` 也可是 `GLSurfaceView`
2. 实现`PreviewSurfaceProvider` 接口，并将预览`View`和 `PreviewSurfaceProvider`进行绑定
3. 使用`PreviewRequest.createBuilder()` 创建预览`Builder`
4. 创建`KCamera` 实例并调用`openCamera`方法启动预览

上面简单几步即可完成预览，下面介绍如何实现一个预览
1. 创建预览`View`，详细可以参考项目中`CameraGLView`（`GLView`代码到项目中自行查看吧）

```
public class CameraGLView extends GLView {
  .......
}
```
2. 实现`PreviewSurfaceProvider` 接口， 详细可以参考项目中`GLViewProvider`

```
public class GLViewProvider implements PreviewSurfaceProvider {
    ......
}
```
3. 使用`PreviewRequest.createBuilder()` 创建预览`Builder`

```
val backImageReader = PreviewYuvImageReader(backYuvSize, listener)
val builder = PreviewRequest.createBuilder()
    .setPreviewSize(backPreviewSize)  // 预览数据分辨率
    .openBackCamera() // 打开后摄
    .addPreviewSurfaceProvider(provider) // 设置预览View provide
    .setPictureSize(backPicSize, ImageFormat.YUV_420_888)  // 设置拍照分辨率
    .setFlash(FlashState.OFF) // 设置打开时闪光灯
    .setCustomerRequestStrategy(BackCustomerRequestStrategy())  // 设置自定义策略
    .addSurfaceProvider(backImageReader)  // 添加一路输出图像数据, 可以是实时预览数据，也可以是拍照数据
```
4. 创建`KCamera` 实例并调用`openCamera`方法启动预览, `openCamera`需要两个参数，
   `PreviewRequest` ： 启动相机的一些参数，在第三步中已经介绍
   `CameraStateListener`：使用过程中相机状态的监听
```
val builder = PreviewRequest.createBuilder() // 上面的builder
val kCamera: KCamera = KCamera(context)
kCamera.openCamera(builder.builder(), cameraListener)
```

### 功能介绍

| API | 功能 |
| --- | --- |
| KCamera.takePic(final CaptureStateListener listener) | 拍照 |
| KCamera.setZoom(int value) |  变焦设置（0 ~ 100）|
| KCamera.setEv(int value) |  曝光设置|
| KCamera.setFocus(float touchX, float touchY, int afTouchViewWidth, int afTouchViewHeight)|  手动对焦|
| KCamera.setCustomRequest(KCustomerRequestStrategy strategy)|  自定义相机参数|
| GLView.setMirrorView(boolean isMirror) |  预览画面镜像（这个功能是在GLView 中）|



下面单独介绍一下拍照的使用：
实现拍照只要两步：
1. 实现拍照的`SurfaceProvider`并在拍照前在`openCamera` 时创建`PreviewRequest`通过`addSurfaceProvider`函数配置拍照`SurfaceProvider`进行配置,具体实现参考项目`CaptureJPEGImageReader`
2. 使用`KCamera.takePic(CaptureStateListener)`函数进行拍照， `CaptureStateListener`是对拍照状态的监听

上面简单介绍功能，功能详细使用方法参考项目中实现