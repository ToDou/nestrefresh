package com.todou.nestrefresh.example.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.todou.nestrefresh.LoadMoreFooterView
import com.todou.nestrefresh.base.OnLoadMoreListener
import com.todou.nestrefresh.example.R
import com.todou.nestrefresh.example.ui.adapter.RecyclerAdapterScroll
import com.todou.nestrefresh.example.ui.widget.ItemDecoration

import java.util.Collections

class InnerFooterFragment : androidx.fragment.app.Fragment() {

    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var adapter: RecyclerAdapterScroll
    private lateinit var loadMoreFooterView: LoadMoreFooterView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_inner_footer_recycler, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView.addItemDecoration(
            ItemDecoration(context, androidx.recyclerview.widget.RecyclerView.VERTICAL
                , resources.getDimensionPixelSize(R.dimen.margin_normal))
        )
        loadMoreFooterView = view.findViewById(R.id.view_footer)

        adapter = RecyclerAdapterScroll()
        recyclerView.adapter = adapter
        adapter.updateDatas(Collections.nCopies(20, Any()))


        loadMoreFooterView.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore() {
                loadMoreFooterView.postDelayed({
                    loadMoreFooterView.stopLoadMore()
                }, 2000)
            }
        })
    }

    companion object {
        fun newInstance(): InnerFooterFragment {
            return InnerFooterFragment()
        }
    }
}
