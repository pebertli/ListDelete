package com.pebertli.listdelete.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

import com.pebertli.listdelete.R;
import com.pebertli.listdelete.helper.SwipeRowHelper;
import com.pebertli.listdelete.models.Country;

public class CountriesAdapter extends RecyclerView.Adapter<CountriesAdapter.CountryViewHolder> {

    public class CountryViewHolder extends RecyclerView.ViewHolder
    {
        private TextView name;
        private TextView currency;
        private TextView language;
        private ImageView deleteButton;

        //pool
        public CountryViewHolder(final View view)
        {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            currency = (TextView) view.findViewById(R.id.currency);
            language = (TextView) view.findViewById(R.id.language);
            deleteButton = (ImageView) view.findViewById(R.id.deleteButton);


            view.findViewById(R.id.foregroundLayout).setOnTouchListener(helper.getNewSwipeTouchListener(this));
            deleteButton.setOnClickListener(helper.getNewSwipeButtonClickListener(this));

        }
    }

    SwipeRowHelper helper;

    private View textViewEmpty;

    public CountriesAdapter(SwipeRowHelper helper) {
        this.helper = helper;
    }

    @Override
    public CountryViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.country_row, parent, false);

        return new CountryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CountryViewHolder holder, int position)
    {
        final View v = holder.itemView.findViewById(R.id.foregroundLayout);
        v.setTranslationX(0); //adjusts the recycled layout
        v.setAlpha(1); //adjusts the recycled layout
        holder.itemView.setAlpha(1); //adjusts the recycled layout
        Country c = helper.rows.get(position);
        holder.name.setText(c.getName());
        holder.currency.setText(c.getCurrency());
        holder.language.setText(c.getLanguage());
    }

    @Override
    public int getItemCount() {
        if(textViewEmpty != null)
            textViewEmpty.setVisibility(this.helper.rows.size() > 0 ? View.GONE : View.VISIBLE);
        return this.helper.rows.size();
    }

    public void setTextViewEmpty(View v)
    {
        textViewEmpty = v;
    }

}
