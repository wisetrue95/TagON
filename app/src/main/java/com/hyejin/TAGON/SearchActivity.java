package com.hyejin.TAGON;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

public class SearchActivity extends Activity {
    public static Context mContext;

    //Naver api
    String clientId = "JOrbQRT1OnMPXqD8VeZZ";
    String clientSecret = "fYxraIG9SY";

    String poombun;

    //xml ???
    TextView searchResult;
    TextView poombun_main;
    TextView product_price_main;
    ImageView product_thumbnail_main;
    Button favoritebtn; // ???????????? ?????? ??????

    //????????????
    ListView listView;
    SearchActivity.ListViewAdapter listViewAdapter;
    //main ????????? ????????? flag
    boolean ascflag=true;

    //???????????? -????????? ??????
    Spinner spinner;
    // ???????????? ?????????
    ArrayList<Results_ListItem> findingItems = new ArrayList<>();   //all
    ArrayList<Results_ListItem> gmarket = new ArrayList<>();        //gmarket
    ArrayList<Results_ListItem> st11 = new ArrayList<>();           //11st

    //????????? ?????????
    SQLiteDatabase favoitesDB ;

    //?????? ??????
    ArrayList<ArrayList<String>> final_poombuns =new ArrayList<ArrayList<String>>();

    //????????? ?????? - ?????? ?????? ?????? ????????? ?????? ?????????
    HashMap<String, Integer> countlist2 = new HashMap<>();  // ??????2


