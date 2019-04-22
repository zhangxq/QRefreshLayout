package com.zhangxq.test.coordinatorlayout;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhangxq.refreshlayout.RefreshLayout;
import com.zhangxq.test.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangxiaoqi on 2019/4/22.
 */

public class PageFragment extends Fragment implements RefreshLayout.OnRefreshListener, RefreshLayout.OnLoadListener {
    private RecyclerView recyclerView;
    private RefreshLayout refreshLayout;

    private List<String> datas = new ArrayList<>();
    private RecyclerAdapter adapter;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page, null);
        recyclerView = view.findViewById(R.id.recyclerView);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setOnLoadListener(this);

        adapter = new RecyclerAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        down();
        return view;
    }

    private void down() {
        datas.clear();
        for (int i = 0; i < 20; i++) {
            datas.add("测试数据" + i);
        }
        adapter.notifyDataSetChanged();
    }

    private void up() {
        int size = datas.size();
        for (int i = size; i < size + 10; i++) {
            datas.add("测试数据" + i);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                down();
                refreshLayout.setRefreshing(false);
            }
        }, 1000);
    }

    @Override
    public void onLoad() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                up();
                refreshLayout.setLoading(false);
            }
        }, 1000);
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<ItemHolder> {

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ItemHolder(LayoutInflater.from(getActivity()).inflate(R.layout.view_list_item, null));
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            holder.tvTest.setText(datas.get(position));
            if (position % 2 == 0) {
                holder.tvTest.setBackgroundColor(0xffabcdef);
            } else {
                holder.tvTest.setBackgroundColor(0xffffffff);
            }
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        TextView tvTest;

        public ItemHolder(View itemView) {
            super(itemView);
            tvTest = itemView.findViewById(R.id.tvTest);
        }
    }
}
