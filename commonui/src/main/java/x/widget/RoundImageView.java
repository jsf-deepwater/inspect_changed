package x.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

public class RoundImageView extends AppCompatImageView {
    private float width;
    private float height;
    private float radius;
    private Paint paint=new Paint();
    private Matrix matrix=new Matrix();

    public RoundImageView(Context context) {
        this(context,null);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Shader shader=initBitmapShader();
        if(shader!=null){
            paint.setAntiAlias(true);
            paint.setShader(initBitmapShader());
            canvas.drawCircle(width/2,height/2,radius,paint);
        }else{
            super.onDraw(canvas);
        }
    }

    private Shader initBitmapShader() {
     if(getDrawable() instanceof BitmapDrawable){
         Bitmap bitmap=((BitmapDrawable)getDrawable()).getBitmap();
         BitmapShader bitmapShader=new BitmapShader(bitmap,Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);
         float scale=Math.max(width/bitmap.getWidth(),height/bitmap.getHeight());
         matrix.setScale(scale,scale);
         bitmapShader.setLocalMatrix(matrix);
         return bitmapShader;
     }
     return null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width=getMeasuredWidth();
        height=getMeasuredHeight();
        radius=Math.min(width,height)/2;
    }
}
