package com.hanry;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import com.hanry.command.CategoryBit;
import com.hanry.command.Command;
import com.hanry.command.CommandBit;
import com.hanry.command.ValueBit;

public class MjpegView extends SurfaceView implements SurfaceHolder.Callback, OnTouchListener {
    public final static int POSITION_UPPER_LEFT = 9;
    public final static int POSITION_UPPER_RIGHT = 3;
    public final static int POSITION_LOWER_LEFT = 12;
    public final static int POSITION_LOWER_RIGHT = 6;

    public final static int SIZE_STANDARD = 1;
    public final static int SIZE_BEST_FIT = 4;
    public final static int SIZE_FULLSCREEN = 8;
    private final static String SAVE_TO_DIR = "SmartCarImg";
    
    private MjpegViewThread thread;
    private MjpegInputStream mIn = null;
    private String mInUrl = null;
    private boolean showFps = true;
    private boolean mRun = false;
    private boolean surfaceDone = false;
    private Paint overlayPaint;
    private int overlayTextColor;
    private int overlayBackgroundColor;
    private int ovlPos;
    private int dispWidth;
    private int dispHeight;
    private int displayMode;
    private boolean resume = false;
    private boolean mtakePic = false;//flag for take a picture
    
    private Context context;
	private Point touchPointFirst;
	private Point touchPointSecond;
	private CommandSender commandSender;
	private DisplayMetrics displayMetrics;
	private int horValue;
	private int verValue;

    public class MjpegViewThread extends Thread {
        private SurfaceHolder mSurfaceHolder;
        private int frameCounter = 0;
        private long start;
        private Bitmap ovl;

        public MjpegViewThread(SurfaceHolder surfaceHolder, Context context) {
            mSurfaceHolder = surfaceHolder;
        }

