
package org.holoeverywhere.slider;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.ThemeManager;
import org.holoeverywhere.addon.AddonSlider.AddonSliderA;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.app.ListFragment;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;

public class SliderMenu {
    public static enum SelectionBehavior {
        BackgroundWhenSelected, Default, OnlyBackground, OnlyHandler;
    }

    public static class SliderItem implements Parcelable {
        public static final Creator<SliderItem> CREATOR = new Creator<SliderMenu.SliderItem>() {
            @Override
            public SliderItem createFromParcel(Parcel source) {
                try {
                    return new SliderItem(source);
                } catch (Exception e) {
                    throw new BadParcelableException(e);
                }
            }

            @Override
            public SliderItem[] newArray(int size) {
                return new SliderItem[size];
            }
        };
        private int mBackgroundColor = 0;
        private int mCustomLayout = 0;
        private Class<? extends Fragment> mFragmentClass;
        private CharSequence mLabel;
        private WeakReference<Fragment> mLastFragment;
        private Fragment.SavedState mSavedState;
        private boolean mSaveState = true;
        private int mSelectionHandlerColor = 0;
        private SliderMenu mSliderMenu;
        private int mTextAppereance = 0;

        private int mTextAppereanceInverse = 0;

        public SliderItem() {
        }

        @SuppressWarnings("unchecked")
        protected SliderItem(Parcel source) throws Exception {
            String classname = source.readString();
            if (classname != null) {
                mFragmentClass = (Class<? extends Fragment>) Class.forName(classname);
            }
            mSavedState = source.readParcelable(Fragment.SavedState.class.getClassLoader());
            mSaveState = source.readInt() == 1;
            mLabel = source.readString();
            mCustomLayout = source.readInt();
            mBackgroundColor = source.readInt();
            mSelectionHandlerColor = source.readInt();
            mTextAppereance = source.readInt();
            mTextAppereanceInverse = source.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public int getBackgroundColor() {
            return mBackgroundColor;
        }

        public int getCustomLayout() {
            return mCustomLayout;
        }

        public Class<? extends Fragment> getFragmentClass() {
            return mFragmentClass;
        }

        public CharSequence getLabel() {
            return mLabel;
        }

        public int getSelectionHandlerColor() {
            return mSelectionHandlerColor;
        }

        public int getTextAppereance() {
            return mTextAppereance;
        }

        public int getTextAppereanceInverse() {
            return mTextAppereanceInverse;
        }

        private void invalidate() {
            if (mSliderMenu != null) {
                mSliderMenu.invalidate();
            }
        }

        public boolean isSaveState() {
            return mSaveState;
        }

        public void setBackgroundColor(int backgroundColor) {
            mBackgroundColor = backgroundColor;
        }

        public void setCustomLayout(int customLayout) {
            mCustomLayout = customLayout;
        }

        public void setFragmentClass(Class<? extends Fragment> fragmentClass) {
            if (mFragmentClass == fragmentClass) {
                return;
            }
            mFragmentClass = fragmentClass;
            mSavedState = null;
        }

        public void setLabel(CharSequence label) {
            mLabel = label;
            invalidate();
        }

        public void setSaveState(boolean saveState) {
            if (mSaveState == saveState) {
                return;
            }
            mSaveState = saveState;
            if (!saveState) {
                mSavedState = null;
            }
        }

        public void setSelectionHandlerColor(int selectionHandlerColor) {
            mSelectionHandlerColor = selectionHandlerColor;
        }

        public void setTextAppereance(int textAppereance) {
            mTextAppereance = textAppereance;
        }

        public void setTextAppereanceInverse(int textAppereanceInverse) {
            mTextAppereanceInverse = textAppereanceInverse;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mFragmentClass == null ? null : mFragmentClass.getName());
            dest.writeParcelable(mSaveState ? mSavedState : null, flags);
            dest.writeInt(mSaveState ? 1 : 0);
            dest.writeString(String.valueOf(mLabel));
            dest.writeInt(mCustomLayout);
            dest.writeInt(mBackgroundColor);
            dest.writeInt(mSelectionHandlerColor);
            dest.writeInt(mTextAppereance);
            dest.writeInt(mTextAppereanceInverse);
        }
    }

