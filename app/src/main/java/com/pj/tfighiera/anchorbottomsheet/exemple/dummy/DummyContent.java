package com.pj.tfighiera.anchorbottomsheet.exemple.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent
{
	public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();
	public static final Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();
	private static final int COUNT = 50;

	static
	{
		// Add some sample items.
		for (int i = 1; i <= COUNT; i++)
		{
			addItem(createDummyItem(i));
		}
	}

	private static void addItem(DummyItem item)
	{
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}

	private static DummyItem createDummyItem(int position)
	{
		return new DummyItem(String.valueOf(position), "Item " + position);
	}

	/**
	 * A dummy item representing a piece of content.
	 */
	public static class DummyItem
	{
		public final String id;
		public final String content;

		public DummyItem(String id, String content)
		{
			this.id = id;
			this.content = content;
		}

		@Override
		public String toString()
		{
			return content;
		}
	}
}