        private Rect destRect(int bmw, int bmh) {
            int tempx;
            int tempy;
            if (displayMode == MjpegView.SIZE_STANDARD) {
                tempx = (dispWidth / 2) - (bmw / 2);
                tempy = (dispHeight / 2) - (bmh / 2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }
            if (displayMode == MjpegView.SIZE_BEST_FIT) {
                float bmasp = (float) bmw / (float) bmh;
                bmw = dispWidth;
                bmh = (int) (dispWidth / bmasp);
                if (bmh > dispHeight) {
                    bmh = dispHeight;
                    bmw = (int) (dispHeight * bmasp);
                }
                tempx = (dispWidth / 2) - (bmw / 2);
                tempy = (dispHeight / 2) - (bmh / 2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }
            if (displayMode == MjpegView.SIZE_FULLSCREEN)
                return new Rect(0, 0, dispWidth, dispHeight);
            return null;
        }

        public void setSurfaceSize(int width, int height) {
            synchronized (mSurfaceHolder) {
                dispWidth = width;
                dispHeight = height;
            }
        }

        private Bitmap makeFpsOverlay(Paint p, String text) {
            Rect b = new Rect();
            p.getTextBounds(text, 0, text.length(), b);
            int bwidth = b.width();
            int bheight = b.height();
            Bitmap bm = Bitmap.createBitmap(bwidth, bheight,
                    Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bm);
            p.setColor(overlayBackgroundColor);
            c.drawRect(0, 0, bwidth, bheight, p);
            p.setColor(overlayTextColor);
            p.setTextSize(40);
            c.drawText(text, -b.left + 1,
                    (bheight / 2) - ((p.ascent() + p.descent()) / 2) + 1, p);
            return bm;
        }

        public void run() {
            start = System.currentTimeMillis();
            Log.i("MjpegView", "playback thread started! time:" + start);
            PorterDuffXfermode mode = new PorterDuffXfermode(
                    PorterDuff.Mode.DST_OVER);
            Bitmap bm;
            int width;
            int height;
            Rect destRect;
            Canvas c = null;
            Paint p = new Paint();
            String fps = "";
            while (mRun) {
                if (surfaceDone) {
                    try {
                        c = mSurfaceHolder.lockCanvas();
                        synchronized (mSurfaceHolder) {
                            try {
                            	if (mIn == null && mInUrl != null) {
                            		mIn = MjpegInputStream.read(mInUrl);
                            	}
                            	
                            	if(null == mIn) { //URL无效，不能获取视频流
                            		//显示无效图片
                            		bm = BitmapFactory.decodeResource(getResources(), R.drawable.radialback);
                            		mtakePic = false;
                            		showFps = false;
                            	}else{
                            		showFps = true;
                            		bm = mIn.readMjpegFrame();
                                }
                                
                                if (mtakePic) {
                                	Log.i("MjpegView", "thread run start to take picture");
                                	String fName = generateFileName();
                                	Log.i("MjpegView", "mtakePic  " + fName);
                                	int res = saveBitmapToFile(bm, fName);
                                	BroardCastResult(res, fName);
                                	mtakePic = false;
                                }
                                destRect = destRect(displayMetrics.widthPixels, displayMetrics.heightPixels);
                                c.drawColor(Color.WHITE);
                                c.drawBitmap(bm, null, destRect, p);
                                if (showFps) {
                                    p.setXfermode(mode);
                                    if (ovl != null) {
                                        height = ((ovlPos & 1) == 1) ? destRect.top
                                                : destRect.bottom
                                                        - ovl.getHeight()-10;
                                        width = ((ovlPos & 8) == 8) ? destRect.left
                                                : destRect.right
                                                        - ovl.getWidth()-10;
                                        c.drawBitmap(ovl, width, height, null);
                                    }
                                    p.setXfermode(null);
                                    frameCounter++;
                                    if ((System.currentTimeMillis() - start) >= 1000) {
                                        fps = String.valueOf(frameCounter)
                                                + "fps";
                                        frameCounter = 0;
                                        start = System.currentTimeMillis();
                                        ovl = makeFpsOverlay(overlayPaint, fps);
                                    }
                                }
                            } catch (Exception e) {
                            	Log.e(this.getClass().toString(), "MjpegInputStream Error.");
                            	e.printStackTrace();
                            }
                        }
                    } finally {
                        if (c != null)
                            mSurfaceHolder.unlockCanvasAndPost(c);
                        if(null == mIn) {
                        	break;
                        }
                    }
                }
            }
        }
    }

    private void init(Context context) {

        this.context = context;
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        this.displayMetrics = getResources().getDisplayMetrics(); 
        this.setOnTouchListener(this);
        this.touchPointFirst = new Point();	//点击点
        this.touchPointSecond = new Point();	//点击点
        this.verValue = 90;
        this.horValue = 90;
        thread = new MjpegViewThread(holder, context);
        setFocusable(true);
        if (!resume) {
            resume = true;
            overlayPaint = new Paint();
            overlayPaint.setTextAlign(Paint.Align.LEFT);
            overlayPaint.setTextSize(12);
            overlayPaint.setTypeface(Typeface.DEFAULT);
            overlayBackgroundColor = Color.argb(00, 00, 00, 00);
            ovlPos = MjpegView.POSITION_LOWER_RIGHT;
            displayMode = MjpegView.SIZE_STANDARD;
            dispWidth = getWidth();
            dispHeight = getHeight();

            Log.i("MjpegView", "init successfully!");
        }
        setOverlayTextColor(Color.argb(127, 0x00, 0x00, 0x00));
        setKeepScreenOn(true);
    }

    public void startPlayback() {
        if (mIn != null || mInUrl != null) {
            mRun = true;
            try {
            	thread.start();
            } catch (IllegalThreadStateException e) {
            	Log.e("MjpegView", "ERROR! " + e.getMessage());
            }
        }
    }

    public void resumePlayback() {
        mRun = true;
        init(context);
        Log.i("AppLog", "resume");
        thread.start();
    }

    public void stopPlayback() {
        mRun = false;
        boolean retry = true;
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    public MjpegView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {
        thread.setSurfaceSize(w, h);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceDone = false;
        stopPlayback();
    }

    public MjpegView(Context context) {
        super(context);
        init(context);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        surfaceDone = true;
    }

    public void showFps(boolean b) {
        showFps = b;
    }

    public void setSource(MjpegInputStream source) {
        mIn = source;
        mInUrl = null; 
//        startPlayback();
    }

    public void setSource(String url) {
    	mInUrl = url;
    	mIn = null;
//        startPlayback();
    }
    
    public void setOverlayPaint(Paint p) {
        overlayPaint = p;
    }

    public void setOverlayTextColor(int c) {
        overlayTextColor = c;
    }

    public void setOverlayBackgroundColor(int c) {
        overlayBackgroundColor = c;
    }

    public void setOverlayPosition(int p) {
        ovlPos = p;
    }

    public void setDisplayMode(int s) {
        displayMode = s;
    }
    
    public void saveBitmap () {
    	if(mRun) {
            Log.i("MjpegView", "saveBitmap start!" );
    		mtakePic = true;
    	} else {
    		Log.i("MjpegView", "saveBitmap error, not running!" );
    	}
    }
    
    @SuppressLint("SimpleDateFormat")
	private String generateFileName() {
    	File sdcard;
    	boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    	if (sdCardExist) 
    	{ 
    		sdcard = Environment.getExternalStorageDirectory();//获取跟目录 
    	} else {
    		return null;
    	}
    	
    	String save2dir = sdcard.toString() + "/" + SAVE_TO_DIR;
    	
    	File fSave2dir  = new File(save2dir);
    	//判断文件夹是否存在,如果不存在则创建文件夹
    	if (!fSave2dir.exists()) {
    		fSave2dir.mkdir();
    	}
    	
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
    	Date curDate = new Date(System.currentTimeMillis());//get current time
    	String str = formatter.format(curDate);
    	
    	String save2file = save2dir + "/" + str + ".png";
    	
    	File fSave2file = new File(save2file);
    	if(fSave2file.exists()) {
            return save2dir + "/" + str + System.currentTimeMillis() + ".png";
    	}
    	
    	return save2file;
    }
    
    private void BroardCastResult (int res, String fName) {
    	Log.i("MjpegView", "BroardCastResult res: " + res);
    	Intent intent = new Intent(Constant.ACTION_TAKE_PICTURE_DONE);
    	intent.putExtra(Constant.EXTRA_RES, res);
    	intent.putExtra(Constant.EXTRA_PATH, fName);
    	context.sendBroadcast(intent);
    }
    
    private int saveBitmapToFile(Bitmap mBitmap, String bitName) {
    	FileOutputStream fOut = null;
    	Log.i("MjpegView", "saveBitmapToFile enter");
    	if (null == bitName || bitName.length() <= 4) {
    		return Constant.CAM_RES_FAIL_FILE_NAME_ERROR;
    	}
    	
    	File f = new File(bitName);
    	Log.i("MjpegView", "saveBitmapToFile, fname =" + f);
    	try {
	    	f.createNewFile();
	    	Log.i("MjpegView", "saveBitmapToFile, createNewFile success, f=" + f);
	    	fOut = new FileOutputStream(f);
	    	Log.i("MjpegView", "saveBitmapToFile, FileOutputStream success, fOut=" + fOut);
    	} catch (Exception e) {
    		Log.i("MjpegView", "exception, err=" + e.getMessage());
    		return Constant.CAM_RES_FAIL_FILE_WRITE_ERROR;
    	}
    	
    	mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
    	
    	try {
    		fOut.flush();
    		fOut.close();
    	} catch (Exception e) {
    		e.printStackTrace();
    		return Constant.CAM_RES_FAIL_BITMAP_ERROR;
    	}
    	
    	return Constant.CAM_RES_OK;
    }
    
    private void handleViewTouch() {
    	SlidingResult slidingResult = SlidingResult.parseSlidingResult(this.touchPointFirst, this.touchPointSecond, displayMetrics);
    	if(slidingResult.getValue() != 0){
    		Log.i("handleViewTouch", slidingResult.toString());
    		if(slidingResult.getDirection().equals(SlidingResult.DIRECTION_HORIZONTAL)){
    			this.horValue += slidingResult.getValue() / 5;
    			this.horValue = this.horValue < 0 ? 0 : this.horValue;
    			this.horValue = this.horValue > 180 ? 180 : this.horValue;
    			this.commandSender.sendCommand(new Command(new CategoryBit(CategoryBit.SERVO), new CommandBit(CommandBit.CHANNEL0), new ValueBit(this.horValue, 0)));
    		}else{
    			this.verValue += slidingResult.getValue() / 5;
    			this.verValue = this.verValue < 0 ? 0 : this.verValue;
    			this.verValue = this.verValue > 180 ? 180 : this.verValue;
    			this.commandSender.sendCommand(new Command(new CategoryBit(CategoryBit.SERVO), new CommandBit(CommandBit.CHANNEL1), new ValueBit(this.verValue, 0)));
    		}
    	}
    }

    public boolean onTouch(View v, MotionEvent event) {

		switch (event.getAction()) {
    	//手按下的时候  
        case MotionEvent.ACTION_DOWN:
        	touchPointFirst.x=(int)event.getX();  
        	touchPointFirst.y=(int)event.getY();
        	break;
        //移动的时候
        case MotionEvent.ACTION_MOVE:
        	break;
        case MotionEvent.ACTION_UP:
        	touchPointSecond.x=(int)event.getX();  
        	touchPointSecond.y=(int)event.getY();
        	handleViewTouch();
        	break;
        default:
        	break;
    	}
        return true;
    }

	public void setCommandSender(CommandSender commandSender) {
		this.commandSender = commandSender;
	}

	public void doCameraReset() {
		this.horValue = 90;
		this.verValue = 90;
		this.commandSender.sendCommand(new Command(new CategoryBit(CategoryBit.SERVO), new CommandBit(CommandBit.CHANNEL0), new ValueBit(this.horValue, 0)));
		this.commandSender.sendCommand(new Command(new CategoryBit(CategoryBit.SERVO), new CommandBit(CommandBit.CHANNEL1), new ValueBit(this.verValue, 0)));
	}
}