    private final class SliderMenuAdapter extends BaseAdapter implements OnItemClickListener {
        private final int mDefaultTextAppearance;
        private final int mDefaultTextAppearanceInverse;
        private final LayoutInflater mLayoutInflater;

        private SliderMenuAdapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
            TypedArray a = context.obtainStyledAttributes(new int[] {
                    android.R.attr.textAppearanceMedium, android.R.attr.textAppearanceMediumInverse
            });
            mDefaultTextAppearance = a.getResourceId(0,
                    R.style.Holo_TextAppearance_Medium);
            mDefaultTextAppearanceInverse = a.getResourceId(1,
                    R.style.Holo_TextAppearance_Medium_Inverse);
            a.recycle();
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public SliderItem getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).mCustomLayout != 0 ? position
                    : Adapter.IGNORE_ITEM_VIEW_TYPE;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final SliderItem item = getItem(position);
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(item.mCustomLayout != 0 ? item.mCustomLayout
                        : R.layout.slider_menu_item, parent, false);
            }
            return bindView(item, convertView, mCurrentPage == position,
                    mDefaultTextAppearance, mDefaultTextAppearanceInverse);
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            setCurrentPage(position, false);
        }
    }

    public static final int[] BLUE = new int[] {
            R.color.holo_blue_dark, R.color.holo_blue_light
    };
    public static final int[] GREEN = new int[] {
            R.color.holo_green_dark, R.color.holo_green_light
    };

    private static final String KEY_CURRENT_PAGE = ":slider:currentPage";

    public static final int[] ORANGE = new int[] {
            R.color.holo_orange_dark, R.color.holo_orange_light
    };

    public static final int[] PURPLE = new int[] {
            R.color.holo_purple_dark, R.color.holo_purple_light
    };

    public static final int[] RED = new int[] {
            R.color.holo_red_dark, R.color.holo_red_light
    };

    private static void setTextAppearance(TextView textView, int resid) {
        if (resid != 0) {
            textView.setTextAppearance(textView.getContext(), resid);
        }
    }

    private SliderMenuAdapter mAdapter;

    private final AddonSliderA mAddon;

    private int mCurrentPage = -1;

    private int mFuturePosition = -1;

    private int mInitialPage = 0;

    private boolean mInverseTextColorWhenSelected = false;

    private final List<SliderItem> mItems;

    private SelectionBehavior mSelectionBehavior = SelectionBehavior.Default;

    public SliderMenu(AddonSliderA addon) {
        mAddon = addon;
        mItems = new ArrayList<SliderItem>();
    }

    public void add(CharSequence label, Class<? extends Fragment> fragmentClass) {
        add(label, fragmentClass, null);
    }

    public void add(CharSequence label, Class<? extends Fragment> fragmentClass, int[] colors) {
        SliderItem item = new SliderItem();
        item.setLabel(label);
        item.setFragmentClass(fragmentClass);
        if (colors != null && colors.length >= 2) {
            final Resources res = mAddon.get().getResources();
            item.setBackgroundColor(res.getColor(colors[0]));
            item.setSelectionHandlerColor(res.getColor(colors[1]));
        }
        add(item);
    }

    public void add(int label, Class<? extends Fragment> fragmentClass) {
        add(label, fragmentClass, null);
    }

    public void add(int label, Class<? extends Fragment> fragmentClass, int[] colors) {
        add(mAddon.get().getResources().getText(label), fragmentClass, colors);
    }

    public void add(SliderItem item) {
        if (item.mSliderMenu != null) {
            throw new IllegalArgumentException("Item already has a parent: "
                    + item + " (" + item.mSliderMenu + ")");
        }
        item.mSliderMenu = this;
        mItems.add(item);
        notifyChanged();
    }

    public void add(SliderItem item, int position) {
        if (item.mSliderMenu != null) {
            throw new IllegalArgumentException("Item already has a parent: "
                    + item + " (" + item.mSliderMenu + ")");
        }
        item.mSliderMenu = this;
        mItems.add(position, item);
        notifyChanged();
    }

    public void bind(ListFragment listFragment) {
        bind(listFragment, null);
    }

    public void bind(ListFragment listFragment, Context context) {
        bind(listFragment.getListView(), context);
    }

    public void bind(ListView listView) {
        bind(listView, null);
    }

    public void bind(ListView listView, Context context) {
        if (mAdapter != null) {
            throw new IllegalStateException("No more than one view allowed");
        }
        mAdapter = new SliderMenuAdapter(context == null ? listView.getContext() : context);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(mAdapter);
        if (mFuturePosition >= 0) {
            setCurrentPage(mFuturePosition, true);
            mFuturePosition = -1;
        }
    }

    public void bindOnLeftPanel() {
        bindOnLeftPanel(null);
    }

    public void bindOnLeftPanel(Context context) {
        tryToBind(mAddon.getLeftView(), context);
    }

    public void bindOnRightPanel() {
        bindOnRightPanel(null);
    }

    public void bindOnRightPanel(Context context) {
        tryToBind(mAddon.getRightView(), context);
    }

    @SuppressWarnings("deprecation")
    public View bindView(SliderItem item, View view, boolean selected,
            int defaultTextAppearance, int defaultTextAppearanceInverse) {
        TextView labelView = (TextView) view.findViewById(android.R.id.text1);
        if (labelView != null) {
            labelView.setText(item.mLabel);
        }
        final int textAppereance = item.mTextAppereance != 0 ?
                item.mTextAppereance : defaultTextAppearance;
        final int textAppereanceInverse = item.mTextAppereanceInverse != 0 ?
                item.mTextAppereanceInverse : defaultTextAppearanceInverse;
        setTextAppearance(labelView,
                mInverseTextColorWhenSelected ? selected ? textAppereanceInverse
                        : textAppereance : textAppereance);
        if (mSelectionBehavior == null) {
            return view;
        }
        View selectionHandlerView = view.findViewById(R.id.selectionHandler);
        switch (mSelectionBehavior) {
            case Default:
                if (selected) {
                    view.setBackgroundColor(item.mBackgroundColor);
                    if (selectionHandlerView != null) {
                        selectionHandlerView.setBackgroundColor(item.mSelectionHandlerColor);
                    }
                } else {
                    view.setBackgroundDrawable(null);
                    if (selectionHandlerView != null) {
                        selectionHandlerView.setBackgroundDrawable(null);
                    }
                }
                break;
            case BackgroundWhenSelected:
                if (selectionHandlerView != null) {
                    selectionHandlerView.setBackgroundColor(item.mSelectionHandlerColor);
                }
                if (selected) {
                    view.setBackgroundColor(item.mBackgroundColor);
                } else {
                    view.setBackgroundDrawable(null);
                }
                break;
            case OnlyBackground:
                if (selected) {
                    view.setBackgroundColor(item.mBackgroundColor);
                } else {
                    view.setBackgroundDrawable(null);
                }
                break;
            case OnlyHandler:
                if (selectionHandlerView != null) {
                    if (selected) {
                        selectionHandlerView.setBackgroundColor(item.mSelectionHandlerColor);
                    } else {
                        selectionHandlerView.setBackgroundDrawable(null);
                    }
                }
                break;
        }
        return view;
    }

    protected void changePage(int position, SliderItem item) {
        final FragmentManager fm = mAddon.get().getSupportFragmentManager();
        if (mCurrentPage >= 0) {
            final SliderItem lastItem = mAdapter.getItem(mCurrentPage);
            final WeakReference<Fragment> ref = lastItem.mLastFragment;
            final Fragment fragment = ref == null ? null : ref.get();
            if (fragment != null && lastItem.mSaveState) {
                if (!fragment.isDetached()) {
                    fm.beginTransaction().detach(fragment).commit();
                    fm.executePendingTransactions();
                }
                lastItem.mSavedState = fm.saveFragmentInstanceState(fragment);
            }
        }
        mCurrentPage = position;
        mAdapter.notifyDataSetInvalidated();
        while (fm.popBackStackImmediate()) {
        }
        final Fragment fragment = Fragment.instantiate(item.mFragmentClass);
        if (item.mSavedState != null) {
            fragment.setInitialSavedState(item.mSavedState);
        }
        item.mLastFragment = new WeakReference<Fragment>(fragment);
        replaceFragment(fm, fragment);
    }

    public int getInitialPage() {
        return mInitialPage;
    }

    public SelectionBehavior getSelectionBehavior() {
        return mSelectionBehavior;
    }

    public int indexOfItem(SliderItem item) {
        return mItems.indexOf(item);
    }

    public void invalidate() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetInvalidated();
        }
    }

    public boolean isInverseTextColorWhenSelected() {
        return mInverseTextColorWhenSelected;
    }

    public void makeDefaultMenu() {
        makeDefaultMenu(mAddon.get());
    }

    public void makeDefaultMenu(Context context) {
        setInverseTextColorWhenSelected(ThemeManager.getThemeType(mAddon.get()) != ThemeManager.LIGHT);
        ListFragment menuFragment = (ListFragment) mAddon.get().getSupportFragmentManager()
                .findFragmentById(R.id.leftView);
        if (menuFragment == null) {
            mAddon.get().getSupportFragmentManager().findFragmentById(R.id.rightView);
        }
        if (menuFragment == null) {
            throw new IllegalStateException("Couldn't find ListFragment for menu");
        }
        bind(menuFragment, context);
    }

    private void notifyChanged() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public void onPostCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCurrentPage = savedInstanceState.getInt(KEY_CURRENT_PAGE, 0);
        }
    }

    public void onResume() {
        if (mCurrentPage < 0 && mItems.size() > 0) {
            setCurrentPage(mInitialPage, true);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_CURRENT_PAGE, mCurrentPage);
    }

    public void remove(int position) {
        mItems.remove(position).mSliderMenu = null;
        notifyChanged();
    }

    public void remove(SliderItem item) {
        if (mItems.remove(item)) {
            item.mSliderMenu = null;
        }
        notifyChanged();
    }

    protected void replaceFragment(FragmentManager fm, Fragment fragment) {
        fm.beginTransaction().replace(R.id.contentView, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
    }

    public void setCurrentPage(int position) {
        setCurrentPage(position, true);
    }

    private void setCurrentPage(int position, boolean force) {
        if (mAdapter == null) {
            mFuturePosition = position;
        }
        if (position < 0 || position >= mItems.size()) {
            throw new IndexOutOfBoundsException();
        }
        if (force || mCurrentPage != position
                || mAddon.get().getSupportFragmentManager().getBackStackEntryCount() > 0) {
            changePage(position, mAdapter.getItem(position));
            mCurrentPage = position;
        }
        if (mAddon.isAddonEnabled()) {
            mAddon.openContentViewDelayed(40);
        }
    }

    public void setInitialPage(int initialPage) {
        mInitialPage = initialPage;
    }

    public void setInverseTextColorWhenSelected(boolean inverseTextColorWhenSelected) {
        mInverseTextColorWhenSelected = inverseTextColorWhenSelected;
    }

    public void setSelectionBehavior(SelectionBehavior selectionBehavior) {
        mSelectionBehavior = selectionBehavior;
    }

    private void tryToBind(View view, Context context) {
        if (view instanceof ListView) {
            bind((ListView) view, context);
            return;
        }
        final ListView listView = (ListView) view.findViewById(android.R.id.list);
        if (listView != null) {
            bind(listView, context);
            return;
        }
        throw new IllegalStateException("Couldn't find ListView on panel");
    }
}
