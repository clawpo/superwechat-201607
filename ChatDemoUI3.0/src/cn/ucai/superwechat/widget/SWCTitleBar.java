package cn.ucai.superwechat.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by clawpo on 2016/12/8.
 */

public class SWCTitleBar extends RelativeLayout {
    protected RelativeLayout a;
    protected ImageView b;
    protected RelativeLayout c;
    protected RelativeLayout d;
    protected ImageView e;
    protected TextView f;
    protected TextView g;
    protected TextView h;
    protected RelativeLayout i;

    public SWCTitleBar(Context var1, AttributeSet var2, int var3) {
        this(var1, var2);
    }

    public SWCTitleBar(Context var1, AttributeSet var2) {
        super(var1, var2);
        this.a(var1, var2);
    }

    public SWCTitleBar(Context var1) {
        super(var1);
        this.a(var1, (AttributeSet)null);
    }

    private void a(Context var1, AttributeSet var2) {
        LayoutInflater.from(var1).inflate(com.easemob.redpacketui.R.layout.rp_widget_title_bar, this);
        this.a = (RelativeLayout)this.findViewById(com.easemob.redpacketui.R.id.left_layout);
        this.b = (ImageView)this.findViewById(com.easemob.redpacketui.R.id.left_image);
        this.c = (RelativeLayout)this.findViewById(com.easemob.redpacketui.R.id.right_layout);
        this.d = (RelativeLayout)this.findViewById(com.easemob.redpacketui.R.id.right_text_layout);
        this.e = (ImageView)this.findViewById(com.easemob.redpacketui.R.id.right_image);
        this.f = (TextView)this.findViewById(com.easemob.redpacketui.R.id.right_text);
        this.g = (TextView)this.findViewById(com.easemob.redpacketui.R.id.title);
        this.h = (TextView)this.findViewById(com.easemob.redpacketui.R.id.subtitle);
        this.i = (RelativeLayout)this.findViewById(com.easemob.redpacketui.R.id.root);
        this.b(var1, var2);
    }

    private void b(Context var1, AttributeSet var2) {
        if(var2 != null) {
            TypedArray var3 = var1.obtainStyledAttributes(var2, com.easemob.redpacketui.R.styleable.app);
            String var4 = var3.getString(com.easemob.redpacketui.R.styleable.app_RPmytitle);
            int var5 = var3.getColor(com.easemob.redpacketui.R.styleable.app_RPtitleTextColor, ContextCompat.getColor(var1, com.easemob.redpacketui.R.color.title_color));
            float var6 = var3.getDimension(com.easemob.redpacketui.R.styleable.app_RPtitleTextSize, 17.0F);
            this.g.setText(var4);
            this.g.setTextColor(var5);
            this.g.setTextSize(var6);
            String var7 = var3.getString(com.easemob.redpacketui.R.styleable.app_RPrightText);
            int var8 = var3.getColor(com.easemob.redpacketui.R.styleable.app_RPrightTextColor, ContextCompat.getColor(var1, com.easemob.redpacketui.R.color.title_color));
            float var9 = var3.getDimension(com.easemob.redpacketui.R.styleable.app_RPrightTextSize, 15.0F);
            this.f.setText(var7);
            this.f.setTextColor(var8);
            this.f.setTextSize(var9);
            String var10 = var3.getString(com.easemob.redpacketui.R.styleable.app_RPsubTitleText);
            int var11 = var3.getColor(com.easemob.redpacketui.R.styleable.app_RPsubTitleTextColor, ContextCompat.getColor(var1, com.easemob.redpacketui.R.color.title_transparent_color));
            float var12 = var3.getDimension(com.easemob.redpacketui.R.styleable.app_RPsubTitleTextSize, 10.0F);
            this.h.setText(var10);
            this.h.setTextColor(var11);
            this.h.setTextSize(var12);
            Drawable var13 = var3.getDrawable(com.easemob.redpacketui.R.styleable.app_RPleftImage);
            if(null != var13) {
                this.b.setImageDrawable(var13);
            }

            Drawable var14 = var3.getDrawable(com.easemob.redpacketui.R.styleable.app_RPrightImage);
            if(null != var14) {
                this.e.setImageDrawable(var14);
            }

            Drawable var15 = var3.getDrawable(com.easemob.redpacketui.R.styleable.app_RPtitleBackground);
            if(null != var15) {
                this.i.setBackgroundDrawable(var15);
            }

            var3.recycle();
        }

    }

    public void setLeftImageResource(int var1) {
        this.b.setImageResource(var1);
    }

    public void setRightImageResource(int var1) {
        this.e.setImageResource(var1);
    }

    public void setLeftLayoutClickListener(OnClickListener var1) {
        this.a.setOnClickListener(var1);
    }

    public void setRightImageLayoutClickListener(OnClickListener var1) {
        this.c.setOnClickListener(var1);
    }

    public void setRightTextLayoutClickListener(OnClickListener var1) {
        this.d.setOnClickListener(var1);
    }

    public void setLeftLayoutVisibility(int var1) {
        this.a.setVisibility(var1);
    }

    public void setRightImageLayoutVisibility(int var1) {
        this.c.setVisibility(var1);
    }

    public void setRightTextLayoutVisibility(int var1) {
        this.d.setVisibility(var1);
    }

    public void setTitle(String var1) {
        this.g.setText(var1);
    }

    public void setSubTitle(String var1) {
        this.h.setText(var1);
    }

    public void setTitleColor(int var1) {
        this.g.setTextColor(var1);
    }

    public void setSubTitleColor(int var1) {
        this.h.setTextColor(var1);
    }

    public void setSubTitleVisibility(int var1) {
        this.h.setVisibility(var1);
    }

    public void setRightText(String var1) {
        this.f.setText(var1);
    }

    public void setBackgroundColor(int var1) {
        this.i.setBackgroundColor(var1);
    }

    public RelativeLayout getLeftLayout() {
        return this.a;
    }

    public RelativeLayout getRightImageLayout() {
        return this.c;
    }

    public RelativeLayout getRightTextLayout() {
        return this.d;
    }
}
