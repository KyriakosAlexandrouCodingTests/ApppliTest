package com.kyriakosalexandrou.ampersandtest.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kyriakosalexandrou.ampersandtest.R;
import com.kyriakosalexandrou.ampersandtest.Util;
import com.kyriakosalexandrou.ampersandtest.events.ErrorEvent;
import com.kyriakosalexandrou.ampersandtest.events.NewsFeedEvent;
import com.kyriakosalexandrou.ampersandtest.models.NewsFeed;
import com.kyriakosalexandrou.ampersandtest.services.NewsFeedService;
import com.kyriakosalexandrou.ampersandtest.ui.activities.BaseActivity;
import com.kyriakosalexandrou.ampersandtest.ui.adapters.NewsFeedAdapter;
import com.kyriakosalexandrou.ampersandtest.ui.decorations.VerticalSpaceItemDecoration;
import com.kyriakosalexandrou.ampersandtest.widgets.AppSwipeRefreshLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by Kyriakos on 25/06/2016.
 */
public class NewsFeedFragment extends BaseFragment implements NewsFeedAdapter.NewsFeedAdapterCallback {
    public static final String TAG = NewsFeedFragment.class.getName();
    private static final int VERTICAL_ITEM_SPACE = 40;
    private final NewsFeedService mNewsFeedService = new NewsFeedService(BaseActivity.REST_ADAPTER);

    private RecyclerView mNewsFeedRecycler;
    private AppSwipeRefreshLayout mAppSwipeRefreshLayout;

    private NewsFeed mNewsFeed;
    private NewsFeedAdapter mNewsFeedAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_feed, container, false);
        bindViews(view);

        mNewsFeedAdapter = new NewsFeedAdapter(getContext(), this);
        setUpNewsFeedRecycler();
        setUpSwipeRefreshLayout();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        requestNewsFeedData();
    }

    private void bindViews(View view) {
        mNewsFeedRecycler = (RecyclerView) view.findViewById(R.id.news_feed);
        mAppSwipeRefreshLayout = (AppSwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
    }

    private void requestNewsFeedData() {
        mAppSwipeRefreshLayout.setRefreshing(true);
        ErrorEvent errorEvent = new ErrorEvent(getString(R.string.request_failure_news_feed));
        mNewsFeedService.getNewsFeedRequest(new NewsFeedEvent(errorEvent));
    }

    private void setUpNewsFeedRecycler() {
        mNewsFeedRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mNewsFeedRecycler.setHasFixedSize(false);
        mNewsFeedRecycler.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));
        mNewsFeedRecycler.setAdapter(mNewsFeedAdapter);
    }

    private void setUpSwipeRefreshLayout() {
        mAppSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestNewsFeedData();
            }
        });
    }

    @Subscribe
    public void onEventMainThread(NewsFeedEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        onEventCalledCommonActions();
        updateNewsFeed(event.getNewsFeed());
    }

    private void updateNewsFeed(NewsFeed newsFeed) {
        mNewsFeed = newsFeed;
        mNewsFeedAdapter.clear();
        mNewsFeedAdapter.addAll(mNewsFeed.getData());
    }

    @Subscribe
    public void onEventMainThread(ErrorEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        onEventCalledCommonActions();

        Util.showSnackbar(mCoordinatorLayout, event.getErrorMessage(), getResources().getString(R.string.retry), Snackbar.LENGTH_INDEFINITE, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestNewsFeedData();
            }
        });
    }

    private void onEventCalledCommonActions() {
        mAppSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onNewsFeedItemClicked(int position) {
        mNewsFeedRecycler.smoothScrollToPosition(position);
    }
}