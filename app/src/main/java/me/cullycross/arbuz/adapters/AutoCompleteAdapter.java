package me.cullycross.arbuz.adapters;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.cullycross.arbuz.utils.LocationHelper;

/**
 * Created by: cullycross
 * Date: 9/5/15
 * For my shining stars!
 */
public class AutoCompleteAdapter extends ArrayAdapter<Address> {
    private LayoutInflater mInflater;
    private Context mContext;

    private List<Address> mAddresses;

    public AutoCompleteAdapter(Context context) {
        super(context, -1);
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public int getCount() {
        return mAddresses.size();
    }

    @Override
    public Address getItem(int position) {
        return mAddresses.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final TextView tv;
        if (convertView != null) {
            tv = (TextView) convertView;
        } else {
            tv = (TextView) mInflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        }

        tv.setText(createFormattedAddressFromAddress(getItem(position)));
        return tv;
    }

    public static String createFormattedAddressFromAddress(final Address address) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.setLength(0);
        final int addressLineSize = address.getMaxAddressLineIndex();
        for (int i = 0; i < addressLineSize; i++) {
            stringBuilder.append(address.getAddressLine(i));
            if (i != addressLineSize - 1) {
                stringBuilder.append(", ");
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private final Filter mFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(final CharSequence constraint) {

            if (constraint != null) {
                try {
                    mAddresses = LocationHelper.getInstance().fetchAddresses(constraint.toString());
                } catch (IOException e) {
                    Toast.makeText(getContext(), "Address wasn't received from the Google service", Toast.LENGTH_SHORT).show();
                }
            }
            if (mAddresses == null) {
                mAddresses = new ArrayList<>();
            }

            final FilterResults filterResults = new FilterResults();
            filterResults.values = mAddresses;
            filterResults.count = mAddresses.size();

            return filterResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(final CharSequence constraint, final Filter.FilterResults results) {
            clear();
            for (Address address : (List<Address>) results.values) {
                add(address);
            }
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

        @Override
        public CharSequence convertResultToString(final Object resultValue) {
            return resultValue == null ? "" : ((Address) resultValue).getAddressLine(0);
        }
    };
}