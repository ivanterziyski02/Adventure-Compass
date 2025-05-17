package com.example.adventurecompass;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationAdapterVh>  implements Filterable {
    private List<LocationModel> locationModelList;
    private Context context;
    private List<LocationModel> locationModelListFiltered;
    private SelectedLocation selectedLocation;

    public LocationAdapter(List<LocationModel> locationModelList,SelectedLocation selectedLocation) {
        this.locationModelList = locationModelList;
        this.selectedLocation = selectedLocation;
        this.locationModelListFiltered=locationModelList;

    }

    @NonNull
    @Override
    public LocationAdapter.LocationAdapterVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new LocationAdapterVh(LayoutInflater.from(context).inflate(R.layout.location_item,null));
    }

    @Override
    public void onBindViewHolder(@NonNull LocationAdapter.LocationAdapterVh holder, @SuppressLint("RecyclerView") int position) {
        LocationModel locationModel = locationModelList.get(position);
        String location = locationModel.getName();
        String description = locationModel.getDescription();

        holder.name.setText(location);
        holder.description.setText(description);

        holder.buttonMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, GoogleMapsActivity.class);
                intent.putExtra("LOCATION_NAME",location);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return locationModelList.size();
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();
                if(charSequence == null | charSequence.length() == 0){
                    filterResults.count = locationModelListFiltered.size();
                    filterResults.values = locationModelListFiltered;
                }else {
                    String searchChr = charSequence.toString().toLowerCase();

                    List<LocationModel> resultData = new ArrayList<>();

                    for(LocationModel locationModel: locationModelListFiltered){
                        if(locationModel.getName().toLowerCase().contains(searchChr)){
                            resultData.add(locationModel);
                        }
                    }
                    filterResults.count = resultData.size();
                    filterResults.values = resultData;
                }
                return filterResults;
            }
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                locationModelList = (List<LocationModel>) results.values;
                notifyDataSetChanged();

            }
        };
        return filter;
    }

    public interface SelectedLocation{
        void selectedLocation(LocationModel locationModel);

    }

    public class LocationAdapterVh extends  RecyclerView.ViewHolder{
        TextView name;
        TextView description;
        Button buttonMaps;
        public LocationAdapterVh(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.namelocation);
            description = itemView.findViewById(R.id.descriptionLocation);

            buttonMaps = itemView.findViewById(R.id.maps);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedLocation.selectedLocation(locationModelList.get(getAbsoluteAdapterPosition()));
                }
            });
        }
    }
}
