# nestrefresh  

One pull refresh kit by nest scroll  

By this library, you can add Pull Refresh by behaviors for one groupview or view with nest child. You can add pull refresh for one top header and scroll top with sticky area. You can add CollapseToolbarLayout in the RefreshBarLayout to collapse when scroll up, pin tab layout on the top and pull refresh when pull down. 
You can add load more footer behavoir for your recyclerview. When you pull up at last of the recyclerview, the footer will be pull up with spring effect. You can set no more result when data have no more.

[Download Sample](https://github.com/ToDou/nestrefresh/releases)

Screenshot
====
|RefreshBar with Collapse|Header Nest Scroll|RefreshBar|Single Refresh|
|---|---|---|---|
|![](/screenshot/nest_refresh_refreshbar_collapse.gif)|![](/screenshot/nest_refresh_header_nest_scroll.gif)|![](/screenshot/nest_refresh_refresh_bar.gif)|![](/screenshot/nest_refresh_single.gif)|

Installation
====
```groovy
dependencies {
    implementation 'com.github.todou:nestrefresh:0.0.3'
}
```
Usages
====
All behavoir as follows:
* RefreshBarBehavior
    ```
    Defualt behavior for RefreshBarLayout
    ```
* RefreshBarScrollBehavior
    ```xml
    @string/nest_refresh_bar_scroll_behavior
    ```
* RefreshBehavior
    ```xml
    @string/nest_refresh_single_behavior
    ```
* LoadMoreBehavior
    ```
    # default to LoadMoreFooterView
    @string/nest_refresh_load_more_behavior
    ```
* RefreshScrollBehavior
    ```xml
    @string/nest_refresh_scroll_behavior
    ```

### Single Nest Refresh with load more footer
```xml
<android.support.design.widget.CoordinatorLayout
    ...>

    <com.todou.nestrefresh.RefreshHeaderView
        ...
        app:layout_behavior="@string/nest_refresh_single_behavior"/>

    <android.support.v7.widget.RecyclerView
        ...
        app:layout_behavior="@string/nest_refresh_scroll_behavior"/>

    <com.todou.nestrefresh.LoadMoreFooterView
        .../>

</android.support.design.widget.CoordinatorLayout>
```
LoadMoreFooterView's default behavior is LoadMoreBehavior, so don't need to add behavior by yourself. And you can write your custom refresh header and custom load more footer with the behavior in the lib.
### RefreshBarLayout Refresh with sticky
```xml
<android.support.design.widget.CoordinatorLayout
    ...>

    <com.todou.nestrefresh.RefreshBarLayout
        ...>

        <com.todou.nestrefresh.RefreshHeaderView
            ...
            app:nr_layout_scrollFlags="flag_refresh_header"/>
 
        ...
        
        <android.support.design.widget.TabLayout
            ...
            app:nr_layout_scrollFlags="flag_sticky"/>
        
    </com.todou.nestrefresh.RefreshBarLayout>

    <android.support.v4.view.ViewPager
        ...
        app:layout_behavior="@string/nest_refresh_bar_scroll_behavior"/>

</android.support.design.widget.CoordinatorLayout>

```
### RefreshBarLayout Refresh with collapse toolbar
For inset status bar effect, you must add fitsSystemWindows true. And RefreshHeaderView in the default lib support compat inset
```xml
<android.support.design.widget.CoordinatorLayout
    ...>

    <com.todou.nestrefresh.RefreshBarLayout
        android:fitsSystemWindows="true">

        <com.todou.nestrefresh.RefreshHeaderView
            app:nr_layout_scrollFlags="flag_refresh_header"/>

        <com.todou.nestrefresh.NRCollapsingToolbarLayout
            android:fitsSystemWindows="true"
            app:nr_layout_scrollFlags="flag_collapse">

            <ImageView
                ...
                android:fitsSystemWindows="true"
                app:nr_layout_collapseMode="parallax"/>

            <android.support.v7.widget.Toolbar
                app:nr_layout_collapseMode="pin"/>

        </com.todou.nestrefresh.NRCollapsingToolbarLayout>

    </com.todou.nestrefresh.RefreshBarLayout>

    <android.support.v4.widget.NestedScrollView
        ...
        app:layout_behavior="@string/nest_refresh_bar_scroll_behavior">

        ...

    </android.support.v4.widget.NestedScrollView>

</android.support.design.widget.CoordinatorLayout>
```
### Load More Footer
If you want to add load more footer for whole scroll child such as viewpager. You can add LoadMoreBehavior directly. If you want add load more footer for recyclerview in fragment of ViewPager, you must wrapper every recyclerview with **ChildCoordinatorLayout**. ChildCoordinatorLayout will send unconsumeY to parent and then send to child behavior to itself.
Like this in fragment layout of viewpager:
```xml
<com.todou.nestrefresh.ChildCoordinatorLayout
    ...>

    <android.support.v7.widget.RecyclerView
        ...
        app:layout_behavior="@string/nest_refresh_scroll_behavior"/>

    <com.todou.nestrefresh.LoadMoreFooterView
        .../>

</com.todou.nestrefresh.ChildCoordinatorLayout>
```
### Add refresh callback
```java
view_refresh_header.setOnRefreshListener(object : OnRefreshListener {
    override fun onRefresh() {
        //Todo test code when network finish, you muast stop refresh by view_refresh_header.stopRefresh()
        view_refresh_header.postDelayed({
            view_refresh_header.stopRefresh()
        }, 2000)
    }
})
        
```
### Add load more callback
```java
view_footer.setOnLoadMoreListener(object : OnLoadMoreListener {
    override fun onLoadMore() {
        //Todo test code when network finish stop load more
        view_footer.postDelayed({
            view_footer.stopLoadMore()
        }, 2000)
    }
})

//Set has more 
view_footer.setHasMore(hasMore)
```
License
====
<pre>
Copyright 2019 ToDou

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
</pre>

