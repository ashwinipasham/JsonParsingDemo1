package com.example.aswinipasham.jsonparsingdemo1;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.FloatRange;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.aswinipasham.jsonparsingdemo1.models.UsersModel;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog dialog;
    private ListView userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage("Loading, Please wait...");

        // Create default options which will be used for every
        //  displayImage(...) call if no options will be passed to this method
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
        .defaultDisplayImageOptions(defaultOptions)
        .build();
        ImageLoader.getInstance().init(config); // Do it on Application start


        userList = (ListView) findViewById(R.id.userList);

        //new JSONTask().execute("https://api.github.com/users");
    }


    public class JSONTask extends AsyncTask<String, String, List<UsersModel>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();

        }

        @Override
        protected List<UsersModel> doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;


            try {
                //Our url goes here
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();

                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String finalJson = buffer.toString();
                JSONArray parentArray = new JSONArray(finalJson);


                List<UsersModel> usersModelList = new ArrayList<>();

                RatingBar rate;
                Gson gson = new Gson();
                for (int i = 0; i < parentArray.length(); i++) {
                    JSONObject parentObject = parentArray.getJSONObject(i);

                    UsersModel usersModel = gson.fromJson(parentObject.toString(), UsersModel.class);


                    /*UsersModel usersModel = new UsersModel();

                    usersModel.setName(parentObject.getString("login"));
                    usersModel.setUrl(parentObject.getString("url"));
                    usersModel.setImage(parentObject.getString("avatar_url"));
                    usersModel.setLink(parentObject.getString("html_url"));
                    //usersModel.setEmail(parentObject.getString("email"));
                    usersModel.setFollowers(parentObject.getString("followers_url"));*/

                    //usersModel.setRating(Float.parseFloat("2.0"));
                    usersModelList.add(usersModel);
                }

                return usersModelList;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
                try {
                    if (reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(List<UsersModel> result) {
            super.onPostExecute(result);
            dialog.dismiss();
            UserAdapter adapter = new UserAdapter(getApplicationContext(), R.layout.row, result);
            userList.setAdapter(adapter);


        }

        public class UserAdapter extends ArrayAdapter {
            List<UsersModel> usersModelList;
            private int resource;
            private LayoutInflater inflater;

            public UserAdapter(Context context, int resource, List<UsersModel> objects) {
                super(context, resource, objects);
                usersModelList = objects;
                this.resource = resource;
                inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                ViewHolder  holder = null;

                if (convertView == null) {
                    holder = new ViewHolder();
                    convertView = inflater.inflate(R.layout.row, null);
                    holder.ivUserIcon = (ImageView) convertView.findViewById(R.id.imageView);
                    holder.userURl = (TextView) convertView.findViewById(R.id.userUrl);
                    holder.Link = (TextView) convertView.findViewById(R.id.htmlLink);
                    holder.email = (TextView) convertView.findViewById(R.id.email);
                    holder.followers = (TextView) convertView.findViewById(R.id.followers);
                    holder.name = (TextView) convertView.findViewById(R.id.loginName);
                    convertView.setTag(holder);
                }
                else{
                    holder = (ViewHolder) convertView.getTag();

                }



                final ProgressBar progressBar = (ProgressBar)convertView.findViewById(R.id.progressBar);

                holder.name.setText("Name: " + usersModelList.get(position).getName());
                holder.followers.setText("Followers: " + usersModelList.get(position).getFollowers());
                holder.email.setText("Email: " + usersModelList.get(position).getEmail());
                holder.Link.setText("Link: " +usersModelList.get(position).getLink());
                holder.userURl.setText("Url:" + usersModelList.get(position).getUrl());

                // Then later, when you want to display image
                // Default options
                ImageLoader.getInstance().displayImage(usersModelList.get(position).getImage(), holder.ivUserIcon, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        progressBar.setVisibility(view.VISIBLE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        progressBar.setVisibility(view.GONE);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        progressBar.setVisibility(view.GONE);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        progressBar.setVisibility(view.GONE);
                    }
                });



                return convertView;
            }

            class ViewHolder{
                private ImageView ivUserIcon;
                private TextView userURl;
                private TextView Link;
                private TextView email;
                private TextView name;
                private TextView followers;

            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            new JSONTask().execute("https://api.github.com/users");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}







