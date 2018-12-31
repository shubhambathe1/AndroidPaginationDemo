package shubham.com.pagenationrecyclerdemo;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shubham.com.pagenationrecyclerdemo.networking.MySingleton;

public class MainActivity extends AppCompatActivity {

    private List<Movie> movieList = new ArrayList<>();
    private RecyclerView recyclerView;
    private MoviesAdapter mAdapter;

    int i = 1;

    //FOR NETWORK REQUESTS
    RequestQueue queue;
    public static final String TAG = "MyNetTag";

    private ProgressBar progressBar;

    //FOR IMPLEMENTING PAGENATION
    Boolean isScrolling = false;
    int currentItems, totalItems, scrollOutItems;

    int COUNTER = 1;
    int SKIP = 0, LIMIT = 30;

    String todayString = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        todayString = formatter.format(todayDate);

        //Log.e("DATE", todayString);

        // Request a string response from the provided URL
        //String url = "https://simplifiedcoding.net/demos/view-flipper/heroes.php";

        String url2 = "https://stage-api.yapsody.com/online/global/account/order_history?skip=" + SKIP + "&limit=" + LIMIT + "&start_date=" + todayString + "&sort=FL&trans_type=sale";


        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.setVisibility(View.VISIBLE);

        mAdapter = new MoviesAdapter(movieList);
        final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Movie movie = movieList.get(position);
                Toast.makeText(getApplicationContext(), movie.getTitle() + " is selected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {
                Movie movie = movieList.get(position);
                Toast.makeText(getApplicationContext(), movie.getGenre() + " is selected!", Toast.LENGTH_SHORT).show();
            }
        }));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {

                    isScrolling = true;

                }

            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                currentItems = mLayoutManager.getChildCount();
                totalItems = mLayoutManager.getItemCount();
                scrollOutItems = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();

                if(isScrolling && (currentItems + scrollOutItems == totalItems)) {

                    //FETCH DATA
                    isScrolling = false;

                    SKIP = SKIP + 30;

                    fetchData(SKIP);

                }

            }
        });

        //prepareMovieData();




        //GET REQUEST EXAMPLE
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        //textViewResponse.setText("Response is: "+ response.substring(0,500));

                        progressBar.setVisibility(View.GONE);

                        try {

                            JSONObject jsonObject = new JSONObject(response);

                            JSONObject jsonObjectData = jsonObject.getJSONObject("data");

                            Log.e("MY_LENGTH_TAG", String.valueOf(jsonObjectData.getJSONArray("trans").length()));

                            int length = jsonObjectData.getJSONArray("trans").length();

                            JSONArray jsonArrayTrans = jsonObjectData.getJSONArray("trans");

                            for(int j = 0; j < length ; j++) {

                                JSONObject jsonObjectTrans = jsonArrayTrans.getJSONObject(j);

                                Movie movie = new Movie(jsonObjectTrans.getString("ft_order_id") + " : " + COUNTER++, "Action & Adventure", "2015");

                                movieList.add(movie);

                            }


                           mAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }



                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("MY_TAG", error.toString());
                Log.e("MY_TAG", "That didn't work!");
                //textViewResponse.setText("That didn't work!");
            }
        }) {
            @Override public Map<String, String> getHeaders() {

                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("venue-code", "vinaychavan");
                params.put("authtoken", "eyJhbGciOiJIUzI1NiJ9.NzBlMWZkODAtZWQ1Mi0xMWU4LTg4MDUtZjNkNGRlZjljOWQ3.dipu-miz4D8PdT0cz7m1O7Ky60X1XAXPnhvKmqt5qik");

                return params;
            }
        };



        stringRequest.setTag(TAG);

        // Add a request (in this example, called stringRequest) to your RequestQueue.
        //MySingleton.getInstance(this).addToRequestQueue(stringRequest);

        // Get a RequestQueue
        queue = MySingleton.getInstance(this.getApplicationContext()).getRequestQueue();
        queue.add(stringRequest);

    }

    @Override
    protected void onStop () {

        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }


    private void fetchData(int SKIP) {

        String url2 = "https://stage-api.yapsody.com/online/global/account/order_history?skip=" + SKIP + "&limit=" + LIMIT + "&start_date=" + todayString + "&sort=FL&trans_type=sale";

        progressBar.setVisibility(View.VISIBLE);



        //GET REQUEST EXAMPLE
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        //textViewResponse.setText("Response is: "+ response.substring(0,500));

                        progressBar.setVisibility(View.GONE);

                        try {

                            JSONObject jsonObject = new JSONObject(response);

                            JSONObject jsonObjectData = jsonObject.getJSONObject("data");

                            Log.e("MY_TAG", String.valueOf(jsonObjectData.getJSONArray("trans").length()));

                            int length = jsonObjectData.getJSONArray("trans").length();

                            JSONArray jsonArrayTrans = jsonObjectData.getJSONArray("trans");

                            for(int j = 0; j < length ; j++) {

                                JSONObject jsonObjectTrans = jsonArrayTrans.getJSONObject(j);

                                Movie movie = new Movie(jsonObjectTrans.getString("ft_order_id") + " : " + COUNTER++, "Action & Adventure", "2015");

                                movieList.add(movie);

                            }

                            mAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }



                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("MY_TAG", error.toString());
                Log.e("MY_TAG", "That didn't work!");
                //textViewResponse.setText("That didn't work!");
            }
        }) {
            @Override public Map<String, String> getHeaders() {

                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("venue-code", "vinaychavan");
                params.put("authtoken", "eyJhbGciOiJIUzI1NiJ9.NzBlMWZkODAtZWQ1Mi0xMWU4LTg4MDUtZjNkNGRlZjljOWQ3.dipu-miz4D8PdT0cz7m1O7Ky60X1XAXPnhvKmqt5qik");

                return params;
            }
        };



        stringRequest.setTag(TAG);

        // Add a request (in this example, called stringRequest) to your RequestQueue.
        //MySingleton.getInstance(this).addToRequestQueue(stringRequest);

        // Get a RequestQueue
        queue = MySingleton.getInstance(this.getApplicationContext()).getRequestQueue();
        queue.add(stringRequest);



