package x.textdisc;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import x.tempdisc.R;

public class TextDisc extends View {
    final static String UNIT_C = "C";
    final static String UNIT_CIRCLE = "°";
    final static int DEF_MIN_VALUE = 22;
    final static int DEF_MAX_VALUE = 44;
    final static int DEF_VALUE = 22;
    Context mContext;
    int minValue = DEF_MIN_VALUE;
    int maxValue = DEF_MAX_VALUE;
    //attrs
    int firstDiscColor;
    int secondDiscColor;
    int firstDiscRadius;
    int secondDiscRadius;
    int checkedProgressColor;
    int unCheckedProgressColor;
    int progressOutRadius;
    int progressStrokeWidth;
    int progressDashCount;
    int arrowColor;
    int textColor;
    float textSize;
    int dashLineWidth;
    int arrowWidth;
    int arrowHeight;
    float unitTextSize;
    int unitInterval;
    int step;
    //show info
    float dashAngle;
    int progress;
    int value;
    float sweepAngle;
    //some resolved attrs
    // int progressInRadius;
    RectF progressOutRect;
    RectF progressInRect;

    Bitmap bmpArrowUp;
    Bitmap bmpArrowDown;
    Rect mSrcArrowUpRect;
    Rect mDestArrowUpRect;
    Rect mSrcArrowDownRect;
    Rect mDestArrowDownRect;

    Paint paint = new Paint();

    //click.
    int extendClickArea;
    Rect mDestClickArrowDownRect;
    Rect mDestClickArrowUpRect;

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public TextDisc(Context context) {
        this(context, null);
    }

    public TextDisc(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextDisc(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        resolveAttrs(context, attrs);
        resolveResource();
        resolveSize();
    }

    void resolveAttrs(Context context, @Nullable AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TextDisc);
        firstDiscColor = ta.getColor(R.styleable.TextDisc_firstDiscColor, getResources().getColor(R.color.def_firstDiscColor));
        secondDiscColor = ta.getColor(R.styleable.TextDisc_secondDiscColor, getResources().getColor(R.color.def_secondDisColor));
        firstDiscRadius = ta.getDimensionPixelSize(R.styleable.TextDisc_firstDiscRadius, getResources().getDimensionPixelSize(R.dimen.def_firstDiscRadius));
        secondDiscRadius = ta.getDimensionPixelSize(R.styleable.TextDisc_secondDiscRadius, getResources().getDimensionPixelSize(R.dimen.def_secondDiscRadius));
        checkedProgressColor = ta.getColor(R.styleable.TextDisc_checkedProgressColor, getResources().getColor(R.color.def_checkedProgressColor));
        unCheckedProgressColor = ta.getColor(R.styleable.TextDisc_unCheckedProgressColor, getResources().getColor(R.color.def_unCheckedProgressColor));
        progressOutRadius = ta.getDimensionPixelSize(R.styleable.TextDisc_progressOutRadius, getResources().getDimensionPixelSize(R.dimen.def_progressOutRadius));
        progressStrokeWidth = ta.getDimensionPixelSize(R.styleable.TextDisc_progressStrokeWidth, getResources().getDimensionPixelSize(R.dimen.def_progressStrokeWidth));
        progressDashCount = ta.getInteger(R.styleable.TextDisc_progressDashCount, getResources().getInteger(R.integer.def_progressDashCount));
        arrowColor = ta.getColor(R.styleable.TextDisc_arrowColor, getResources().getColor(R.color.def_arrowColor));
        textColor = ta.getColor(R.styleable.TextDisc_textColor, getResources().getColor(R.color.def_textColor));
        textSize = ta.getDimensionPixelSize(R.styleable.TextDisc_textSize, getResources().getDimensionPixelSize(R.dimen.def_textSize));
        step = ta.getInteger(R.styleable.TextDisc_step, 1);
        dashLineWidth = getResources().getDimensionPixelSize(R.dimen.dash_line_width);
        arrowWidth = getResources().getDimensionPixelSize(R.dimen.arrow_width);
        arrowHeight = getResources().getDimensionPixelSize(R.dimen.arrow_height);
        unitTextSize = getResources().getDimensionPixelSize(R.dimen.unit_textSize);
        extendClickArea = getResources().getDimensionPixelSize(R.dimen.extend_click_area);
        unitInterval = getResources().getDimensionPixelSize(R.dimen.unit_interval);
        dashAngle = 360.0f / progressDashCount;
        value = DEF_VALUE;
        ta.recycle();
    }

