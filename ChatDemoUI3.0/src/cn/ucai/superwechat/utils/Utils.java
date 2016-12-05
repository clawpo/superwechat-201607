package cn.ucai.superwechat.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;


/**
 * Created by wei on 2016/6/2.
 */
public class Utils {
    public static void hideKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) SuperWeChatApplication.getInstance().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) SuperWeChatApplication.getInstance().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }
    public static int getGiftImage(int giftId){
        if(giftId>0) {
            return SuperWeChatApplication.getInstance().getResources()
                    .getIdentifier(I.GIFT_PREFIX_MSG+giftId,"drawable",
                            SuperWeChatApplication.getInstance().getPackageName());
        }else{
            return R.drawable.gift_star;
        }
    }
}
