##PageAndPullToRefresh
An android open source for ListView with paging request and pull to refresh actions.

The project's encoding is utf-8

###分页加载和下拉刷新，几行代码完全搞定

本项目封装了分页请求和下拉刷新的全部过程，开发者只需要关注ListView子项视图的绘制和ListView数据源的请求。 整个页面的逻辑处理变得非常非常简单

另外，还封装了列表请求数据失败或者无数据的情况下显示的视图样式。
``` java
        // 此方法添加列表数据为空的显示图片
        mListView.setNoDataImage(R.drawable.ic_launcher, Gravity.CENTER);
        // 此方法添加数据请求失败时的显示图片
        mListView.setNoLinkImage(R.drawable.ic_launcher, Gravity.CENTER);
        // 此方法添加列表数据为空时显示的自定义视图
        mListView.addNoDataView(LayoutInflater.from(this).inflate(R.layout.header, null), Gravity.CENTER);
        // 此方法添加数据请求失败时显示的自定义视图
        mListView.addNoLinkView(LayoutInflater.from(this).inflate(R.layout.header, null), Gravity.CENTER);
```
ListView效果分以下类型：

##1：普通的列表，包含分页加载和下拉刷新

![Screenshot](https://raw.githubusercontent.com/tubeber/PageAndPullToRefresh/master/demo/res/drawable-hdpi/screenshot02.png)

Layout
``` xml
<com.king.refresh.widget.PageAndRefreshListView
        android:id="@+id/demo_page_list"
        android:layout_width="fill_parent"
        android:layout_height="fill_parent"
        android:fadingEdge="none"
        android:fadingEdgeLength="0px"
        android:scrollbars="none"
        refresh:pageDemandingEnable="true"
        refresh:refreshable="true" />
```

##2：图片拉伸的列表，包含分页加载和下拉刷新。类似于QQ空间效果
![Screenshot](https://raw.githubusercontent.com/tubeber/PageAndPullToRefresh/master/demo/res/drawable-hdpi/screenshot01.png)

##3：瀑布流 + 图片拉伸的列表，包含分页加载和下拉刷新。
使用方法同2
![Screenshot](https://raw.githubusercontent.com/tubeber/PageAndPullToRefresh/master/demo/res/drawable-hdpi/screenshot03.png)

