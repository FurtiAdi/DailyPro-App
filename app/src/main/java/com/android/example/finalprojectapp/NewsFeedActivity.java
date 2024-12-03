package com.android.example.finalprojectapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.example.finalprojectapp.Adapter.NewsAdapter;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.kwabenaberko.newsapilib.NewsApiClient;
import com.kwabenaberko.newsapilib.models.Article;
import com.kwabenaberko.newsapilib.models.request.TopHeadlinesRequest;
import com.kwabenaberko.newsapilib.models.response.ArticleResponse;

import java.util.ArrayList;
import java.util.List;

public class NewsFeedActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Article> articleList = new ArrayList<>();
    NewsAdapter adapter;
    LinearProgressIndicator progressIndicator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("News");
        }

        recyclerView = findViewById(R.id.new_recycler_view);
        progressIndicator = findViewById(R.id.progress_bar);
        setRecyclerView();
        fetchNews();
    }

    void setRecyclerView(){
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NewsAdapter(articleList);
        recyclerView.setAdapter(adapter);
    }

    private void changeInProgress(boolean show){
        if (show){
            progressIndicator.setVisibility(View.VISIBLE);
        } else {
            progressIndicator.setVisibility(View.INVISIBLE);
        }
    }
    
    private void fetchNews(){
        changeInProgress(true);
        NewsApiClient newsApiClient = new NewsApiClient("5e1c20ba983c4102a546caaee6b29eed");
        newsApiClient.getTopHeadlines(
                new TopHeadlinesRequest.Builder().language("en").build(),
                new NewsApiClient.ArticlesResponseCallback(){
                    @Override
                    public void onSuccess(ArticleResponse response){
                        runOnUiThread(()->{
                            changeInProgress(false);
                            articleList.clear();
                            articleList.addAll(response.getArticles());
                            adapter.notifyDataSetChanged();
                        });
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.i("Got failure", throwable.getMessage());
                    }
                }
        );
    }
}
