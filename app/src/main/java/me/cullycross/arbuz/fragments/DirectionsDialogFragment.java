package me.cullycross.arbuz.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.cullycross.arbuz.R;
import me.cullycross.arbuz.adapters.AutoCompleteAdapter;
import me.cullycross.arbuz.utils.DelayAutoCompleteTextView;

/**
 * Created by: cullycross
 * Date: 9/5/15
 * For my shining stars!
 */
public class DirectionsDialogFragment extends DialogFragment
        implements DialogInterface.OnClickListener,
        ZeroLengthTextListener {

    @Bind(R.id.delay_autocomplete_from)
    DelayAutoCompleteTextView mDelayAutoCompleteFrom;
    @Bind(R.id.delay_autocomplete_to)
    DelayAutoCompleteTextView mDelayAutoCompleteTo;
    @Bind(R.id.spinner_route_types)
    Spinner mSpinnerRouteTypes;
    @Bind(R.id.progress_bar_from)
    ProgressBar mProgressBarFrom;
    @Bind(R.id.progress_bar_to)
    ProgressBar mProgressBarTo;

    private OnFragmentInteractionListener mListener;

    private static final int SEARCH_THRESHOLD = 2;

    public static DirectionsDialogFragment newInstance() {
        DirectionsDialogFragment fragment = new DirectionsDialogFragment();

        // set args here

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.fragment_dialog_directions, null);

        ButterKnife.bind(this, dialogView);

        builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton("Find", this)
                .setNegativeButton("Cancel", this);
        return builder.create();
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        switch (which) {

            case DialogInterface.BUTTON_POSITIVE:
                //todo(CullyCross): find the safe way
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                DirectionsDialogFragment.this.getDialog().cancel();
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        initViews();

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        setPositiveButtonEnabled(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private void initViews() {

        mDelayAutoCompleteFrom.setThreshold(SEARCH_THRESHOLD);
        mDelayAutoCompleteTo.setThreshold(SEARCH_THRESHOLD);

        mDelayAutoCompleteFrom.setAdapter(new AutoCompleteAdapter(getActivity()));
        mDelayAutoCompleteFrom.setLoadingIndicator(mProgressBarFrom);

        mDelayAutoCompleteTo.setAdapter(new AutoCompleteAdapter(getActivity()));
        mDelayAutoCompleteTo.setLoadingIndicator(mProgressBarTo);

        MoreThanOneTextWatcher watcher = new MoreThanOneTextWatcher();
        watcher.add(mDelayAutoCompleteFrom)
                .add(mDelayAutoCompleteTo);

        watcher.setListener(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.direction_route_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerRouteTypes.setAdapter(adapter);
    }

    @Override
    public void onTextChanged(boolean everyFieldIsFilled) {
        setPositiveButtonEnabled(everyFieldIsFilled);
    }

    private void setPositiveButtonEnabled(boolean flag) {
        ((AlertDialog) getDialog())
                .getButton(DialogInterface.BUTTON_POSITIVE)
                .setEnabled(flag);
    }

    public static class MoreThanOneTextWatcher implements TextWatcher {

        private List<EditText> mEditTexts;
        private ZeroLengthTextListener mListener;

        public MoreThanOneTextWatcher() {
            mEditTexts = new ArrayList<>();
        }

        public MoreThanOneTextWatcher add(EditText editText) {
            mEditTexts.add(editText);
            editText.addTextChangedListener(this);
            return this;
        }

        public boolean remove(EditText e) {
            return mEditTexts.remove(e);
        }

        public void setListener(ZeroLengthTextListener listener) {
            mListener = listener;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            boolean flag = true;
            for(EditText editText : mEditTexts) {
                if(editText.getText().length() == 0) {
                    flag = false;
                    break;
                }
            }
            mListener.onTextChanged(flag);
        }
    }

    public interface OnFragmentInteractionListener {
        void onWayFound();
    }
}

interface ZeroLengthTextListener {
    void onTextChanged(boolean everyFieldIsFilled);
}