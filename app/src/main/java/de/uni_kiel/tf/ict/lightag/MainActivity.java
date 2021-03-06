package de.uni_kiel.tf.ict.lightag;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.Core.mean;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mIntermediateMat;
    private Mat mRgba;

    private TextView tvInfoBox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("main", "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        tvInfoBox = (TextView) findViewById(R.id.InfoBox);
        tvInfoBox.setText("Startup");

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        try {
            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);
        } catch(NullPointerException npe) {
            Log.e("main", "Nullpointer possible here");
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mRgba = new Mat(height, width, CvType.CV_8UC4);

    }

    public void onCameraViewStopped() {
        mIntermediateMat.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Imgproc.Canny(inputFrame.gray(), mIntermediateMat, 80, 100);

        /*double Mean = 0;
        for (int r=0; r<mIntermediateMat.rows(); r++) {
            for (int c=0; c<mIntermediateMat.cols(); c++) {
                //Mean += mIntermediateMat.get(r, c)[0];
            }
        }*/

        final Scalar Mean = mean(mIntermediateMat);
        Log.i("Mean", Mean.toString());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvInfoBox.setText("Mean" + Mean.toString());
            }
        });

        Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);

        return mRgba;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("main", "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }
}
