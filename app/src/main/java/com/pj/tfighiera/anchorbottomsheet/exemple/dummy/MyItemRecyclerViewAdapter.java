package com.pj.tfighiera.anchorbottomsheet.exemple.dummy;

import com.pj.tfighiera.anchorbottomsheet.R;
import com.pj.tfighiera.anchorbottomsheet.exemple.ItemFragment;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>
{
	private final List<DummyContent.DummyItem> mValues;

	public MyItemRecyclerViewAdapter(List<DummyContent.DummyItem> items)
	{
		mValues = items;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(parent.getContext())
		                          .inflate(R.layout.fragment_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position)
	{
		holder.mItem = mValues.get(position);
		holder.mIdView.setText(mValues.get(position).id);
	}

	@Override
	public int getItemCount()
	{
		return mValues.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder
	{
		public final View mView;
		public final TextView mIdView;
		public DummyContent.DummyItem mItem;

		public ViewHolder(View view)
		{
			super(view);
			mView = view;
			mIdView = (TextView) view.findViewById(R.id.id);
		}
	}
}
