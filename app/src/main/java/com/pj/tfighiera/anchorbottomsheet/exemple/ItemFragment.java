package com.pj.tfighiera.anchorbottomsheet.exemple;

import com.pj.tfighiera.anchorbottomsheet.R;
import com.pj.tfighiera.anchorbottomsheet.exemple.dummy.DummyContent;
import com.pj.tfighiera.anchorbottomsheet.exemple.dummy.DummyContent.DummyItem;
import com.pj.tfighiera.anchorbottomsheet.exemple.dummy.MyItemRecyclerViewAdapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A fragment representing a list of Items.
 * <p />
 */
public class ItemFragment extends Fragment
{
	private static final String ARG_COLUMN_COUNT = "column-count";
	private int mColumnCount;

	public static ItemFragment newInstance(int columnCount)
	{
		ItemFragment fragment = new ItemFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_COLUMN_COUNT, columnCount);
		fragment.setArguments(args);
		return fragment;
	}

	public ItemFragment()
	{
		mColumnCount = 1;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (getArguments() != null)
		{
			mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_item_list, container, false);
		RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
		if (recyclerView != null)
		{
			Context context = view.getContext();
			if (mColumnCount <= 1)
			{
				recyclerView.setLayoutManager(new LinearLayoutManager(context));
			}
			else
			{
				recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
			}
			recyclerView.setAdapter(new MyItemRecyclerViewAdapter(DummyContent.ITEMS));
			ViewCompat.setNestedScrollingEnabled(recyclerView, false);
		}
		return view;
	}
}
