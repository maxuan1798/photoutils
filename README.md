# 基于Fesco的Android图片选择器

![此处输入图片的描述][1]

![此处输入图片的描述][2]

这是根据自己实际项目写的一个图片选择器，加入了自己一些设计，也使用了一些开源代码，所以现在也开源出来啦...

使用方法：

 - 带删除功能的预览
```Java
//打开预览
ArrayList<String> images;
...
PhotoPreviewActivity.Preview(activity, images, position);
    
```
```Java
//获取删除后的图片
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == PictureSelectActivity.GALLARY_REQUEST) {
        List<String> photos = PictureSelectActivity.onPhotoSelected(requestCode, resultCode, data);
        //更新photos操作
    }
}
```
 - 不带删除功能的预览：
 
```Java
ArrayList<Photo> photoList;
...
PhotoPreviewActivity.Preview(context, photoList, position);
```
 - 图片选择
```Java
//maxCount为最多选择几张图片
PictureSelectActivity.openGallery(activity, maxCount);
```


  [1]: http://7xl4uu.com1.z0.glb.clouddn.com/FotorCreated.jpg
  [2]: http://7xl4uu.com1.z0.glb.clouddn.com/FotorCreated1.jpg
  [3]: https://github.com/maxuan1798/photoutils
