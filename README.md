# nestrefresh  

One pull refresh kit by nest  
By this library, you can add Pull Refresh by behaviors for one groupview or view with nest child. You can add pull refresh for one top header and scroll top with sticky area. You can add CollapseToolbarLayout in the RefreshBarLayout to collapse when scroll up, pin tab layout on the top and pull refresh when pull down. 
You can add load more footer behavoir for your recyclerview. When you pull up at last of the recyclerview, the footer will be pull up with spring effect. You can set no more result when data have no more.

Screenshot
====
|||||
|---|---|---|---|
|RefreshBar with Collapse|Header Nest Scroll|RefreshBar|Single Refresh|
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
    ```xml
    @string/nest_refresh_load_more_behavior
    ```
* RefreshScrollBehavior
    ```xml
    @string/nest_refresh_scroll_behavior
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

