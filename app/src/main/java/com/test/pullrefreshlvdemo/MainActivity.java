package com.test.pullrefreshlvdemo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.test.pullrefreshlvdemo.view.PullRefreshListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PullRefreshListView.StateCallBack{

    private PullRefreshListView lv;
    private View header;
    private View footer;
    private ImageView iv;
    private TextView tv;
    private ProgressBar pb;
    private int refresh;
    private int counts;
    private Adapter a;

    /**
     * listview的建造者，里面封装了额外的方法
     */
    private PullRefreshListView.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (PullRefreshListView) findViewById(R.id.lv_pullrefresh);
        builder = lv.new Builder(lv);
        init();
        iv = (ImageView) header.findViewById(R.id.iv_rotate);
        tv = (TextView) header.findViewById(R.id.tv_text);
        pb = (ProgressBar) header.findViewById(R.id.pb);
    }

    List<String> list;
    private void init() {
        list = new ArrayList<>();
        for (int i=0;i<15;i++){
            list.add(String.valueOf(counts++)+"　　第"+refresh+"次刷新。");
        }
        a = new Adapter(this,list);
        lv.setAdapter(a);
        builder.setHeaderView(header =View.inflate(this,R.layout.header_view,null));
        builder.setFooterView(footer = View.inflate(this,R.layout.footer_view,null));
        lv.setStateCallBace(this);
    }

    @Override
    public void toLoosenRefresh() {
        tv.setText("松开刷新");
    }

    @Override
    public void toRullRefresh() {
        tv.setText("下拉刷新");
        iv.setVisibility(View.VISIBLE);
        pb.setVisibility(View.GONE);
    }

    private Handler h ;
    private Runnable r;
    @Override
    public void toRefreshing() {
        tv.setText("正在刷新");
        iv.setVisibility(View.GONE);
        pb.setVisibility(View.VISIBLE);
        h =new Handler();
        h.postDelayed(r =new Runnable() {
            @Override
            public void run() {
                counts = 0;
                list.clear();
                refresh++;
                for (int i=0;i<20;i++){
                    list.add(String.valueOf(counts++)+"　　第"+refresh+"次刷新。");
                }
                a.notifyDataSetChanged();
                builder.closeRefreshing();
            }
        },3000);
    }

    @Override
    public void toLoading() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i=0;i<20;i++){
                    list.add(String.valueOf(counts++)+"　　第"+refresh+"次刷新。");
                }
                a.notifyDataSetChanged();
                builder.closeLoading();
            }
        },2000);
    }

    @Override
    public void dragToLoosen(float percent, int dY) {
        iv.setRotation(180*percent);
    }

    @Override
    public void drag(float percent, int dY) {
    }

    @Override
    public void stopRefresh() {
        h.removeCallbacks(r);
        Toast.makeText(this, "停止刷新不允许通过手动滑动停止", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void stopLoad() {
        Toast.makeText(this, "停止加载不允许通过手动滑动停止", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void scroll() {

    }
}
