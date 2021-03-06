package com.hyejin.TAGON;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.preference.Preference;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;

public class Favorites extends Activity {

    //Naver api
    String clientId = "JOrbQRT1OnMPXqD8VeZZ";
    String clientSecret = "fYxraIG9SY";

    String poombun;

    //xml ???

    ImageView grid_imageview;
    TextView grid_textview_name;
    TextView grid_textview_price;
    ToggleButton toggle;
    Button grid_deletebutton;

    //????????????
    GridView gridView_favorites;
    //SingerAdapter mMyAdapter;

    // DB
    SQLiteDatabase favoitesDB ;

    // ?????? ??????
    int new_price;

    Timer timer;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.singer_main);

        // ?????? ??????????????? ???????????? ?????? ????????????: android.os.NetworkOnMainThreadException
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        gridView_favorites=findViewById(R.id.gridView);

        // DB
        favoitesDB = init_database() ;
        init_tables();  // ????????? ??????
        load_values();  // ????????? ??????


    }

    // ??????????????? ??????
    private void createNotification() {

        PendingIntent mPendingIntent = PendingIntent.getActivity(Favorites.this, 0,
                new Intent(getApplicationContext(), Favorites.class),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("TAGON");
        builder.setContentText(poombun+" ??? ????????? ?????? ??????!");

        builder.setWhen(System.currentTimeMillis());

        // ???????????? ?????? ???????????? ?????? ??????
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_VIBRATE); //???????????? ??????

        // ?????? ??????
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("default", "?????? ??????", NotificationManager.IMPORTANCE_DEFAULT));
        }

        // id??????
        // ?????????????????? ??? ????????? ????????? int???
        notificationManager.notify(1, builder.build());
    }


    //--???????????? -> ???????????? ?????????
    class SingerAdapter extends BaseAdapter {
        Context context;
        ArrayList<SingerItem> items = new ArrayList<SingerItem>();
        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(SingerItem singerItem){
            items.add(singerItem);
        }

        @Override
        public SingerItem getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup parent) {

            final int pos = i ; // ????????? ??????
            final Context context = parent.getContext();

            // 'listview_custom' Layout??? inflate?????? convertView ?????? ??????
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.singeritem, parent, false);
            }

            //singeritem??? ?????? ??????
            grid_textview_name = convertView.findViewById(R.id.singertextview1);
            grid_textview_price = convertView.findViewById(R.id.singertextview2);
            grid_imageview = convertView.findViewById(R.id.singerimageview1);
            grid_deletebutton=convertView.findViewById(R.id.singerbutton2);
            toggle=convertView.findViewById(R.id.toggleButton);


            /* ??? ???????????? ????????? ???????????? ??????????????? mMyItem ????????? */
            SingerItem myItem = getItem(i);

            /* ??? ????????? ????????? ???????????? ???????????? */
            grid_textview_name.setText(myItem.getName());
            grid_textview_price.setText(myItem.getprice());
            grid_imageview.setImageBitmap(myItem.getImage());


            // ???????????? ????????? ????????????
            gridView_favorites.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                // ????????? ????????? ??????
                public void onItemClick(AdapterView parent, View v, int position, long id) {
                    // ????????? ??????(pos+1) ??????
                    //Toast.makeText(getApplicationContext(), Integer.toString(position+1) + " Item is selected..", Toast.LENGTH_SHORT).show() ;

                    // ?????? ?????? ????????????
                    String fav_poombun= items.get(position).getName();
                    //Toast.makeText(getApplicationContext(), fav_poombun, Toast.LENGTH_SHORT).show() ;

                    // SearchActivity ????????? ?????? ??????
                    Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                    intent.putExtra("poombun", fav_poombun);
                    startActivity(intent);
                    finish();
                }

            }) ;


            // ?????? button ?????? ??? ?????????????????? ??????
            grid_deletebutton.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {

                    // ?????? ?????? ????????????
                    String delete_poombun= items.get(pos).getName();  // ?????? ????????? ??????(pos)??? ?????? ????????? ????????????
                    //Toast.makeText(getApplicationContext(), delete_poombun, Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), "?????????????????????.", Toast.LENGTH_LONG).show();

                    // ?????? ?????? ????????? ??????
                    favoitesDB.execSQL(" DELETE FROM CONTACT_T WHERE POOMBUN = '" + delete_poombun + "'; ");

                    // ???????????? ?????????
                    load_values();
                }
            });


            // ??????????????? ?????? ?????? (Toggle)
            toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // toggle ON
                    if (isChecked) {
                        // ?????? ??????
                        TimerTask tt=new TimerTask() {
                            @Override
                            public void run() {
                                // ?????????
                                Handler mHandler = new Handler(Looper.getMainLooper());
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        // ?????? ?????? ????????????
                                        String previous_poombun= items.get(pos).getName();  // ?????? ????????? ??????(pos)??? ?????? ????????? ????????????
                                        //Toast.makeText(getApplicationContext(), previous_poombun, Toast.LENGTH_LONG).show();

                                        // ???????????? ????????? push
                                        ArrayList<Push_ListItem> findingItems = new ArrayList<>();

                                        // Navershopping ??????
                                        poombun=previous_poombun;
                                        try{

                                            String text = URLEncoder.encode(poombun, "UTF-8"); //poombun

                                            // ????????? api ?????? ??????: display: ????????????, sort=asc:????????????
                                            //fav??? ???????????? 5??? ??? : display=5
                                            String apiURL = "https://openapi.naver.com/v1/search/shop.xml?query="+ text
                                                    +"&display=5" +"&sort=asc"; // xml ??????

                                            URL url = new URL(apiURL);
                                            HttpURLConnection con = (HttpURLConnection)url.openConnection();
                                            con.setRequestMethod("GET");
                                            con.setRequestProperty("X-Naver-Client-Id", clientId);
                                            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                                            int responseCode = con.getResponseCode();
                                            BufferedReader br;

                                            //Toast.makeText(getApplicationContext(), "????????????", Toast.LENGTH_LONG).show();

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

                                            // ?????? ?????? for ??????
                                            for(Shop shop : parsingResult) {
                                                Bitmap thumbImg = getBitmapFromURL(shop.getImage());
                                                // ?????? ??????
                                                findingItems.add(new Push_ListItem(shop.getTitle(),thumbImg,
                                                        shop.getLprice() + "???", shop.getLprice(),
                                                        shop.getLink(),
                                                        shop.getMallName()));
                                            }
                                            if(findingItems.size()!=0) {
                                                //????????? ?????? ??????(?????????)
                                                Push_ListItem poombun_main_list = findingItems.get(0);
                                                new_price = poombun_main_list.getPrice();
                                                //Toast.makeText(getApplicationContext(), poombun+" "+Integer.toString(new_price), Toast.LENGTH_LONG).show();
                                            }
                                        } catch (Exception e) { //????????????
                                            System.out.println(e);
                                            //Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                                        }

                                        //--????????????

                                        // ?????? ??????
                                        CharSequence previous_price = items.get(pos).getprice();
                                        String previous_price_string = previous_price.toString().substring(0, previous_price.length() - 1);
                                        int previous_price_int = Integer.parseInt(previous_price_string);

                                        //?????? ????????? ????????? <
                                        if (previous_price_int <= new_price) {    //test ?????? ????????????

                                            Toast.makeText(getApplicationContext(), "??????: " + previous_poombun + " ????????????: " + previous_price_string + " ???????????????: " + Integer.toString(new_price), Toast.LENGTH_LONG).show();
                                            createNotification(); //???????????? ??????

                                            //DB??????
                                            Push_ListItem poombun_main = findingItems.get(0);
                                            String price = Integer.toString(new_price) + "???";
                                            Bitmap thumbnail_main_value = poombun_main.getThumb();
                                            String img = BitMapToString(thumbnail_main_value);

                                            //????????? ????????? ??????> ???????????? ??????
                                            save_values(previous_poombun, price, img);
                                            //Toast.makeText(getApplicationContext(), "??????!", Toast.LENGTH_LONG).show();

                                            previous_price_int=new_price;

                                        }else if(previous_price_int > new_price){ // ????????? ????????? ??????
                                            Toast.makeText(getApplicationContext(), "??????: " + previous_poombun + " ????????????: " + previous_price_string + " ???????????????: " + Integer.toString(new_price), Toast.LENGTH_LONG).show();

                                            //DB??????
                                            Push_ListItem poombun_main = findingItems.get(0);
                                            String price = Integer.toString(new_price) + "???";
                                            Bitmap thumbnail_main_value = poombun_main.getThumb();
                                            String img = BitMapToString(thumbnail_main_value);

                                            //????????? ????????? ??????> ???????????? ??????
                                            save_values(previous_poombun, price, img);

                                        }
                                        else {

                                            Toast.makeText(getApplicationContext(), "?????? ?????? ?????? "+"??????: " + previous_poombun + " ????????????: " + previous_price_string + " ???????????????: " + Integer.toString(new_price), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }, 0);
                            }
                        };

                        timer = new Timer();
                        timer.schedule(tt,0,10000); //1?????? 1???

                    }else{
                        // timer ??????
                        timer.cancel();
                        timer.purge();
                        timer=null;

                        // ???????????? ?????????
                        //load_values();
                    }
                }
            });

            return convertView;
        }

        /* ????????? ????????? ????????? ?????? ??????. ????????? ??????????????? ?????? */
        public void addItem(String name, String price, Bitmap img) {

            SingerItem mItem = new SingerItem(name, price,img);

            /* MyItem??? ???????????? setting??????. */
            mItem.setImage(img);
            mItem.setName(name);
            mItem.setprice(price);

            /* mItems??? MyItem??? ????????????. */
            items.add(mItem);

        }

    }




    // DB
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

        if (favoitesDB != null) {
            String sqlCreateTbl = "CREATE TABLE IF NOT EXISTS CONTACT_T (" +
                    "POOMBUN "         + "TEXT," +
                    "PRICE "        + "TEXT," +
                    "IMG "       + "TEXT" + ")" ;

            System.out.println(sqlCreateTbl) ;

            favoitesDB.execSQL(sqlCreateTbl) ;
        }
    }
    // DB ????????? ??????????????? ??????
    private void load_values() {
        SingerAdapter mMyAdapter = new SingerAdapter();

        if (favoitesDB != null) {
            String sqlQueryTbl = "SELECT * FROM CONTACT_T" ;
            Cursor cursor = null ;

            // ?????? ??????
            cursor = favoitesDB.rawQuery(sqlQueryTbl, null) ;

            if (cursor.moveToNext()) { // ???????????? ???????????????,

                // ???????????? ?????? ??????
                int cnt=cursor.getCount();
                //Toast.makeText(getApplicationContext(), Integer.toString(cnt), Toast.LENGTH_LONG).show();



                do {
                    // poombunDB (TEXT) ??? ????????????.
                    String poombunDB = cursor.getString(0);
                    // price (TEXT) ??? ????????????
                    String price = cursor.getString(1);
                    // img (TEXT) ??? ????????????
                    String img = cursor.getString(2);
                    // ????????? ??? ????????? ??????
                    mMyAdapter.addItem(new SingerItem(poombunDB,
                            price, StringToBitmap(img)));

                }while (cursor.moveToNext()); // ?????? ?????? ????????? ?????? ?????????


            }
        }
        // ??????????????? ????????? ??????
        gridView_favorites.setAdapter(mMyAdapter);
        // ???????????? ?????????(????????????)
        mMyAdapter.notifyDataSetChanged();
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
    // ???????????? ????????? ????????? ?????? ????????? ????????? ?????? ??????
    class Push_ListItem{
        private Bitmap thumb;
        private String productName;
        private String price;
        private int int_price;
        private String url;
        private ArrayList<String> keywords;
        private String combinationKeyword;
        private String thumbUrl;
        private String mallName;

        public Push_ListItem(String productName, Bitmap thumb, String price, int int_price, String url,
                             String mallName) {
            this.thumb = thumb;
            this.productName = productName;
            this.price = price;
            this.int_price = int_price;
            this.url = url;
            this.keywords = keywords;
            this.combinationKeyword = combinationKeyword;
            this.thumbUrl = thumbUrl;
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



    // String?????? BitMap?????? ?????????????????? ??????
    public Bitmap StringToBitmap(String encodedString) {
        try { byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage(); return null;
        }
    }



}