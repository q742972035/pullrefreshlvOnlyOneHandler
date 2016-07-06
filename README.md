# pullrefreshlvOnlyOneHandler
下拉刷新和上拉加载，不允许滑动打断

- 具体流程：
 先在oncreate中找到该控件：  

 lv = (PullRefreshListView) findViewById(R.id.lv_pullrefresh);
 
- 得到建造者：

  PullRefreshListView.Builder builder = lv.new Builder(lv);

builder.setDamp(0.3f)
                .setDuration(500)
                .setFooterView(View view)
                .setHeaderView(View view);

建造者允许连号，分别是设置阻尼系数、设置回弹时间、设置脚布局、设置头布局。

- 中断加载

builder.closeLoading();
- 中断刷新

builder.closeRefreshing();

- 给PullRefreshListView设置监听

 lv.setStateCallBace(State CallBack callback);

## 回调方法何时调用 ##
- 当状态为“松开刷新”时 

void toLoosenRefresh();

- 当状态为“下拉刷新”时

void toRullRefresh();

- 当状态为“正在刷新”时

void toRefreshing();

- 当状态为“正在加载”时

void toLoading();

- 从拖拽高度开始计，状态从“下拉刷新”变为“松开刷新”的过程
- percent 值为0-1
- dY 在过程中有值，在过程外值为0

void dragToLoosen(float percent, int dY);

- 从拖拽高度开始计，状态从“下拉刷新”一直往下拖拽到无穷
- percent 值为0-正无穷
- dY 始终有值

void drag(float percent, int dY);

- 调用builder.closeRefreshing()执行

void stopRefresh();

- 调用builder.closeLoading()执行

void stopLoad();

- 滑动的时候会调用的方法
- scrollState值只有 SCROLL_STATE_FLING 或者 SCROLL_STATE_TOUCH_SCROLL

void scroll(AbsListView view, int scrollState);