    // main
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_list);

        mContext=this;


        // ????????? ??????
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());

        //xml
        searchResult=findViewById(R.id.searchResult);
        poombun_main=findViewById(R.id.poombun);
        product_price_main=findViewById(R.id.product_price_main);
        product_thumbnail_main=findViewById(R.id.product_thumbnail_main);
        listView=findViewById(R.id.listView);
        spinner=findViewById(R.id.spinner);
        favoritebtn = findViewById(R.id.favoritebtn); // ???????????? ?????? ??????

        //DB
        favoitesDB = init_database() ;
        init_tables() ;


        // ?????? ?????? ????????????
        Intent intent = getIntent();
        poombun = intent.getExtras().getString("poombun");

        //????????? ??????
        poombun=poombun.toUpperCase();
        poombun=poombun.replace(" ","");
        //Toast.makeText(SearchActivity.this, "??????????????? ????????????.", Toast.LENGTH_SHORT).show();


        // ????????? ?????? ?????? ?????? ?????? ????????? ?????? ?????? ?????? ??????
        poombun=rePermutation(poombun);
        // ?????? ??????
        NaverShopping(poombun);




        // ????????? ??? ??????
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //??????
                if(position==0){
                    //NaverShopping("all");
                    if(findingItems.size()!=0){
                        listViewAdapter = new ListViewAdapter(getApplicationContext(), findingItems);
                        listView.setAdapter(listViewAdapter);

                    }else{
                        spinner.setVisibility(View.INVISIBLE);
                        Intent intent = new Intent(getApplicationContext(), NoSearch.class);
                        startActivityForResult(intent, 1);
                        finish();
                        //Toast.makeText(SearchActivity.this, "??????????????? ????????????.", Toast.LENGTH_SHORT).show();
                    }
                }

                //G??????
                if(position==1){
                    //NaverShopping("gmarket");
                    if(gmarket.size()!=0){
                        for(int i=0;i<gmarket.size();i++) {
                            listViewAdapter = new ListViewAdapter(getApplicationContext(), gmarket);
                            listView.setAdapter(listViewAdapter);
                        }
                    }else{
                        //Toast.makeText(SearchActivity.this, "??????????????? ????????????.", Toast.LENGTH_SHORT).show();
                    }
                }

                //11??????
                if(position==2){
                    //NaverShopping("11st");
                    if (st11.size() != 0) {
                        for (int i = 0; i < st11.size(); i++) {
                            listViewAdapter = new ListViewAdapter(getApplicationContext(), st11);
                            listView.setAdapter(listViewAdapter);
                        }
                    } else {
                        //Toast.makeText(SearchActivity.this, "??????????????? ????????????.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // ???????????? ?????? ?????????
        favoritebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //SearchActivity.this,
                Results_ListItem poombun_main = findingItems.get(0);
                String price = poombun_main.getPriceText();
                Bitmap thumbnail_main_value = poombun_main.getThumb();
                String img = BitMapToString(thumbnail_main_value);


                //Toast.makeText(getApplicationContext(), "??????????????? ??????????????????.", Toast.LENGTH_LONG).show();


                //?????? string ???????????? ??????
                //????????? ????????? ??????
                save_values(poombun, price, img);
                searchResult.setText(poombun);
            }
        });

    }


    //-- ??????

    // ????????? ??????
    public void NaverShopping(String poombun){
        try{


            String text = URLEncoder.encode(poombun, "UTF-8"); //poombun

            // ????????? api ?????? ??????: display: ????????????, sort=asc:????????????
            String apiURL = "https://openapi.naver.com/v1/search/shop.xml?query="+ text
                    +"&display=50" +"&sort=asc"; // xml ??????

            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            int responseCode = con.getResponseCode();
            BufferedReader br;


            if(responseCode==200) { // ?????? ??????
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // ?????? ??????
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            System.out.println(response.toString());


            // ????????? api ?????? ?????? ????????? ????????? ?????????
            String shopResult=response.toString();
            List<Shop> parsingResult = parsingShopResultXml(shopResult);

            String total_value=Total(shopResult);
            //Toast.makeText(getApplicationContext(),total_value,Toast.LENGTH_LONG).show();




            // ?????? ?????? for ??????
            for(Shop shop : parsingResult) {
                Bitmap thumbImg = getBitmapFromURL(shop.getImage());
                // ?????? ??????
                findingItems.add(new Results_ListItem(shop.getTitle(),thumbImg,
                        shop.getLprice() + "???", shop.getLprice(),
                        shop.getLink(),
                        shop.getMallName()));
            }
            if(findingItems.size()!=0) {
                //?????? ?????? ??? ????????? ??????
                // SearchActivity ?????? ?????? (fav==0)
                poombun_main.setText("NO. "+poombun);

                // ???????????? ??? ?????? ??????
                for (int i = 0; i < findingItems.size(); i++) {


                    // main ?????? ????????????
                    if (i == 0 && ascflag == true) {

                        Results_ListItem poombun_main_list = findingItems.get(0);
                        String price_main_value = poombun_main_list.getPrice()+"???";
                        Bitmap thumbnail_main_value = poombun_main_list.getThumb();
                        product_price_main.setText("?????????: " + price_main_value);
                        product_thumbnail_main.setImageBitmap(thumbnail_main_value);



                    }
                    ascflag = false;


                    // mall ?????? ??? ???????????? ????????????
                    // 1) G??????
                    Results_ListItem gmarket_list = findingItems.get(i);

                    if (gmarket_list.getMallName().equals("G??????")) {
                        gmarket.add(findingItems.get(i));
                    }

                    // 2) 11??????
                    Results_ListItem st11_list = findingItems.get(i);
                    if (st11_list.getMallName().equals("11??????")) {
                        st11.add(findingItems.get(i));
                    }
                }
            }

            //????????????
        } catch (Exception e) {
            System.out.println(e);
            //searchResult.setText("?????????:  "+e.toString());   //????????????

        }



    }

    // xml ????????? ????????? ???????????? ??????
    public List<Shop> parsingShopResultXml(String data) throws Exception {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();

        List<Shop> list = null ;
        parser.setInput(new StringReader(data));
        int eventType = parser.getEventType();
        Shop b = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    list = new ArrayList<Shop>();
                    break;
                case XmlPullParser.END_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG: {
                    // item????????? ????????? ??????
                    String tag = parser.getName();
                    switch (tag) {
                        case "item":
                            b = new Shop();
                            break;
                        case "title":
                            if (b != null) {
                                b.setTitle(RemoveHTMLTag(parser.nextText()));
                            }
                            break;
                        case "link":
                            if (b != null)
                                b.setLink(parser.nextText());
                            break;
                        case "image":
                            if (b != null)
                                b.setImage(parser.nextText()+"?type=f140");
                            break;
                        case "total":
                            if (b != null)
                                b.setTotal(parser.next());
                            break;
                        case "lprice":
                            if (b != null)
                                b.setLprice(Integer.parseInt(parser.nextText()));
                        case "hprice":
                            if (b != null)
                                b.setHprice(parser.next());
                            break;
                        case "mallName":
                            if (b != null)
                                b.setMallName(RemoveHTMLTag(parser.nextText()));
                            break;
                    }
                    break;
                }
                case XmlPullParser.END_TAG: {
                    String tag = parser.getName();
                    if (tag.equals("item")) {
                        list.add(b);
                        b = null;
                    }
                }
            }
            eventType = parser.next();
        }
        return list;
    }


    // ?????? ?????? ?????? ????????? ??????
    public String Total(String data) throws Exception {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();

        parser.setInput(new StringReader(data));
        int eventType = parser.getEventType();
        String total="";


        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.END_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG: {
                    // item????????? ????????? ??????
                    String tag = parser.getName();
                    if (tag.equals("total")) {
                        eventType = parser.next();
                        total=parser.getText();
                        System.out.println("total="+total);
                        break;

                    }
                }
            }eventType = parser.next();
        }
        return total;
    }

    // ?????? ?????? ??????
    public String RemoveHTMLTag(String changeStr) {
        if (changeStr != null && !changeStr.equals("")) {
            changeStr = changeStr.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "");
        } else {
            changeStr = "";
        }
        return changeStr;
    }

    // ?????????
    public Bitmap getBitmapFromURL(String src) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(src);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap myBitmap = BitmapFactory.decodeStream(input, null, op);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    //--------------------------------------------------------------------------------------------
    // --????????? ??????
    /* ????????? ?????? ?????? ??????
        1. [rePermutation()]    input : poombun / output : poombun
                                ????????? ????????? poombun???  ????????? ??? ?????? (??????"O" ??? ??????"0"??? ?????????)
                                ????????? ????????? ??? ?????? search_count(poombun) ??????

        2. [search_count()]     input : poombun / output : int array
                                ??????1. ????????? ???????????????, ?????? ????????? ?????? ????????? int ???????????? ??????
                                [Descending()] ????????? ???????????? ??????

                                -->> (??????)
                                ??????2. ????????? ???????????????, ?????? ????????? <?????? ??????(int),poombun(String)> ?????? hashmap ??????
                                hashmap > treemap ?????? key ??? ???????????? ??????

        3. [rePermutation()]    input : poombun / output : poombun
                                ?????? ??? ??? ???????????? (?????????[0] ??? ???????????? ??????)
                                -->> ?????? ?????? poombun??? ???????????? ?????????, how? --> hashmap

                                hashmap ??? key : ???????????? / value : poombun?????? ??????, key ??????,
                                https://jobc.tistory.com/176

     */

    public String rePermutation(String abc) {


        // ?????? abc

        //????????? ?????? (0, O)
        String []n = {"0","O"};

        // ????????? ?????? ?????? * ??????
        abc = abc.replace("0","*");
        abc = abc.replace("O","*");
        String replaced_poombun = abc; //????????? ????????????
        System.out.println("Test : ??????1"+abc);

        //?????????(*??????)
        int r = StringUtils.countMatches(replaced_poombun, "*");
        System.out.println("r??????: "+r);
        //int r = 3;

        //????????????
        LinkedList<String> rCom = new LinkedList<String>();

        rePermutation1(n, r, rCom);

        System.out.println();
        System.out.println("Test : ????????? ??????"+final_poombuns);
        System.out.println();

        // ????????? ???????????? ?????? ??????
        //ArrayList<String> changed =new ArrayList<String>();

        //*?????? ?????? ????????? ?????????
        for(ArrayList<String> s : final_poombuns){
            for(String k : s){
                abc = abc.replaceFirst("\\*",k);
                //System.out.println(poombun);
            }
            System.out.println("Test : ??????2"+abc);
            //changed.add(poombun);   // ??? ????????? ????????? ?????? ?????? ?????? ????????? ?????? ???????????? ??????
            search_count(abc);

            // ?????? ????????? ?????? ????????? ?????????
            abc = replaced_poombun;

        }

        System.out.println("???1"+countlist2);

        List<String> keySetList = new ArrayList<>(countlist2.keySet());
        // ???????????? //
        Collections.sort(keySetList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return countlist2.get(o2).compareTo(countlist2.get(o1));
            }
        });

        //?????? ?????? ?????? ?????? ????????? ?????? ??????
        String bestpoombun="";

        //--?????? ?????? ?????? ????????? ?????????
        for(String key : keySetList) {
            System.out.println(String.format("Key : %s, Value : %s", key, countlist2.get(key)));
        }

        // ???????????? ??????
        for(String key : keySetList) {
            System.out.println(String.format("Key : %s, Value : %s", key, countlist2.get(key)));
            bestpoombun=key; // ???????????? ?????????(????????????)
            break;

        }

        System.out.println("?????? ?????? ?????? ??????"+bestpoombun);

        return bestpoombun;


    }
    // ?????? ?????? ????????? ??? ?????????
    private void rePermutation1(String[] n, int r, LinkedList<String> rCom) {


        if(rCom.size() == r){

            ArrayList<String> list2=new ArrayList(); //????????? ???????????? ?????????
            for(String i : rCom){
                System.out.print("???????????? "+i+" ");
                // ?????? ???????????? ??????
                list2.add(i);
            }

            System.out.println();
            System.out.println(list2);

            final_poombuns.add(list2); //?????? ????????? ??????

            return;
        }

        for(String c : n){

            rCom.add(c);
            rePermutation1(n, r, rCom);
            rCom.removeLast();
        }

    }

    // ?????? ?????? int??? ?????? + ????????? ??????
    //????????? ?????? - ?????? ????????? ????????? search_count
    private void search_count(String abc){

        try{
            String text = URLEncoder.encode(abc, "UTF-8"); //poombun

            // ????????? api ?????? ??????: display: ????????????, sort=asc:????????????
            String apiURL = "https://openapi.naver.com/v1/search/shop.xml?query="+ text
                    +"&display=100" +"&sort=asc"; // xml ??????

            // ????????? ?????? ??????

            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            int responseCode = con.getResponseCode();
            BufferedReader br;


            if(responseCode==200) { // ?????? ??????
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // ?????? ??????
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            System.out.println(response.toString());

            // ????????? api ?????? ?????? ????????? ????????? ?????????
            String shopResult=response.toString();

            // ?????? ?????? ????????? ??????(Total()) ???????????? poombun ?????? ?????????. => ????????? int.
            int total_count = Integer.parseInt(Total(shopResult));
            System.out.println("??????????????? ?????????: "+total_count);


            // -- ??????2. ?????? ?????? ????????? Treemap??????

            // hashmap list??? (key : ??????????????? total_count, value : ?????? ?????? poombun) ????????????
            //countlist2.put(total_count, abc);
            countlist2.put(abc, total_count);




            //????????????
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    //--------------------------------------------------------------------------------------------
    //--DB
    // DB ??????/??????
    private SQLiteDatabase init_database() {

        SQLiteDatabase db = null ;
        // File file = getDatabasePath("contact.db") ;
        File file = new File(getFilesDir(), "contact.db") ;

        System.out.println("PATH : " + file.toString()) ;
        try {
            db = SQLiteDatabase.openOrCreateDatabase(file, null) ;
        } catch (SQLiteException e) {
            e.printStackTrace() ;
        }

        if (db == null) {
            System.out.println("DB creation failed. " + file.getAbsolutePath()) ;
        }

        return db ;
    }
    // DB ????????? ?????????
    private void init_tables() {

        // ????????? ??? [POOMBUN | PRICE | IMG]
        if (favoitesDB != null) {
            String sqlCreateTbl = "CREATE TABLE IF NOT EXISTS CONTACT_T (" +
                    "POOMBUN "         + "TEXT," +
                    "PRICE "        + "TEXT," +
                    "IMG "       + "TEXT" + ")" ;

            System.out.println(sqlCreateTbl) ;
            favoitesDB.execSQL(sqlCreateTbl) ;
        }
    }
    // DB ????????? ??????
    private void save_values(String poombunDB, String price, String img) {
        //????????? ??????
        poombunDB=poombunDB.toUpperCase();

        //??? ??? ???????????? ??????
        String sqlInsert = "INSERT INTO CONTACT_T " +
                    "(POOMBUN, PRICE, IMG) VALUES (" +
                    "'" + poombunDB + "'," +
                    "'" + price + "'," +
                    "'" + img + "'"+ ")" ;


        //Toast.makeText(getApplicationContext(), poombunDB+" "+price, Toast.LENGTH_LONG).show();
        Toast.makeText(getApplicationContext(), "????????? ??????!", Toast.LENGTH_LONG).show();


        //?????? ????????? ??????
        favoitesDB.execSQL(" DELETE FROM CONTACT_T WHERE POOMBUN = '" + poombunDB + "'; ");


        System.out.println(sqlInsert) ;
        favoitesDB.execSQL(sqlInsert) ;


    }

    //???????????? ???????????????
    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte [] b = baos.toByteArray();
        String temp = Base64.encodeToString(b,Base64.DEFAULT);
        return temp;
    }

    //--------------------------------------------------------------------------------------------

   //--????????????
    private class ListViewAdapter extends BaseAdapter {
        Context context;
        ArrayList<SearchActivity.Results_ListItem> listItems;

        private ListViewAdapter(Context context, ArrayList<SearchActivity.Results_ListItem> listItems) {
            this.context = context;
            this.listItems = listItems;
        }

        public int getCount() {
            return listItems.size();
        }

        public SearchActivity.Results_ListItem getItem(int position) {
            return listItems.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // ??? ?????? ??????: ???????????? ?????? ??????
            final UserViewHolder  userViewHolder;

            if(convertView == null) {

                LayoutInflater inflater = LayoutInflater.from(context);
                convertView=inflater.inflate(R.layout.list_item, parent, false);

                //list_item??? ?????? ??????
                userViewHolder = new UserViewHolder();
                userViewHolder.thumbView = convertView.findViewById(R.id.product_thumbnail);
                userViewHolder.productNameView = convertView.findViewById(R.id.product_name);
                userViewHolder.priceView = convertView.findViewById(R.id.product_price);
                userViewHolder.mallNameView = convertView.findViewById(R.id.product_mallname);

                //?????? ??????
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(userViewHolder.shoppingmall_url));
                        startActivity(myIntent);
                    }
                });
                convertView.setTag(userViewHolder);


            } else {
                userViewHolder = (UserViewHolder) convertView.getTag();


            }
            userViewHolder.bind(this.context, this.listItems.get(position));


            return convertView;
        }
    }

    // ???????????? ????????? ????????? ?????? ????????? ????????? ?????? ??????
    class Results_ListItem{
        private Bitmap thumb;
        private String productName;
        private String price;
        private int int_price;
        private String url;
        private String mallName;

        public Results_ListItem(String productName, Bitmap thumb, String price, int int_price, String url,
                               String mallName) {
            this.thumb = thumb;
            this.productName = productName;
            this.price = price;
            this.int_price = int_price;
            this.url = url;
            this.mallName = mallName;

        }

        public int getPrice() { return this.int_price; }
        public String getPriceText() { return this.price; }
        public Bitmap getThumb() { return this.thumb; }
        public String getProductName() { return this.productName; }
        public String getUrl() { return this.url; }
        public String getMallName() {
            return mallName;
        }

    }

    // ???????????? ???, xml??? ????????? ???????????? ?????????
    // ?????????: ???????????? ?????? ??????
    public static class UserViewHolder {
        ImageView thumbView;
        TextView productNameView;
        TextView priceView;
        TextView mallNameView;
        String shoppingmall_url;

        public void bind(Context context, final SearchActivity.Results_ListItem aItem) {

            thumbView.setImageBitmap(aItem.getThumb());
            productNameView.setText(aItem.getProductName());
            priceView.setText(aItem.getPriceText());
            mallNameView.setText(aItem.getMallName());
            shoppingmall_url=aItem.getUrl();


        }
    }

}

