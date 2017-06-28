package com.pj.tfighiera.anchorbottomsheet;

import com.pj.tfighiera.anchorbottomsheet.bottomsheet.behaviors.AnchorBottomSheetBehavior;
import com.pj.tfighiera.anchorbottomsheet.exemple.ItemFragment;
import com.pj.tfighiera.anchorbottomsheet.exemple.dummy.DummyContent;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import static com.pj.tfighiera.anchorbottomsheet.bottomsheet.state.StateManager.STATE_EXPANDED;

public class MainActivity extends AppCompatActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

		final NestedScrollView bottomSheet = (NestedScrollView) findViewById(R.id.bottom_sheet);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AnchorBottomSheetBehavior.from(bottomSheet)
				                         .setState(STATE_EXPANDED);
			}
		});
		getSupportFragmentManager().beginTransaction()
		                           .replace(R.id.bottom_sheet, ItemFragment.newInstance(1))
		                           .commit();
	}
}
