package com.yinhao.wanandroid.ui.fragment.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.jeremyliao.liveeventbus.LiveEventBus
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.yinhao.wanandroid.base.BaseFragment
import com.yinhao.wanandroid.databinding.FragmentHomeBinding
import com.yinhao.wanandroid.model.bean.ArticleBean
import com.yinhao.wanandroid.model.bean.BannerBean
import com.yinhao.wanandroid.ui.content.ContentActivity
import com.youth.banner.indicator.CircleIndicator
import com.youth.banner.listener.OnBannerListener
import org.jetbrains.anko.toast

/**
 * author:  yinhao
 * date:    2020/7/8
 * version: v1.0
 * ### description:
 */
class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>() {
    private val mAdapter by lazy { ArticleAdapter() }

    override fun initViewModel(): HomeViewModel =
        ViewModelProvider(this).get(HomeViewModel::class.java)

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding = FragmentHomeBinding.inflate(layoutInflater)

    override fun initView() {
        viewBinding?.multipleStatusView?.showLoading()
        initBanner()
        initRecyclerView()
        initRefresh()
        viewModelObserver()
    }

    override fun initData() {
        viewModel.getHomeBanner()
        viewModel.getArticleList(true)
    }

    private fun initRecyclerView() {
        viewBinding?.recyclerView?.run {
            layoutManager = LinearLayoutManager(activity)
            adapter = mAdapter
        }

        mAdapter.setOnItemClickListener { adapter, view, position ->
            if (adapter.data.size != 0) {
                val data = adapter.data[position] as ArticleBean
                ContentActivity.actionStart(activity, data.id, data.title, data.link)
            }
        }
    }

    private fun viewModelObserver() {
        LiveEventBus.get("switch_show_top", Boolean::class.java).observe(this, Observer {
            viewModel.getArticleList(true)
        })


        viewModel.bannerData.observe(this) {
            viewBinding!!.banner.setDatas(it).start()
        }

        viewModel.uiState.observe(this) {
            it.showSuccess?.let { list ->
                mAdapter.run {
                    if (it.isRefresh) {
                        replaceData(list)
                        viewBinding?.refreshLayout?.finishRefresh()
                    } else {
                        addData(list)
                        viewBinding?.refreshLayout?.finishLoadMore(true)
                    }
                }
                viewBinding?.multipleStatusView?.showContent()
            }

            if (it.showEnd) viewBinding?.refreshLayout?.finishLoadMoreWithNoMoreData()

            it.showError?.let { message ->
                activity?.toast(if (message.isBlank()) "网络异常" else message)
                viewBinding?.multipleStatusView?.showError()
            }
        }
    }

    private fun initRefresh() {
        viewBinding?.refreshLayout?.run {
            setRefreshHeader(ClassicsHeader(activity))
            setRefreshFooter(ClassicsFooter(activity))
            setOnRefreshLoadMoreListener(object :
                OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    viewModel.getArticleList(false)
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {
                    viewModel.getArticleList(true)
                }
            })
        }
    }

    private fun initBanner() {
        viewBinding!!.banner.addBannerLifecycleObserver(activity)//添加生命周期观察者
            .setAdapter(ImageAdapter(null)).indicator = CircleIndicator(activity)

        viewBinding.banner.setOnBannerListener { data: Any?, position: Int ->
            val bean = data as BannerBean
            ContentActivity.actionStart(activity, bean.id.toInt(), bean.desc, bean.url)
        }
    }

}