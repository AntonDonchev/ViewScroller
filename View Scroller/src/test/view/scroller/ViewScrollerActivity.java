package test.view.scroller;

import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import anton.ViewScroller;
import anton.ViewScroller.ORIENTATION;

public class ViewScrollerActivity extends Activity {
	ViewScroller scroller;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		scroller = new ViewScroller(this);
//		scroller.setOrientation(ORIENTATION.VERTICAL);

		for (int i = 0; i < 5; i++) {
			addChild(i);
		}

//		scroller.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				int scrollToScreen = new Random().nextInt(5);
//				scroller.scrollToScreen(scrollToScreen, 500);
//				Log.d("Anton Test", "Scrolling to screen " + scrollToScreen);
//				
//			}
//		});

		setContentView(scroller);

	}

	private void addChild(int i) {
		RelativeLayout screen = new RelativeLayout(this);
		ImageView item = new ImageView(this);
		item.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
		RelativeLayout.LayoutParams itemParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		itemParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		item.setLayoutParams(itemParams);
		
		TextView label = new TextView(this);
		label.setText("Screen " + i);
		
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
		screen.setLayoutParams(params);
		screen.addView(item);
		screen.addView(label);
		scroller.addView(screen);
	}

}