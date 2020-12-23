package x.dashline;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class DashLine extends View {
    final static int VERTICAL = 0;
    final static int HORIZONTAL = 1;
    int orientation;
    int color;
    int dash_width;
    int dash_gap;
    int line_width;
    Paint paint=new Paint();
    int width, height;

    public DashLine(Context context) {
        super(context);
    }

    public DashLine(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DashLine);
        orientation = ta.getInt(R.styleable.DashLine_orientation, HORIZONTAL);
        color = ta.getColor(R.styleable.DashLine_color, getResources().getColor(android.R.color.darker_gray));
        dash_width = ta.getDimensionPixelSize(R.styleable.DashLine_dash_width, getResources().getDimensionPixelSize(R.dimen.def_dash_width));
        dash_gap = ta.getDimensionPixelSize(R.styleable.DashLine_dash_gap, getResources().getDimensionPixelSize(R.dimen.def_dash_gap));
        line_width = ta.getDimensionPixelSize(R.styleable.DashLine_line_width, getResources().getDimensionPixelSize(R.dimen.def_line_width));
        ta.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        this.width = width;
        this.height = height;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setStrokeWidth(line_width);
        float startX, startY;
        if(orientation==HORIZONTAL){
            startY=height/2.0f;
            startX=0;
        }else{
            startX=width/2.0f;
            startY=0;
        }
        if(dash_gap==0){
            canvas.drawLine(orientation==HORIZONTAL?0:width/2.0f,orientation==HORIZONTAL?height/2.0f:0,orientation==HORIZONTAL?width:width/2.0f,orientation==HORIZONTAL?height/2.0f:height,paint);
        }else{
            int max=orientation==HORIZONTAL?width:height;
            float[] pts=new float[4*(max/(dash_gap+dash_width)+1)];
            for(int i=0;i<(max/(dash_gap+dash_width)+1);i++){
                int index=i*4;
                pts[index]=orientation==HORIZONTAL?(startX+i*(dash_gap+dash_width)):startX;//startx
                pts[index+1]=orientation==HORIZONTAL?startY:(startY+i*(dash_gap+dash_width));//starty
                pts[index+2]=orientation==HORIZONTAL?(startX+i*(dash_gap+dash_width)+dash_width):startX;//endx
                pts[index+3]=orientation==HORIZONTAL?startY:(startY+i*(dash_gap+dash_width)+dash_width);//endy
            }
            canvas.drawLines(pts,paint);
        }

    }
}