    void resolveResource() {
        Bitmap bmup = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_arrow_up)).getBitmap();
        bmpArrowUp = Bitmap.createScaledBitmap(bmup, arrowWidth, arrowHeight, false);
        //bmup.recycle();
        Bitmap bmdown = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_arrow_down)).getBitmap();
        bmpArrowDown = Bitmap.createScaledBitmap(bmdown, arrowWidth, arrowHeight, false);
        //bmdown.recycle();
    }

    void resolveSize() {
        // progressInRadius=progressOutRadius-progressStrokeWidth;
        int arrowUpLeft = firstDiscRadius - bmpArrowUp.getWidth() / 2;
        int arrowUpRight = firstDiscRadius + bmpArrowUp.getWidth() / 2;
        int arrowUpBottom = firstDiscRadius - secondDiscRadius * 2 / 3;
        int arrowUpTop = arrowUpBottom - bmpArrowUp.getHeight();
        mDestArrowUpRect = new Rect(arrowUpLeft, arrowUpTop, arrowUpRight, arrowUpBottom);
        int arrowDownLeft = firstDiscRadius - bmpArrowDown.getWidth() / 2;
        int arrowDownRight = firstDiscRadius + bmpArrowDown.getWidth() / 2;
        int arrowDownTop = firstDiscRadius + secondDiscRadius * 2 / 3;
        int arrowDownBottom = arrowDownTop + bmpArrowDown.getHeight();
        mDestArrowDownRect = new Rect(arrowDownLeft, arrowDownTop, arrowDownRight, arrowDownBottom);
        mSrcArrowDownRect = new Rect(0, 0, bmpArrowDown.getWidth(), bmpArrowDown.getHeight());
        mSrcArrowUpRect = new Rect(0, 0, bmpArrowUp.getWidth(), bmpArrowUp.getHeight());
        progressOutRect = new RectF(firstDiscRadius - progressOutRadius, firstDiscRadius - progressOutRadius, firstDiscRadius + progressOutRadius, firstDiscRadius + progressOutRadius);
        progressInRect = new RectF(progressOutRect.left + progressStrokeWidth, progressOutRect.top + progressStrokeWidth, progressOutRect.right - progressStrokeWidth, progressOutRect.bottom - progressStrokeWidth);
        mDestClickArrowDownRect = new Rect(arrowDownLeft - extendClickArea, arrowDownTop - extendClickArea, arrowDownRight + extendClickArea, arrowDownBottom + extendClickArea);
        mDestClickArrowUpRect = new Rect(arrowUpLeft - extendClickArea, arrowUpTop - extendClickArea, arrowUpRight + extendClickArea, arrowUpBottom + extendClickArea);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (width < firstDiscRadius * 2) {
            width = firstDiscRadius;
        }
        if (height < firstDiscRadius * 2) {
            height = firstDiscRadius * 2;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        progress = (int) ((value - minValue) * 100.0f / (maxValue - minValue));
        drawBg(canvas);
        drawDivider(canvas);
        drawText(canvas);
    }

    void drawBg(Canvas canvas) {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(firstDiscColor);
        canvas.drawCircle(firstDiscRadius, firstDiscRadius, firstDiscRadius, paint);
        //draw unchecked progress.
        sweepAngle = 360.0f * progress / progressDashCount;
        paint.setColor(checkedProgressColor);
        canvas.save();
        canvas.drawArc(progressOutRect, -180, sweepAngle, true, paint);
        paint.setColor(unCheckedProgressColor);
        canvas.drawArc(progressOutRect, sweepAngle - 180, 360 - sweepAngle, false, paint);
        canvas.restore();
        paint.setColor(firstDiscColor);
        canvas.drawCircle(firstDiscRadius, firstDiscRadius, progressOutRadius - progressStrokeWidth, paint);
        paint.setColor(secondDiscColor);
        canvas.drawCircle(firstDiscRadius, firstDiscRadius, secondDiscRadius, paint);
        //arrow
        paint.reset();
        paint.setAntiAlias(true);
        canvas.drawBitmap(bmpArrowDown, mSrcArrowDownRect, mDestArrowDownRect, paint);
        canvas.drawBitmap(bmpArrowUp, mSrcArrowUpRect, mDestArrowUpRect, paint);
    }

    void drawDivider(Canvas canvas) {
        canvas.save();
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(firstDiscColor);
        paint.setStrokeWidth(dashLineWidth);
        for (int i = 0; i < progressDashCount; i++) {
            canvas.drawLine(firstDiscRadius, firstDiscRadius - progressOutRadius, firstDiscRadius, firstDiscRadius - progressOutRadius + progressStrokeWidth, paint);
            canvas.rotate(dashAngle, firstDiscRadius, firstDiscRadius);
        }
        canvas.restore();
    }

    void drawText(Canvas canvas) {
        String valueStr = String.valueOf(value);
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(textColor);
        paint.setTextSize(textSize);
        Paint.FontMetrics fm1 = paint.getFontMetrics();
        float valueBottom = firstDiscRadius - (fm1.bottom - fm1.top) / 2 - fm1.top;
        float valueWidth = paint.measureText(valueStr);
        paint.setTextSize(unitTextSize);
        Paint.FontMetrics fm2 = paint.getFontMetrics();
        float unitHeight = -fm2.ascent;
        float unitWidth = paint.measureText(UNIT_C);
        float textWidth = valueWidth + unitWidth + unitInterval;
        //draw text.
        paint.setTextSize(textSize);
        canvas.drawText(valueStr, firstDiscRadius - textWidth / 2, valueBottom, paint);
        paint.setTextSize(unitTextSize);
        canvas.drawText(UNIT_C, firstDiscRadius + textWidth / 2 + unitInterval, valueBottom, paint);
        //draw circle.
        canvas.drawText(UNIT_CIRCLE, firstDiscRadius + textWidth / 2 + unitInterval, valueBottom - unitHeight, paint);
    }

    public void setValue(int _value,boolean isForce) {
        if(isForce){
            value=_value;
        }else{
            if (_value < minValue) {
                value = minValue;
            } else if (_value > maxValue) {
                value = maxValue;
            } else {
                value = _value;
            }
        }
        invalidate();
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (event.getX() > mDestClickArrowUpRect.left && event.getX() < mDestClickArrowUpRect.right
                    && event.getY() > mDestClickArrowUpRect.top && event.getY() < mDestClickArrowUpRect.bottom) {
                if (mOnSelectListener != null) {
                    mOnSelectListener.beforeSelect();
                }
                if (value < maxValue) {
                    value += step;
                    if (value > maxValue) {
                        value = maxValue;
                    }
                    if (mOnSelectListener != null) {
                        mOnSelectListener.onSelect(value);
                    }
                }
                invalidate();
            } else if (event.getX() > mDestClickArrowDownRect.left && event.getX() < mDestClickArrowDownRect.right
                    && event.getY() > mDestClickArrowDownRect.top && event.getY() < mDestClickArrowDownRect.bottom) {
                if (mOnSelectListener != null) {
                    mOnSelectListener.beforeSelect();
                }
                if (value > minValue) {
                    value -= step;
                    if (value < minValue) {
                        value = minValue;
                    }
                    if (mOnSelectListener != null) {
                        mOnSelectListener.onSelect(value);
                    }
                }
                invalidate();
            }


        }

        return true;
    }

    public void setOnSelectListener(OnSelectListener mOnSelectListener) {
        this.mOnSelectListener = mOnSelectListener;
    }

    OnSelectListener mOnSelectListener;

    public interface OnSelectListener {
        void onSelect(int value);
        void beforeSelect();
    }
}
