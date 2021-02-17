package md.intelectsoft.quickpos.verifone.transaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Locale;

import md.intelectsoft.quickpos.R;


public class SurfaceDrawCanvas extends SurfaceView implements SurfaceHolder.Callback {

    private Paint paint, paintText, mPaint;//
    private Canvas canvas;//
    private Bitmap bitmap;//
    // private Bitmap originBitmap;//
    private int mov_x;//
    private int mov_y;
    private String signaValue;
    final static int BUFFER_SIZE = 10000;
    public byte[] top;
    private int width, height;
    private int zoomMultiples = 1;// 放大倍数
    SurfaceHolder holder;

    private Rect textRect;
    private int textW = 0;
    private int textH = 0;
    private int leftOffset = 0;
    private int reSign_x = 0;
    private int reSign_y = 0;

    public SurfaceDrawCanvas(Context context, String signValue, int screenWidth, int width, int height) {
        super(context);
        holder = this.getHolder();
        holder.addCallback(this);
        String numEnd = "";

        numEnd = signValue;

        this.signaValue = numEnd;
        this.width = width;
        this.height = height;
        Log.d("", "screenWidth" + screenWidth);
        Log.d("", "width" + width);
        if (width > screenWidth) {
            leftOffset = (width - screenWidth) / 2;
        }
        Log.d("", "" + leftOffset);
        init();
    }

    private void init() {
        zoomMultiples = height / 100;
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        // paint.setStyle(Style.FILL);//
        paint.setStrokeWidth(4);//
        // paint.setFakeBoldText(true);
        paint.setLinearText(true);
        // paint.setSubpixelText(true);
        // paint.setFilterBitmap(true);
        paint.setStrokeCap(Cap.ROUND);//
        paint.setDither(true);//
        paint.setAntiAlias(true);// ?


        paintText = new Paint();
        String familyName = "Times New Roman";
        Typeface font = Typeface.create(familyName, Typeface.NORMAL);
        paintText.setTypeface(font);
        paintText.setTextSize(15 * zoomMultiples);
        paintText.setAntiAlias(true);// 锯齿不显示
        paintText.setColor(getResources().getColor(R.color.black));
        paintText.setStyle(Style.FILL);// 设置非填充
        // paintText.setStrokeWidth(4* zoomMultiples);// 笔宽5像素

        mPaint = new Paint();
        mPaint.setStrokeWidth(3);
        mPaint.setTextSize(40);
        mPaint.setColor(Color.GRAY);
        mPaint.setTextAlign(Paint.Align.LEFT);

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); //
        bitmap.eraseColor(Color.parseColor("#e5e6ea"));
        canvas = new Canvas();
        canvas.setBitmap(bitmap);

        textRect = new Rect();
        // 获取特征值所需矩阵
        paintText.getTextBounds(signaValue, 0, signaValue.length(), textRect);
        textW = textRect.width();// 获取特征值矩阵的宽
        textH = textRect.height();// 获取特征值矩阵的高

        textRect = new Rect((width - textW) / 2 + leftOffset,
                (height - textH) / 2, (width - textW) / 2 + textW + leftOffset,
                (height - textH) / 2 + textH);
        // canvas.drawRect(textRect, paint);
        // 画上特征值， 不知为何画文字时会以左下为坐标原点
//        canvas.drawText(this.signaValue, (width - textW) / 2, (height - textH) / 2 + textH, paintText);

//        canvas.setBitmap(bitmap);

        Log.d("Surface", "w:" + bitmap.getWidth());
        Log.d("Surface", "h:" + bitmap.getHeight());
        myDrawText(canvas, mPaint);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        new MyThread().start();
        threadFlag = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        threadFlag = false;
    }

    boolean threadFlag = false;

    class MyThread extends Thread {
        @Override
        public void run() {
            while (threadFlag) {
                long startTime = System.currentTimeMillis();

                myDraw();
                long endTime = System.currentTimeMillis();
                /*
                 * 1000ms /60 = 16.67ms
                 */
                if (endTime - startTime < 30) {
                    try {
                        Thread.sleep(30 - (endTime - startTime));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void resetCanvas() {
        isOnTouch = false;
        isCoverTrait = false;
        bitmap = Bitmap.createBitmap(width + 30, height, Bitmap.Config.ARGB_8888); // 设置位图的宽高
        bitmap.eraseColor(Color.parseColor("#e5e6ea"));
        canvas.setBitmap(bitmap);
//        canvas.drawText(this.signaValue, (width - textW) / 2, (height - textH) / 2 + textH, paintText);
        canvas.setBitmap(bitmap);
    }

    public Bitmap saveCanvas() {
        if (isOnTouch) {
//            saveBitmapFile(bitmap);
            if (bitmap == null) {
                return null;
            }

            myDrawText(canvas, mPaint);
            Bitmap newbmp = mycompress(bitmap);
            return newbmp;
        } else {
            return null;
        }
    }

/*    public void saveBitmapFile(Bitmap bitmap) {
        File file = new File("/mnt/sdcard/simple.bmp");//将要保存图片的路径
        try {
            mPaint.setColor(Color.parseColor("#e5e6ea"));
            myDrawText(canvas, mPaint);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/


/*    public byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }*/

    private Bitmap mycompress(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale((float) 384 / bitmap.getWidth(), (float) 200 / bitmap.getHeight());
        // matrix.postRotate(180);
        Bitmap compressBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        return compressBitmap;
    }

    protected void myDraw() {
        Canvas draw_canvas = holder.lockCanvas();
        if (draw_canvas == null) {
            return;
        }
        draw_canvas.drawBitmap(bitmap, 0, 0, paint);
        holder.unlockCanvasAndPost(draw_canvas);
    }

    protected void myDrawText(Canvas canvas, Paint mPaint) {
        String signString = getResources().getString(R.string.signyourname);

        Rect bounds = new Rect();
        mPaint.getTextBounds(signString, 0, signString.length(), bounds);
        canvas.drawText(signString, bounds.width() / 4, bounds.height(), mPaint);

        reSign_x = this.width - bounds.width();
        reSign_y = this.height - bounds.height();

        Bitmap resign_bmp = null;//
        if (Locale.getDefault().getLanguage().equals("zh"))
            resign_bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.resign);
        else
            resign_bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.resign_en);
        Log.d("Surface", String.valueOf(resign_bmp));
        canvas.drawBitmap(resign_bmp, 570, 358, paint);
    }

    private boolean isOnTouch = false;//
    private boolean isCoverTrait = false;//
    private int down_x = 0;
    private int down_y = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!isOnTouch) {
                down_x = (int) event.getX();
                down_y = (int) event.getY();
            }
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            canvas.drawLine(mov_x, mov_y, event.getX(), event.getY(), paint);
            if (!isOnTouch && (Math.abs(down_x - mov_x) > 100 || Math.abs(down_y - mov_y) > 50)) {
                isOnTouch = true;
            }
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mov_x = (int) event.getX();
            mov_y = (int) event.getY();
        }
        mov_x = (int) event.getX();
        mov_y = (int) event.getY();

        if ((mov_x > (reSign_x - 20) && mov_x < this.width) && (mov_y > (reSign_y - 20) && mov_y < this.height)) {
            resetCanvas();
            myDrawText(canvas, mPaint);
        }
        return true;
    }
}
