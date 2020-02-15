package example.com.solutvent.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import example.com.solutvent.Model.User;
import example.com.solutvent.Remote.APIService;
import example.com.solutvent.Remote.GoogleRetrofitClient;
import example.com.solutvent.Remote.IGoogleService;
import example.com.solutvent.Remote.RetrofitClient;

public class Common {
    public static final Object DISABLE_TAG = "DISABLE";
    public static String PHONE_TEXT = "userPhone";
    public static final String KEY_ENABLE_BUTTON_NEXT = "ENABLE_BUTTON_NEXT";
    public static final String KEY_TIME_SLOT = "TIME_SLOT";
    public static final String KEY_STEP = "STEP";
    public static final String KEY_CONFIRM_BOOKING = "CONFIRM_BOOKING";
    public static final int FIVE_MINUTES = 5 * 60 * 1000;
    public static final int TWO_MINUTES = 2 * 60 * 1000;
    public static int currentTimeSlot = -1;
    public static User currentUser;
    public static User currentCompany;
    public static Calendar currentDate = Calendar.getInstance();
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy");

    public static int step = 0;
    public static final int TIME_SLOT_TOTAL = 10;

    public static final String INTENT_EVENT_ID = "eventId";

    private static final String BASE_URL = "https://fcm.googleapis.com";
    private static final String GOOGLE_API_URL = "https://maps.googleapis.com";

    public static APIService getFCMService() {
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

    public static IGoogleService getGoogleMapAPI() {
        return GoogleRetrofitClient.getGoogleClient(GOOGLE_API_URL).create(IGoogleService.class);
    }

    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";
    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";

    public static final int PICK_IMAGE_REQUEST = 71;

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {

            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }

    public static BigDecimal formatCurrency(String amount, Locale locale) throws java.text.ParseException{
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        if (format instanceof DecimalFormat)
            ((DecimalFormat) format).setParseBigDecimal(true);
        return (BigDecimal)format.parse(amount.replace("[^\\d.,]", ""));
    }

    public static String convertTimeSlotToString(int slot){
       switch(slot){
           case 0:
               return "9.00-10:00";
           case 1:
               return "10.00-11.00";
           case 2:
               return "11.00-12.00";
           case 3:
               return "12.00-13.00";
           case 4:
               return "13.00-14.00";
           case 5:
               return "14.00-15.00";
           case 6:
               return "15.00-16.00";
           case 7:
               return "16.00-17.00";
           case 8:
               return "17.00-18.00";
           case 9:
               return "18.00-19.00";
           default:
               return "Closed";
       }
    }
}
