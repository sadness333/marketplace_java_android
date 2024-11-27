package com.example.mypolka;

import android.widget.Filter;

import com.example.mypolka.adapters.AdapterProduct;
import com.example.mypolka.models.ModelProduct;

import java.util.ArrayList;

public class FilterProduct extends Filter {
    private AdapterProduct adapter;
    private ArrayList<ModelProduct> filterList;

    public FilterProduct(AdapterProduct adapter, ArrayList<ModelProduct> filterList){
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint){
        FilterResults results = new FilterResults();
        if(constraint != null && constraint.length() > 0){
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelProduct> filteredModels = new ArrayList<>();
            for (int i =0;i<filterList.size();i++){
                if (filterList.get(i).getCategory().toUpperCase().contains(constraint) ||
                filterList.get(i).getTitle().toUpperCase().contains(constraint)) {
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count =filteredModels.size();
            results.values = filteredModels;
        }else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        adapter.productArrayList = (ArrayList<ModelProduct>) filterResults.values;

        adapter.notifyDataSetChanged();
    }
}
