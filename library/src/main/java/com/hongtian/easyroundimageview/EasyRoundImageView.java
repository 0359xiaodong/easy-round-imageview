package com.hongtian.easyroundimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by hongtianyou on 15/8/13.
 */
public class EasyRoundImageView extends ImageView {

    private boolean hasLeftTop;
    private boolean hasRightTop;
    private boolean hasLeftBottom;
    private boolean hasRightBottom;

    private float radius;

    private Rect mDrawableRect;
    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;
    private Paint mBitmapPaint;
    private Matrix mShaderMatrix;
    private int mBitmapWidth;
    private int mBitmapHeight;

    public EasyRoundImageView(Context context) {
        super(context);
    }

    public EasyRoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    public EasyRoundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs){
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EasyRoundImageView);
        hasLeftTop = typedArray.getBoolean(R.styleable.EasyRoundImageView_hasLeftTop, false);
        hasRightTop = typedArray.getBoolean(R.styleable.EasyRoundImageView_hasRightTop, false);
        hasLeftBottom = typedArray.getBoolean(R.styleable.EasyRoundImageView_hasLeftBottom, false);
        hasRightBottom = typedArray.getBoolean(R.styleable.EasyRoundImageView_hasRightBottom, false);
        radius = typedArray.getDimension(R.styleable.EasyRoundImageView_radius, 0f);
        typedArray.recycle();

        if (!hasLeftTop && !hasLeftBottom && !hasRightBottom && !hasRightTop){
            hasLeftBottom = true;
            hasRightBottom = true;
            hasLeftTop = true;
            hasRightTop = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (radius == 0f){
            super.onDraw(canvas);
        }else{
            if (getDrawable() == null) {
                return;
            }
            RectF rect = new RectF(getPaddingLeft(), getPaddingTop(), getRight()
                    - getLeft() - getPaddingRight(), getBottom() - getTop()
                    - getPaddingBottom());

            Path path = new Path();
            
            halfCirclePath(rect, path);

            canvas.drawPath(path, mBitmapPaint);
        }
    }

    public void halfCirclePath(RectF rect, Path path){

        float width = rect.right - rect.left;
        float height = rect.bottom - rect.top;
        if(radius > width / 2 || radius > height / 2){
            radius = Math.min(width, height)/2;
        }
        path.moveTo(rect.left + radius, rect.top);

        path.lineTo(rect.width() - radius, rect.top);
        if (hasRightTop){
            path.arcTo(new RectF(rect.right - 2*radius, rect.top, rect.right, rect.top+2*radius), 270, 90);
        }else{
            path.lineTo(rect.width(), rect.top);
        }
        path.lineTo(rect.width(), rect.bottom - radius);
        if (hasRightBottom){
            path.arcTo(new RectF(rect.right - 2*radius, rect.bottom - 2 * radius, rect.right, rect.bottom), 0, 90);
        }else{
            path.lineTo(rect.width(), rect.bottom);
        }
        path.lineTo(radius, rect.bottom);
        if (hasLeftBottom){
            path.arcTo(new RectF(rect.left, rect.bottom - 2 * radius, rect.left + 2 * radius, rect.bottom), 90, 90);
        }else{
            path.lineTo(rect.left, rect.bottom);
        }
        path.lineTo(rect.left, rect.top + radius);
        if (hasLeftTop){
            path.arcTo(new RectF(rect.left, rect.top, rect.left + 2 * radius, rect.top + 2 * radius), 180, 90);
        }else{
            path.lineTo(rect.left, rect.top);
        }
        path.close();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setup();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mBitmap = bm;
        setup();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        mBitmap = getBitmapFromDrawable(drawable);
        setup();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        mBitmap = getBitmapFromDrawable(getDrawable());
        setup();
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    private void setup() {
        if (mBitmap == null) {
            return;
        }

        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP);

        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setShader(mBitmapShader);

        mBitmapHeight = mBitmap.getHeight();
        mBitmapWidth = mBitmap.getWidth();

        updateShaderMatrix();
        invalidate();
    }

    private void updateShaderMatrix() {
        float scale;
        float dx = 0;
        float dy = 0;

        mShaderMatrix = new Matrix();
        mShaderMatrix.set(null);

        mDrawableRect = new Rect(0, 0, getRight() - getLeft(), getBottom()
                - getTop());

        if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width()
                * mBitmapHeight) {
            scale = mDrawableRect.height() / (float) mBitmapHeight;
            dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f;
        } else {
            scale = mDrawableRect.width() / (float) mBitmapWidth;
            dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f;
        }

        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));

        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }
    
}
