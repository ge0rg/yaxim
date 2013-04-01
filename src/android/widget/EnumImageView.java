/**
 * 
 */
package android.widget;

import org.yaxim.androidclient.util.StatusMode;
import org.yaxim.androidclient.widget.ViewWithValue;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * @author hmeyer
 *
 */
public class EnumImageView extends ImageView implements ViewWithValue {

	@Override
	public void setValue(String value) {
		int drawableId = 0;
		drawableId = StatusMode.valueOf(value).drawableId;
		if (drawableId!=0) {
			setImageResource(drawableId);
			setVisibility(View.VISIBLE);
		}
		else setVisibility(View.GONE);
	}

	/**
	 * @param context
	 */
	public EnumImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public EnumImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public EnumImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
}