//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//                for(int i=1; i <= 35; i++) {
//
//                    Movie movie = new Movie("Mad Max: Fury Road " + i, "Action & Adventure", "2015");
//                    movieList.add(movie);
//
//                    progressBar.setVisibility(View.GONE);
//                }
//                mAdapter.notifyDataSetChanged();
//
//            }
//        }, 2000);

    }

    private void prepareMovieData() {
        Movie movie = new Movie("Mad Max: Fury Road", "Action & Adventure", "2015");
        movieList.add(movie);

        movie = new Movie("Inside Out", "Animation, Kids & Family", "2015");
        movieList.add(movie);

        movie = new Movie("Star Wars: Episode VII - The Force Awakens", "Action", "2015");
        movieList.add(movie);

        movie = new Movie("Shaun the Sheep", "Animation", "2015");
        movieList.add(movie);

        movie = new Movie("The Martian", "Science Fiction & Fantasy", "2015");
        movieList.add(movie);

        movie = new Movie("Mission: Impossible Rogue Nation", "Action", "2015");
        movieList.add(movie);

        movie = new Movie("Up", "Animation", "2009");
        movieList.add(movie);

        movie = new Movie("Star Trek", "Science Fiction", "2009");
        movieList.add(movie);

        movie = new Movie("The LEGO Movie", "Animation", "2014");
        movieList.add(movie);

        movie = new Movie("Iron Man", "Action & Adventure", "2008");
        movieList.add(movie);

        movie = new Movie("Aliens", "Science Fiction", "1986");
        movieList.add(movie);

        movie = new Movie("Chicken Run", "Animation", "2000");
        movieList.add(movie);

        movie = new Movie("Back to the Future", "Science Fiction", "1985");
        movieList.add(movie);

        movie = new Movie("Raiders of the Lost Ark", "Action & Adventure", "1981");
        movieList.add(movie);

        movie = new Movie("Goldfinger", "Action & Adventure", "1965");
        movieList.add(movie);

        movie = new Movie("Guardians of the Galaxy", "Science Fiction & Fantasy", "2014");
        movieList.add(movie);

        mAdapter.notifyDataSetChanged();
    }
}
