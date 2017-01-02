package com.painlessshopping.mohamed.findit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Samuel on 2016-12-23.
 */

public class MastermindToysSearch extends SearchQuery{

    public Elements finalDoc;
    private ArrayList<Item> processed;
    private final Handler uiHandler = new Handler();
    public int status = 0;

    //This basically is just so that the class knows which Activity we're working with
    private Context c;

    protected class JSHtmlInterface {
        @android.webkit.JavascriptInterface
        public void showHTML(String html) {
            final String htmlContent = html;

            uiHandler.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            Document doc = Jsoup.parse(htmlContent);
                        }
                    }
            );
        }
    }

    /**
     * Constructor method
     * @param context The context taken from the webview (So that the asynctask can show progress)
     */
    public MastermindToysSearch(Context context, String query) {


        final Context c = context;

        try {
            final WebView browser = new WebView(c);
            browser.setVisibility(View.INVISIBLE);
            browser.setLayerType(View.LAYER_TYPE_NONE, null);
            browser.getSettings().setJavaScriptEnabled(true);
            browser.getSettings().setBlockNetworkImage(true);
            browser.getSettings().setDomStorageEnabled(true);
            browser.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            browser.getSettings().setLoadsImagesAutomatically(false);
            browser.getSettings().setGeolocationEnabled(false);
            browser.getSettings().setSupportZoom(false);
            browser.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
            browser.addJavascriptInterface(new JSHtmlInterface(), "JSBridge");

            browser.setWebViewClient(
                    new WebViewClient() {

                        @Override
                        public void onPageStarted(WebView view, String url, Bitmap favicon) {
                            super.onPageStarted(view, url, favicon);
                        }

                        @Override
                        public void onPageFinished(WebView view, String url) {
                            browser.loadUrl("javascript:window.JSBridge.showHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                        }
                    }
            );


                browser.loadUrl("http://www.mastermindtoys.com/catalog/searchresults.aspx?search=" + query);
                browser.loadUrl(browser.getUrl());
                final String link = browser.getUrl();
                new fetcher(c).execute(link);
                new fetcher(c).execute(link + "&p=2");
                new fetcher(c).execute(link + "&p=3");



        }
        catch(Exception e){
            e.printStackTrace();
        }

        //Get the link from the WebView, and save it in a final string so it can be accessed from worker thread


    }

    /**
     * This subclass is a worker thread meaning it does work in the background while the user interface is doing something else
     * This is done to prevent "lag".
     * To call this class you must write fetcher(Context c).execute(The link you want to connect to)
     *
     */
    class fetcher extends AsyncTask<String, Void, Elements> {

        Context mContext;
        ProgressDialog pdialog;

        public fetcher(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdialog = new ProgressDialog(mContext);
            pdialog.setTitle("Finding Results!");
            pdialog.setCancelable(false);
            pdialog.show();
        }

        //This return elements because the postExecute() method needs an Elements object to parse its results
        @Override
        protected Elements doInBackground(String... strings) {

            //You can pass in multiple strings, so this line just says to use the first string
            String link = strings[0];

            //For Debug Purposes, Do NOT Remove - **Important**
            System.out.println("Connecting to: " + link);

            try {
                doc = Jsoup.connect(link)
                        .ignoreContentType(true)
                        .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36")
                        .timeout(10000)
                        .get();


                finalDoc = doc.select("body div.mm_SearchProduct");




            } catch (IOException e) {
                e.printStackTrace();
            }

            return finalDoc;
        }


        @Override
        protected void onPostExecute(Elements result) {


            //This line clears the list of info in the Search activity
            //I should probably be using a getter method but adapter is a static variable so it shouldn't matter


            //parse seperates document into elements
            //crunch results formats those elements into item objects
            //I am saving the result of this to an ArrayList<Item> called "processed"
            processed = crunchResults(result);

            //For debug purposes, do NOT remove - **Important**
            System.out.println(processed.size() + " results have been crunched by Mastermind Toys.");

            //Adds all of the processed results to the list of info in Search activity
            if(mContext == BookSearch.getAppContext()){
                BookSearch.adapter.addAll(processed);
            } else if (mContext == ToySearch.getAppContext()){
                ToySearch.adapter.addAll(processed);
            }


            //For debug purposes, do NOt remove - **Important
            System.out.println("Adapter has been notified by Mastermind Toys.");

            //Closes the progress dialog called pdialog assigned to the AsyncTask

            pdialog.dismiss();

            if(mContext == BookSearch.getAppContext()){
                BookSearch.adapter.notifyDataSetChanged();
                SearchQueueHandler.makeRequest(mContext, processed, SearchQueueHandler.BOOK_SEARCH);

            } else if (mContext == ToySearch.getAppContext()){
                ToySearch.adapter.notifyDataSetChanged();
                SearchQueueHandler.makeRequest(mContext, processed, SearchQueueHandler.TOYS_SEARCH);
            }



        }
    }



        public ArrayList<Item> crunchResults(Elements e){

        ArrayList<Item> results = new ArrayList<Item>();

        try {

            for (int i = 0; i < e.size(); i++) {

                Element ele = e.get(i);

                String link = "http://www.mastermindtoys.com" + ele.select(" div.mm_SearchProdDescription > a").attr("href");
                System.out.println("http://www.mastermindtoys.com" + ele.select(" div.mm_SearchProdDescription > a").attr("href"));
                String title = ele.select(" div.mm_SearchProdDescription > a").text();

                price = Double.parseDouble(ele.select(" span.mm_PriceListValue").text().substring(1));
                System.out.println(ele.select(" span.mm_PriceListValue").text());

                //*******************************************

                String store = "Mastermind Toys";

                //Adds the formatted item to an ArrayList of items
                results.add(new Item(title, store, price, link));


                //Prints the object's to String to console
                //For debug purposes, do NOT remove - **Important
                System.out.println(results.get(i).toString());

            }

        } catch (Exception a){
            a.printStackTrace();
        }

        return results;
    }

    public int getStatus(){
        return status;
    }

}
