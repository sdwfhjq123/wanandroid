package com.yinhao.wanandroid.ui.fragment.projectList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.yinhao.wanandroid.base.BaseFragment
import com.yinhao.wanandroid.databinding.FragmentProjectListBinding
import com.yinhao.wanandroid.model.bean.ArticleBean
import com.yinhao.wanandroid.ui.content.ContentActivity
import org.jetbrains.anko.support.v4.toast

/**
 * author:  yinhao
 * date:    2020/7/25
 * version: v1.0
 * ### description:
 */
class ProjectListFragment : BaseFragment<ProjectListViewModel, FragmentProjectListBinding>() {
    private var mCId: Int = -1
    private val mAdapter by lazy { ProjectAdapter() }

    companion object {
        const val KEY_CID = "com.yinhao.wanandroid.ui.fragment.projectList.ProjectListFragment.cId"

        fun newInstance(cid: Int): ProjectListFragment {
            val fragment = ProjectListFragment()
            val args = Bundle()
            args.putInt(KEY_CID, cid)
            fragment.arguments = args
            return fragment
        }
    }

    override fun initViewModel(): ProjectListViewModel =
        ViewModelProvider(this).get(ProjectListViewModel::class.java)

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProjectListBinding = FragmentProjectListBinding.inflate(layoutInflater)

    override fun initView() {
        viewBinding?.multipleStatusView?.showLoading()
        initRecyclerView()
        initRefresh()
        viewObserver()

        mAdapter.setOnItemClickListener { adapter, view, position ->
            if (adapter.data.size != 0) {
                val data = adapter.data[position] as ArticleBean
                ContentActivity.actionStart(activity, data.id, data.title, data.link)
            }
        }
    }

    override fun initData() {
        mCId = arguments?.getInt(KEY_CID) ?: -1
        viewModel.getProjectList(mCId, true)
    }

    private fun viewObserver() {
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
                toast(message)
                viewBinding?.multipleStatusView?.showError()
            }
        }
    }

    private fun initRecyclerView() {
        viewBinding?.recyclerView?.run {
            layoutManager = LinearLayoutManager(activity)
            adapter = mAdapter
        }
    }

    private fun initRefresh() {
        viewBinding?.refreshLayout?.run {
            setRefreshHeader(ClassicsHeader(activity))
            setRefreshFooter(ClassicsFooter(activity))
            setOnRefreshLoadMoreListener(object :
                OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    viewModel.getProjectList(mCId, false)
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {
                    viewModel.getProjectList(mCId, true)
                }
            })
        }
    }
}