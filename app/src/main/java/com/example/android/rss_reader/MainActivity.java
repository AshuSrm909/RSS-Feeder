package com.example.android.rss_reader;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView lnews;
    ArrayList<String> titles;
    ArrayList<String> links;
    SwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        swipeRefreshLayout=(SwipeRefreshLayout) findViewById(R.id.swipe);
        titles=new ArrayList<String>();
        links=new ArrayList<String>();
        lnews = (ListView) findViewById(R.id.listnews);
        lnews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Uri uri=Uri.parse(links.get(i));
                Intent intent=new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
            }
        });
        new ProcessInBackground().execute();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new ProcessInBackground().execute();
                final ProgressDialog progressDialog=new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Loading...");
                progressDialog.show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        progressDialog.dismiss();
                    }
                },4000);
            }
        });
    }

    public InputStream getInputStream(URL url){
        try
        {
            return url.openConnection().getInputStream();
        }
        catch (IOException e)
        {
            return null;
        }
    }
    @SuppressLint("StaticFieldLeak")
    public class ProcessInBackground extends AsyncTask<Integer,Void,Exception>{

        ProgressDialog progressDialog=new ProgressDialog(MainActivity.this);
        Exception exception=null;
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }

        @Override
        protected Exception doInBackground(Integer... integers) {

            try {
                URL url=new URL("http://rss.nytimes.com/services/xml/rss/nyt/Technology.xml");
                XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp=factory.newPullParser();
                xpp.setInput(getInputStream(url),"UTF_8");
                boolean insideItem=false;
                int eventType=xpp.getEventType();
                while(eventType!=XmlPullParser.END_DOCUMENT)
                {
                    if(eventType==XmlPullParser.START_TAG)
                    {
                        if(xpp.getName().equalsIgnoreCase("item"))
                        {
                            insideItem=true;
                        }
                        else if(xpp.getName().equalsIgnoreCase("title"))
                        {
                            if(insideItem)
                            {
                                titles.add(xpp.nextText());
                            }
                        }
                        else if(xpp.getName().equalsIgnoreCase("link"))
                        {
                            if(insideItem)
                            {
                                links.add(xpp.nextText());
                            }
                        }
                    }
                    else if(eventType==XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item"))
                    {
                        insideItem=false;
                    }
                    eventType=xpp.next();
                }
            }
            catch (MalformedURLException e) {
                exception=e;
            }
            catch (XmlPullParserException e) {
                exception=e;
            }
            catch (IOException e) {
                exception=e;
            }

            return exception;
        }

        @Override
        protected void onPostExecute(Exception e){
            ArrayAdapter<String> adapter= new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, titles);
            lnews.setAdapter(adapter);
            super.onPostExecute(e);
            progressDialog.dismiss();
        }
    }
}

